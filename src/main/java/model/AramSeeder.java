package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the ARAM preference survey with a smart starting order: champions the player actually
 * plays float to the top, everything else falls to the bottom alphabetically. Reads lifetime
 * mastery + the last ~50 games from the local client (LCU) and blends them into a single score.
 *
 * <p>Everything is keyed by champion NAME (via {@link DDragonParser}) so the order stays stable
 * across Data Dragon versions. All LCU/JSON work is defensive: any failure logs {@code [Seeder]}
 * and degrades (skip the game / return an empty map). {@link #fetch()} never throws.
 *
 * <p>Score = normalized mastery + 0.6 * normalized recent play, where recent play weights ranked
 * SR games at half an ARAM game (this is an ARAM survey). Both terms are normalized against the
 * per-map maxima so the scale is roughly [0, 1.6] regardless of the player's absolute mastery.
 */
public class AramSeeder {

    /** Per-champion play context surfaced next to each survey entry. */
    public static final class ChampInfo {
        public String name;
        public long masteryPoints;      // 0 if none
        public int masteryLevel;
        public String highestGrade = ""; // "" if none
        public long lastPlayMillis;     // 0 if unknown
        public int aramGames, mayhemGames, srGames;
        public int recentWins, recentLosses;
        public double score;            // filled by computeScores

        public ChampInfo() {
        }

        public ChampInfo(String name) {
            this.name = name;
        }
    }

    /** Recent play weight: ARAM + Mayhem count full, SR counts half (this is an ARAM survey). */
    private static double weightedRecent(ChampInfo info) {
        return info.aramGames + info.mayhemGames + 0.5 * info.srGames;
    }

    /**
     * Pure. Fills each {@code info.score}. Mastery weight 1.0, recent-games boost 0.6.
     * weightedRecent = aramGames + mayhemGames + 0.5*srGames.
     * score = (maxPts>0 ? masteryPoints/maxPts : 0) + 0.6 * (maxRecent>0 ? weightedRecent/maxRecent : 0)
     * where maxPts / maxRecent are the maxima across the map (normalization).
     */
    public static void computeScores(Map<String, ChampInfo> infos) {
        long maxPts = 0L;
        double maxRecent = 0.0;
        for (ChampInfo info : infos.values()) {
            if (info.masteryPoints > maxPts) {
                maxPts = info.masteryPoints;
            }
            double wr = weightedRecent(info);
            if (wr > maxRecent) {
                maxRecent = wr;
            }
        }
        for (ChampInfo info : infos.values()) {
            double masteryPart = maxPts > 0 ? (double) info.masteryPoints / maxPts : 0.0;
            double recentPart = maxRecent > 0 ? weightedRecent(info) / maxRecent : 0.0;
            info.score = masteryPart + 0.6 * recentPart;
        }
    }

    /**
     * Pure. Roster sorted by (score desc, then name asc). Champions absent from {@code infos} OR
     * with score == 0 go after all scored champions, alphabetical. Returns a new list; never
     * mutates {@code roster}.
     */
    public static List<String> seededOrder(Map<String, ChampInfo> infos, List<String> roster) {
        List<String> scored = new ArrayList<>();
        List<String> unscored = new ArrayList<>();
        for (String name : roster) {
            ChampInfo info = infos.get(name);
            if (info != null && info.score > 0) {
                scored.add(name);
            } else {
                unscored.add(name);
            }
        }
        scored.sort((a, b) -> {
            int cmp = Double.compare(infos.get(b).score, infos.get(a).score); // score desc
            return cmp != 0 ? cmp : a.compareTo(b);                            // then name asc
        });
        unscored.sort(String::compareTo);
        List<String> out = new ArrayList<>(scored.size() + unscored.size());
        out.addAll(scored);
        out.addAll(unscored);
        return out;
    }

    /**
     * LCU fetch: builds a {@link ChampInfo} per champion (name via {@link DDragonParser}, skipping
     * "Unknown Champion"), merging lifetime mastery with the last ~50 games (Arena excluded), then
     * calls {@link #computeScores}. Keys are champion NAMES. Returns an EMPTY map on any failure;
     * never throws.
     */
    public static Map<String, ChampInfo> fetch() {
        Map<String, ChampInfo> infos = new HashMap<>();
        try {
            mergeMastery(infos);
            String puuid = fetchPuuid();
            if (!puuid.isEmpty()) {
                mergeRecentGames(infos, puuid);
            }
            computeScores(infos);
            return infos;
        } catch (Exception e) {
            System.out.println("[Seeder] fetch failed: " + e);
            return new HashMap<>();
        }
    }

    /** Lifetime mastery for every played champion. */
    private static void mergeMastery(Map<String, ChampInfo> infos) {
        try {
            String json = LCUGet.getFromClient("/lol-champion-mastery/v1/local-player/champion-mastery");
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                String name = DDragonParser.getChampionName(asInt(o, "championId"));
                if (name == null || name.equals("Unknown Champion")) {
                    continue;
                }
                ChampInfo info = infos.computeIfAbsent(name, ChampInfo::new);
                info.masteryPoints = asLong(o, "championPoints");
                info.masteryLevel = asInt(o, "championLevel");
                info.highestGrade = asString(o, "highestGrade", "");
                info.lastPlayMillis = asLong(o, "lastPlayTime");
            }
        } catch (Exception e) {
            System.out.println("[Seeder] mastery fetch/parse failed: " + e);
        }
    }

    /** Current summoner puuid, or "" if unavailable. */
    private static String fetchPuuid() {
        try {
            String json = LCUGet.getFromClient("/lol-summoner/v1/current-summoner");
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            return asString(o, "puuid", "");
        } catch (Exception e) {
            System.out.println("[Seeder] current-summoner fetch/parse failed: " + e);
            return "";
        }
    }

    /** Last ~50 games; classifies each and folds it into the matching ChampInfo (Arena ignored). */
    private static void mergeRecentGames(Map<String, ChampInfo> infos, String puuid) {
        try {
            String json = LCUGet.getFromClient(
                    "/lol-match-history/v1/products/lol/" + puuid + "/matches?begIndex=0&endIndex=49");
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray games = root.getAsJsonObject("games").getAsJsonArray("games");
            for (JsonElement gEl : games) {
                try {
                    if (gEl.isJsonObject()) {
                        mergeGame(gEl.getAsJsonObject(), puuid, infos);
                    }
                } catch (Exception e) {
                    // one malformed game shouldn't sink the rest
                    System.out.println("[Seeder] skipping unparsable game: " + e);
                }
            }
        } catch (Exception e) {
            System.out.println("[Seeder] match-history fetch/parse failed: " + e);
        }
    }

    // Mode buckets for a recent game.
    private static final int MODE_SKIP = 0, MODE_ARAM = 1, MODE_MAYHEM = 2, MODE_SR = 3;

    /** SR = mapId 11; ARAM = queueId 450; ARAM Mayhem = queueId 2400; Arena (mapId 30) / else skip. */
    private static int classify(JsonObject game) {
        int queueId = asInt(game, "queueId");
        if (queueId == 450) {
            return MODE_ARAM;
        }
        if (queueId == 2400) {
            return MODE_MAYHEM;
        }
        if (asInt(game, "mapId") == 11) {
            return MODE_SR;
        }
        return MODE_SKIP;
    }

    private static void mergeGame(JsonObject game, String puuid, Map<String, ChampInfo> infos) {
        int mode = classify(game);
        if (mode == MODE_SKIP) {
            return;
        }

        // Map the summoner's puuid to their participantId for this game.
        int participantId = -1;
        JsonArray idents = game.has("participantIdentities") && game.get("participantIdentities").isJsonArray()
                ? game.getAsJsonArray("participantIdentities") : null;
        if (idents == null) {
            return;
        }
        for (JsonElement el : idents) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject ident = el.getAsJsonObject();
            JsonObject player = ident.has("player") && ident.get("player").isJsonObject()
                    ? ident.getAsJsonObject("player") : null;
            if (player != null && puuid.equals(asString(player, "puuid", ""))) {
                participantId = asInt(ident, "participantId");
                break;
            }
        }
        if (participantId < 0) {
            return;
        }

        // Find that participant and read their champion + win.
        JsonArray participants = game.has("participants") && game.get("participants").isJsonArray()
                ? game.getAsJsonArray("participants") : null;
        if (participants == null) {
            return;
        }
        for (JsonElement el : participants) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject p = el.getAsJsonObject();
            if (asInt(p, "participantId") != participantId) {
                continue;
            }
            String name = DDragonParser.getChampionName(asInt(p, "championId"));
            if (name == null || name.equals("Unknown Champion")) {
                return;
            }
            ChampInfo info = infos.computeIfAbsent(name, ChampInfo::new);
            switch (mode) {
                case MODE_ARAM:
                    info.aramGames++;
                    break;
                case MODE_MAYHEM:
                    info.mayhemGames++;
                    break;
                case MODE_SR:
                    info.srGames++;
                    break;
                default:
                    break;
            }
            boolean win = false;
            if (p.has("stats") && p.get("stats").isJsonObject()) {
                win = asBool(p.getAsJsonObject("stats"), "win");
            }
            if (win) {
                info.recentWins++;
            } else {
                info.recentLosses++;
            }
            return;
        }
    }

    // ---- defensive JSON accessors (same style as Honor) --------------------------------------

    private static int asInt(JsonObject o, String key) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static long asLong(JsonObject o, String key) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private static String asString(JsonObject o, String key, String def) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean asBool(JsonObject o, String key) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() && o.get(key).getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    // Pure-logic self-check: java -ea model.AramSeeder
    public static void main(String[] args) {
        Map<String, ChampInfo> m = new HashMap<>();

        ChampInfo a = new ChampInfo("A");
        a.masteryPoints = 500000;
        m.put("A", a);

        ChampInfo b = new ChampInfo("B");
        b.masteryPoints = 300000;
        m.put("B", b);

        ChampInfo c = new ChampInfo("C"); // no mastery, but recently played a lot
        c.aramGames = 8;
        m.put("C", c);

        ChampInfo d = new ChampInfo("D"); // in the map but never played
        m.put("D", d);

        computeScores(m);

        // Recent play outranks a never-played champ; more mastery outranks less.
        assert c.score > d.score : "C (recent) should outscore D (unplayed): c=" + c.score + " d=" + d.score;
        assert a.score > b.score : "A should outscore B: a=" + a.score + " b=" + b.score;
        assert d.score == 0.0 : "unplayed champ scores 0, got " + d.score;

        // "Absent" is not in the map at all -> unscored, alphabetical bottom group.
        List<String> roster = List.of("D", "C", "Absent", "B", "A");
        List<String> order = seededOrder(m, roster);

        assert order.get(0).equals("A") : "A should be first, got " + order;
        assert order.get(order.size() - 1).equals("D") : "D should be last, got " + order;
        // Absent champ sorts below every scored champ, alphabetically (before D: 'A' < 'D').
        assert order.indexOf("Absent") > order.indexOf("C")
                : "absent champ should sort after all scored champs, got " + order;
        assert order.equals(List.of("A", "B", "C", "Absent", "D")) : "full order = " + order;

        // seededOrder must not mutate its input roster.
        assert roster.equals(List.of("D", "C", "Absent", "B", "A")) : "roster was mutated: " + roster;

        System.out.println("AramSeeder self-check passed.");
    }
}
