package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Pre-game SCOUT: gathers intel on the players in the current game (allies during champ select,
 * all 10 once the game is loading / in progress) and hands the controller/view a ready-to-render
 * report for the "Players" tab.
 *
 * <h2>How the controller uses it</h2>
 * <pre>
 *   // On a BACKGROUND thread (do NOT call on the EDT - this does blocking LCU I/O):
 *   ScoutReport report = ScoutReport.build();                 // resolves phase itself
 *   ScoutReport report = ScoutReport.build(lastGameflowPhase);// or pass the phase you already track
 *
 *   // Or fire-and-forget; the callback is delivered on the Swing EDT so the view can render:
 *   ScoutReport.refreshAsync(report -> view.showScout(report));
 *   ScoutReport.refreshAsync(lastGameflowPhase, report -> view.showScout(report));
 * </pre>
 *
 * <p>The synchronous {@link #build()} does all the LCU work on the calling thread (fan-out over a
 * bounded daemon pool internally), so the controller must invoke it off the EDT. {@link #build()}
 * never throws: any failed/hung LCU call is tolerated (GET returns {@code "{}"}; a per-call timeout
 * guard skips anything that hangs) and simply leaves the affected fields blank.
 *
 * <p>When there is no active roster (gameflow phase None / Matchmaking with no lobby / errors),
 * the returned report has {@link #isEmpty()} == true so the view can show its empty state instead
 * of a table.
 */
public class ScoutReport {

    /** Gameflow phase this report was built for (e.g. "ChampSelect", "InProgress", "None"). */
    public final String phase;

    /** Local player's team (champ-select myTeam, lobby members, or the in-game side we are on). */
    public final List<ScoutPlayer> allies;

    /** Enemy team. Empty during champ select until names are revealed / until the game loads. */
    public final List<ScoutPlayer> enemies;

    public ScoutReport(String phase, List<ScoutPlayer> allies, List<ScoutPlayer> enemies) {
        this.phase = phase == null ? "None" : phase;
        this.allies = allies == null ? new ArrayList<>() : allies;
        this.enemies = enemies == null ? new ArrayList<>() : enemies;
    }

    /** True when there is no one to scout (no active game). The view should show its empty state. */
    public boolean isEmpty() {
        return allies.isEmpty() && enemies.isEmpty();
    }

    /** allies + enemies in one list (allies first). */
    public List<ScoutPlayer> allPlayers() {
        List<ScoutPlayer> out = new ArrayList<>(allies.size() + enemies.size());
        out.addAll(allies);
        out.addAll(enemies);
        return out;
    }

    /** Convenience for the "played recently" alert: every player flagged {@code playedRecently}. */
    public List<ScoutPlayer> playedRecentlyPlayers() {
        List<ScoutPlayer> out = new ArrayList<>();
        for (ScoutPlayer p : allPlayers()) {
            if (p.playedRecently) {
                out.add(p);
            }
        }
        return out;
    }

    // ============================ public API: build / refresh ============================

    /** Resolve the current phase from the client, then build. Blocking - call OFF the EDT. */
    public static ScoutReport build() {
        return build(null);
    }

    /**
     * Build a report for {@code phase} (pass the gameflow phase you already track, or null to have
     * this resolve it via {@code GET /lol-gameflow/v1/gameflow-phase}). Blocking - call OFF the EDT.
     * Never throws; returns an empty report on any failure.
     */
    public static ScoutReport build(String phase) {
        try {
            String resolved = normalizePhase(phase != null ? phase : fetchPhase());
            String myPuuid = fetchMyPuuid();

            Roster roster = rosterForPhase(resolved, myPuuid);
            List<ScoutPlayer> all = new ArrayList<>(roster.allies);
            all.addAll(roster.enemies);

            if (all.isEmpty()) {
                return new ScoutReport(resolved, roster.allies, roster.enemies); // empty state
            }

            enrichAll(all);
            applyRecentOverlap(all, fetchMyRecentGames(myPuuid, RECENT_ALERT_GAMES));
            return new ScoutReport(resolved, roster.allies, roster.enemies);
        } catch (Exception e) {
            System.out.println("[Scout] build failed: " + e);
            return new ScoutReport(phase, new ArrayList<>(), new ArrayList<>());
        }
    }

    /** Fire-and-forget build on a daemon thread; {@code callback} runs on the Swing EDT. */
    public static void refreshAsync(Consumer<ScoutReport> callback) {
        refreshAsync(null, callback);
    }

    /** Fire-and-forget build on a daemon thread; {@code callback} runs on the Swing EDT. */
    public static void refreshAsync(String phase, Consumer<ScoutReport> callback) {
        Thread t = new Thread(() -> {
            ScoutReport report = build(phase);
            if (callback != null) {
                SwingUtilities.invokeLater(() -> callback.accept(report));
            }
        }, "scout-refresh");
        t.setDaemon(true);
        t.start();
    }

    // ============================ pure logic (self-checkable) ============================

    /** Number of most-recent games of the local player scanned for the overlap alert. */
    public static final int RECENT_ALERT_GAMES = 3;

    /**
     * One of the local player's recent games, reduced to the puuids on each side. The local
     * player's own puuid is intentionally NOT included in either set, so scouting yourself never
     * self-flags as "played recently".
     */
    public static final class RecentGame {
        public final Set<String> allyPuuids;   // the local player's teammates that game
        public final Set<String> enemyPuuids;  // the local player's opponents that game

        public RecentGame(Set<String> allyPuuids, Set<String> enemyPuuids) {
            this.allyPuuids = allyPuuids == null ? new HashSet<>() : allyPuuids;
            this.enemyPuuids = enemyPuuids == null ? new HashSet<>() : enemyPuuids;
        }
    }

    /**
     * PURE. For each player, count how many of {@code lastGames} they appeared in (by puuid) and
     * set {@code playedRecently} / {@code playedRecentlyCount} / {@code playedRecentlyAsEnemy}:
     * TRUE if every overlap was on the enemy side, FALSE if every overlap was as a teammate, null
     * if unknown or mixed. Players with a blank puuid are left untouched. No network.
     */
    public static void applyRecentOverlap(List<ScoutPlayer> players, List<RecentGame> lastGames) {
        if (players == null) {
            return;
        }
        List<RecentGame> games = lastGames == null ? List.of() : lastGames;
        for (ScoutPlayer p : players) {
            if (p == null || p.puuid == null || p.puuid.isEmpty()) {
                continue;
            }
            int inGames = 0, asAlly = 0, asEnemy = 0;
            for (RecentGame g : games) {
                boolean ally = g.allyPuuids.contains(p.puuid);
                boolean enemy = g.enemyPuuids.contains(p.puuid);
                if (ally || enemy) {
                    inGames++;
                }
                if (ally) {
                    asAlly++;
                }
                if (enemy) {
                    asEnemy++;
                }
            }
            p.playedRecently = inGames > 0;
            p.playedRecentlyCount = inGames;
            if (inGames == 0) {
                p.playedRecentlyAsEnemy = null;
            } else if (asEnemy > 0 && asAlly == 0) {
                p.playedRecentlyAsEnemy = Boolean.TRUE;
            } else if (asAlly > 0 && asEnemy == 0) {
                p.playedRecentlyAsEnemy = Boolean.FALSE;
            } else {
                p.playedRecentlyAsEnemy = null; // mixed
            }
        }
    }

    /** PURE. Normalize the gameflow phase string ("\"ChampSelect\"" or "ChampSelect" -> "ChampSelect"). */
    static String normalizePhase(String raw) {
        if (raw == null) {
            return "None";
        }
        String s = raw.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.isEmpty() || "{}".equals(s) ? "None" : s;
    }

    /**
     * PURE. Format a ranked queue entry into a display line, or "Unranked" if no tier. Master+
     * tiers drop the division. Example: "Gold II 45 LP (120W/95L)".
     */
    static String formatRank(String tier, String division, int leaguePoints, int wins, int losses) {
        if (tier == null || tier.isEmpty() || "NONE".equalsIgnoreCase(tier) || "UNRANKED".equalsIgnoreCase(tier)) {
            return "Unranked";
        }
        StringBuilder sb = new StringBuilder(prettyCase(tier));
        if (!isApexTier(tier) && division != null && !division.isEmpty() && !"NA".equalsIgnoreCase(division)) {
            sb.append(' ').append(division.toUpperCase());
        }
        sb.append(' ').append(leaguePoints).append(" LP");
        if (wins > 0 || losses > 0) {
            sb.append(" (").append(wins).append("W/").append(losses).append("L)");
        }
        return sb.toString();
    }

    private static boolean isApexTier(String tier) {
        String t = tier.toUpperCase();
        return t.equals("MASTER") || t.equals("GRANDMASTER") || t.equals("CHALLENGER");
    }

    private static String prettyCase(String s) {
        String t = s.toLowerCase();
        return t.isEmpty() ? t : Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    // ============================ roster (phase-aware source) ============================

    /** allies + enemies for a phase; either list may be empty. */
    private static final class Roster {
        final List<ScoutPlayer> allies = new ArrayList<>();
        final List<ScoutPlayer> enemies = new ArrayList<>();
    }

    private static Roster rosterForPhase(String phase, String myPuuid) {
        switch (phase) {
            case "ChampSelect":
                return champSelectRoster();
            case "GameStart":
            case "InProgress":
            case "Reconnect":
                return gameflowRoster(myPuuid);
            case "Lobby":
            case "Matchmaking":
            case "ReadyCheck":
                return lobbyRoster();
            default:
                return new Roster(); // None / EndOfGame / WaitingForStats / ... -> empty
        }
    }

    /**
     * Champ select: {@code GET /lol-champ-select/v1/session}. myTeam -> allies (always have puuids);
     * theirTeam -> enemies, but their puuids are usually blank until reveal, so blanks are skipped.
     * VERIFIED path (used elsewhere as {@code /lol-champ-select/v1/session}).
     */
    private static Roster champSelectRoster() {
        Roster r = new Roster();
        try {
            JsonObject root = obj(timedGet("/lol-champ-select/v1/session"));
            addCells(root.getAsJsonArray("myTeam"), r.allies, Boolean.TRUE);
            addCells(root.getAsJsonArray("theirTeam"), r.enemies, Boolean.FALSE);
        } catch (Exception e) {
            System.out.println("[Scout] champ-select roster failed: " + e);
        }
        return r;
    }

    /** Parse champ-select cells (myTeam / theirTeam) directly from raw JSON (Team model has no puuid). */
    private static void addCells(JsonArray cells, List<ScoutPlayer> into, Boolean ally) {
        if (cells == null) {
            return;
        }
        for (JsonElement el : cells) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject c = el.getAsJsonObject();
            String puuid = asString(c, "puuid", "");
            if (puuid.isEmpty()) {
                continue; // hidden enemy cell pre-reveal
            }
            ScoutPlayer p = new ScoutPlayer(puuid);
            p.isAlly = ally;
            p.summonerName = firstNonEmpty(asString(c, "gameName", ""), asString(c, "summonerName", ""));
            int champ = asInt(c, "championId");
            if (champ <= 0) {
                champ = asInt(c, "championPickIntent");
            }
            p.championId = champ > 0 ? champ : null;
            into.add(p);
        }
    }

    /**
     * In-game: {@code GET /lol-gameflow/v1/session} -> gameData.teamOne + gameData.teamTwo (all 10).
     * The side containing our own puuid becomes allies; the other becomes enemies. If our puuid is
     * not found, teamOne is treated as allies (isAlly left as reported). VERIFIED path.
     */
    private static Roster gameflowRoster(String myPuuid) {
        Roster r = new Roster();
        try {
            JsonObject root = obj(timedGet("/lol-gameflow/v1/session"));
            JsonObject gameData = root.has("gameData") && root.get("gameData").isJsonObject()
                    ? root.getAsJsonObject("gameData") : null;
            if (gameData == null) {
                return r;
            }
            JsonArray t1 = gameData.has("teamOne") && gameData.get("teamOne").isJsonArray()
                    ? gameData.getAsJsonArray("teamOne") : null;
            JsonArray t2 = gameData.has("teamTwo") && gameData.get("teamTwo").isJsonArray()
                    ? gameData.getAsJsonArray("teamTwo") : null;

            boolean iAmOnTwo = myPuuid != null && !myPuuid.isEmpty() && arrayHasPuuid(t2, myPuuid);
            JsonArray allyArr = iAmOnTwo ? t2 : t1;
            JsonArray enemyArr = iAmOnTwo ? t1 : t2;
            addGameData(allyArr, r.allies, Boolean.TRUE);
            addGameData(enemyArr, r.enemies, Boolean.FALSE);
        } catch (Exception e) {
            System.out.println("[Scout] gameflow roster failed: " + e);
        }
        return r;
    }

    private static void addGameData(JsonArray players, List<ScoutPlayer> into, Boolean ally) {
        if (players == null) {
            return;
        }
        for (JsonElement el : players) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject o = el.getAsJsonObject();
            String puuid = asString(o, "puuid", "");
            ScoutPlayer p = new ScoutPlayer(puuid);
            p.isAlly = ally;
            p.summonerName = firstNonEmpty(asString(o, "summonerName", ""), asString(o, "gameName", ""));
            int champ = asInt(o, "championId");
            p.championId = champ > 0 ? champ : null;
            into.add(p);
        }
    }

    /** Lobby: {@code GET /lol-lobby/v2/lobby} -> members (premades only). All treated as allies. */
    private static Roster lobbyRoster() {
        Roster r = new Roster();
        try {
            JsonObject root = obj(timedGet("/lol-lobby/v2/lobby"));
            JsonArray members = root.has("members") && root.get("members").isJsonArray()
                    ? root.getAsJsonArray("members") : null;
            if (members == null) {
                return r;
            }
            for (JsonElement el : members) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject m = el.getAsJsonObject();
                String puuid = asString(m, "puuid", "");
                if (puuid.isEmpty()) {
                    continue;
                }
                ScoutPlayer p = new ScoutPlayer(puuid);
                p.isAlly = Boolean.TRUE;
                p.summonerName = firstNonEmpty(asString(m, "gameName", ""), asString(m, "summonerName", ""));
                r.allies.add(p);
            }
        } catch (Exception e) {
            System.out.println("[Scout] lobby roster failed: " + e);
        }
        return r;
    }

    // ============================ per-player enrichment (parallel) ============================

    /** Fan out per-player enrichment over a bounded daemon pool; bounded so a slow game can't hang. */
    private static void enrichAll(List<ScoutPlayer> players) {
        int parallel = Math.min(MAX_PARALLEL, Math.max(1, players.size()));
        ExecutorService pool = Executors.newFixedThreadPool(parallel, daemonFactory("scout-enrich"));
        try {
            List<Callable<Void>> tasks = new ArrayList<>(players.size());
            for (ScoutPlayer p : players) {
                tasks.add(() -> {
                    enrichOne(p);
                    return null;
                });
            }
            pool.invokeAll(tasks, ENRICH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[Scout] enrichAll failed: " + e);
        } finally {
            pool.shutdownNow();
        }
    }

    private static void enrichOne(ScoutPlayer p) {
        if (p.puuid == null || p.puuid.isEmpty()) {
            return; // bot / hidden cell -> nothing to enrich
        }
        enrichSummoner(p);
        enrichRanked(p);
        enrichMastery(p);
        enrichRecentForm(p);
    }

    /**
     * Name + level. Per-puuid {@code GET /lol-summoner/v2/summoners/puuid/{puuid}} (CONFIRMED live;
     * the v1 {@code summoners-by-puuid/{puuid}} path 404s with "Invalid URI format"). {@code gameName}
     * is the Riot ID; {@code displayName} is deprecated/empty on current clients, so gameName wins.
     */
    private static void enrichSummoner(ScoutPlayer p) {
        try {
            JsonObject o = obj(timedGet("/lol-summoner/v2/summoners/puuid/" + p.puuid));
            int lvl = asInt(o, "summonerLevel");
            if (lvl > 0) {
                p.level = lvl;
            }
            String name = firstNonEmpty(asString(o, "gameName", ""), asString(o, "displayName", ""));
            if (!name.isEmpty()) {
                p.summonerName = name;
            }
        } catch (Exception e) {
            // leave name/level as-is (from roster source)
        }
    }

    /**
     * Ranked solo + flex. {@code GET /lol-ranked/v1/ranked-stats/{puuid}} -> {@code queues[]} with
     * queueType / tier / division / leaguePoints / wins / losses. VERIFY-LIVE: confirm the response
     * uses a "queues" array (older builds also expose "highestRankedEntry"); unranked / bots return
     * "{}" or empty and are left as "Unranked".
     */
    private static void enrichRanked(ScoutPlayer p) {
        try {
            JsonObject o = obj(timedGet("/lol-ranked/v1/ranked-stats/" + p.puuid));
            JsonArray queues = o.has("queues") && o.get("queues").isJsonArray()
                    ? o.getAsJsonArray("queues") : null;
            String solo = "Unranked";
            String flex = "Unranked";
            if (queues != null) {
                for (JsonElement el : queues) {
                    if (!el.isJsonObject()) {
                        continue;
                    }
                    JsonObject q = el.getAsJsonObject();
                    String type = asString(q, "queueType", "");
                    String line = formatRank(asString(q, "tier", ""), asString(q, "division", ""),
                            asInt(q, "leaguePoints"), asInt(q, "wins"), asInt(q, "losses"));
                    if ("RANKED_SOLO_5x5".equals(type)) {
                        solo = line;
                    } else if ("RANKED_FLEX_SR".equals(type)) {
                        flex = line;
                    }
                }
            }
            p.soloRank = solo;
            p.flexRank = flex;
        } catch (Exception e) {
            // leave "" -> view shows nothing
        }
    }

    /**
     * Top mastery champion. {@code GET /lol-champion-mastery/v1/{puuid}/champion-mastery} -> array of
     * {championId, championPoints}, sorted by points descending (CONFIRMED live; the {@code /top}
     * variant returns 405 WRONG_METHOD). We still scan for the max so order can't bite us.
     */
    private static void enrichMastery(ScoutPlayer p) {
        try {
            String json = timedGet("/lol-champion-mastery/v1/" + p.puuid + "/champion-mastery");
            JsonArray arr = arr(json);
            long bestPts = -1;
            Integer bestId = null;
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                long pts = asLong(o, "championPoints");
                int id = asInt(o, "championId");
                if (id > 0 && pts > bestPts) {
                    bestPts = pts;
                    bestId = id;
                }
            }
            if (bestId != null) {
                p.topMasteryChampId = bestId;
                p.topMasteryPoints = Math.max(0, bestPts);
            }
        } catch (Exception e) {
            // leave null
        }
    }

    /** Recent W/L over the player's last {@value #RECENT_FORM_GAMES} games (any queue). */
    private static void enrichRecentForm(ScoutPlayer p) {
        int[] wl = winsLosses(p.puuid, RECENT_FORM_GAMES);
        p.recentWins = wl[0];
        p.recentLosses = wl[1];
    }

    /** Number of most-recent games summed into each player's W/L "recent form". */
    private static final int RECENT_FORM_GAMES = 5;

    /**
     * W/L over a puuid's last {@code count} games. {@code GET
     * /lol-match-history/v1/products/lol/{puuid}/matches?begIndex=0&endIndex=count-1} (same endpoint
     * AramSeeder reads; ~50 game cap). Returns {wins, losses}; {0,0} on any failure / bot.
     */
    private static int[] winsLosses(String puuid, int count) {
        int wins = 0, losses = 0;
        try {
            String json = timedGet("/lol-match-history/v1/products/lol/" + puuid
                    + "/matches?begIndex=0&endIndex=" + Math.max(0, count - 1));
            JsonArray games = matchHistoryGames(json);
            for (JsonElement gEl : games) {
                if (!gEl.isJsonObject()) {
                    continue;
                }
                Boolean win = didWin(gEl.getAsJsonObject(), puuid);
                if (win == null) {
                    continue;
                }
                if (win) {
                    wins++;
                } else {
                    losses++;
                }
            }
        } catch (Exception e) {
            // {0,0}
        }
        return new int[]{wins, losses};
    }

    /** stats.win for the given puuid in one match-history game, or null if not determinable. */
    private static Boolean didWin(JsonObject game, String puuid) {
        int pid = participantIdFor(game, puuid);
        if (pid < 0) {
            return null;
        }
        JsonArray participants = game.has("participants") && game.get("participants").isJsonArray()
                ? game.getAsJsonArray("participants") : null;
        if (participants == null) {
            return null;
        }
        for (JsonElement el : participants) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject p = el.getAsJsonObject();
            if (asInt(p, "participantId") != pid) {
                continue;
            }
            if (p.has("stats") && p.get("stats").isJsonObject()) {
                return asBool(p.getAsJsonObject("stats"), "win");
            }
            return null;
        }
        return null;
    }

    // ============================ "played recently" alert (network side) ============================

    /**
     * The local player's last {@code count} games, each reduced to ally/enemy puuid sets (with our
     * own puuid excluded). Reads {@code /lol-match-history/v1/products/lol/{myPuuid}/matches}. Returns
     * an empty list if the puuid is blank or on any failure - the pure matcher then flags nobody.
     */
    private static List<RecentGame> fetchMyRecentGames(String myPuuid, int count) {
        List<RecentGame> out = new ArrayList<>();
        if (myPuuid == null || myPuuid.isEmpty()) {
            return out;
        }
        try {
            String json = timedGet("/lol-match-history/v1/products/lol/" + myPuuid
                    + "/matches?begIndex=0&endIndex=" + Math.max(0, count - 1));
            JsonArray games = matchHistoryGames(json);
            for (JsonElement gEl : games) {
                if (!gEl.isJsonObject()) {
                    continue;
                }
                RecentGame rg = toRecentGame(gEl.getAsJsonObject(), myPuuid);
                if (rg != null) {
                    out.add(rg);
                }
            }
        } catch (Exception e) {
            System.out.println("[Scout] my recent games fetch failed: " + e);
        }
        return out;
    }

    /** Split one game into ally/enemy puuid sets relative to {@code myPuuid} (excluding myself). */
    private static RecentGame toRecentGame(JsonObject game, String myPuuid) {
        try {
            // participantId -> puuid, from participantIdentities.
            Map<Integer, String> pidToPuuid = new HashMap<>();
            JsonArray idents = game.has("participantIdentities") && game.get("participantIdentities").isJsonArray()
                    ? game.getAsJsonArray("participantIdentities") : null;
            if (idents == null) {
                return null;
            }
            for (JsonElement el : idents) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject ident = el.getAsJsonObject();
                JsonObject player = ident.has("player") && ident.get("player").isJsonObject()
                        ? ident.getAsJsonObject("player") : null;
                if (player != null) {
                    pidToPuuid.put(asInt(ident, "participantId"), asString(player, "puuid", ""));
                }
            }

            // participantId -> teamId, from participants; and find my participantId + team.
            Map<Integer, Integer> pidToTeam = new HashMap<>();
            JsonArray participants = game.has("participants") && game.get("participants").isJsonArray()
                    ? game.getAsJsonArray("participants") : null;
            if (participants == null) {
                return null;
            }
            for (JsonElement el : participants) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject p = el.getAsJsonObject();
                pidToTeam.put(asInt(p, "participantId"), asInt(p, "teamId"));
            }

            int myPid = -1;
            for (Map.Entry<Integer, String> e : pidToPuuid.entrySet()) {
                if (myPuuid.equals(e.getValue())) {
                    myPid = e.getKey();
                    break;
                }
            }
            if (myPid < 0) {
                return null;
            }
            int myTeam = pidToTeam.getOrDefault(myPid, -1);

            Set<String> allySet = new HashSet<>();
            Set<String> enemySet = new HashSet<>();
            for (Map.Entry<Integer, String> e : pidToPuuid.entrySet()) {
                int pid = e.getKey();
                String puuid = e.getValue();
                if (pid == myPid || puuid == null || puuid.isEmpty()) {
                    continue; // exclude self / blanks
                }
                int team = pidToTeam.getOrDefault(pid, -2);
                if (team == myTeam) {
                    allySet.add(puuid);
                } else {
                    enemySet.add(puuid);
                }
            }
            return new RecentGame(allySet, enemySet);
        } catch (Exception e) {
            return null;
        }
    }

    // ============================ LCU helpers ============================

    private static final int MAX_PARALLEL = 8;
    private static final long GET_TIMEOUT_MS = 3500;    // per LCU call guard
    private static final long ENRICH_TIMEOUT_MS = 15000; // overall enrichment fan-out cap

    /** Cached daemon pool that runs each guarded LCU GET so a hung call can't block forever. */
    private static final ExecutorService GET_POOL =
            Executors.newCachedThreadPool(daemonFactory("scout-get"));

    /** GET with a hard timeout; returns "{}" on timeout/failure so callers can parse uniformly. */
    private static String timedGet(String endpoint) {
        try {
            Future<String> f = GET_POOL.submit(() -> LCUGet.getFromClient(endpoint));
            return f.get(GET_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** Current summoner puuid, or "" if unavailable. {@code GET /lol-summoner/v1/current-summoner}. */
    private static String fetchMyPuuid() {
        try {
            return asString(obj(timedGet("/lol-summoner/v1/current-summoner")), "puuid", "");
        } catch (Exception e) {
            return "";
        }
    }

    /** {@code GET /lol-gameflow/v1/gameflow-phase} -> a quoted phase string, or "None" on error. */
    private static String fetchPhase() {
        return timedGet("/lol-gameflow/v1/gameflow-phase");
    }

    private static ThreadFactory daemonFactory(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }

    // -- JSON parsing helpers (defensive; same style as AramSeeder) ----------------------------

    /** Match-history games array from the products/lol response ({@code root.games.games}). */
    private static JsonArray matchHistoryGames(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject games = root.has("games") && root.get("games").isJsonObject()
                    ? root.getAsJsonObject("games") : null;
            if (games != null && games.has("games") && games.get("games").isJsonArray()) {
                return games.getAsJsonArray("games");
            }
        } catch (Exception e) {
            // fall through
        }
        return new JsonArray();
    }

    /** Map a puuid to its participantId in a match-history game, or -1. */
    private static int participantIdFor(JsonObject game, String puuid) {
        JsonArray idents = game.has("participantIdentities") && game.get("participantIdentities").isJsonArray()
                ? game.getAsJsonArray("participantIdentities") : null;
        if (idents == null) {
            return -1;
        }
        for (JsonElement el : idents) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject ident = el.getAsJsonObject();
            JsonObject player = ident.has("player") && ident.get("player").isJsonObject()
                    ? ident.getAsJsonObject("player") : null;
            if (player != null && puuid.equals(asString(player, "puuid", ""))) {
                return asInt(ident, "participantId");
            }
        }
        return -1;
    }

    private static boolean arrayHasPuuid(JsonArray arr, String puuid) {
        if (arr == null || puuid == null || puuid.isEmpty()) {
            return false;
        }
        for (JsonElement el : arr) {
            if (el.isJsonObject() && puuid.equals(asString(el.getAsJsonObject(), "puuid", ""))) {
                return true;
            }
        }
        return false;
    }

    /** Parse to a JsonObject, or an empty object on any failure ("{}" tolerant). */
    private static JsonObject obj(String json) {
        try {
            JsonElement e = JsonParser.parseString(json);
            if (e.isJsonObject()) {
                return e.getAsJsonObject();
            }
        } catch (Exception ignore) {
            // fall through
        }
        return new JsonObject();
    }

    /** Parse to a JsonArray, or an empty array on any failure. */
    private static JsonArray arr(String json) {
        try {
            JsonElement e = JsonParser.parseString(json);
            if (e.isJsonArray()) {
                return e.getAsJsonArray();
            }
        } catch (Exception ignore) {
            // fall through
        }
        return new JsonArray();
    }

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

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        return b == null ? "" : b;
    }

    // ============================ self-check (pure only; no network) ============================

    // java -ea model.ScoutReport
    public static void main(String[] args) {
        // ---- isEmpty() on an empty roster ----
        ScoutReport empty = new ScoutReport("None", new ArrayList<>(), new ArrayList<>());
        assert empty.isEmpty() : "empty roster should be isEmpty()";
        assert empty.playedRecentlyPlayers().isEmpty() : "no players -> no alerts";

        ScoutReport nonEmpty = new ScoutReport("ChampSelect",
                new ArrayList<>(List.of(new ScoutPlayer("me"))), new ArrayList<>());
        assert !nonEmpty.isEmpty() : "roster with a player is not empty";

        // ---- recent-overlap matcher ----
        // My last 3 games (my own puuid excluded from the sets, as the fetcher does).
        RecentGame g1 = new RecentGame(setOf("ally1", "ally2"), setOf("enemyA", "enemyB"));
        RecentGame g2 = new RecentGame(setOf("ally1"), setOf("enemyA", "enemyC"));
        RecentGame g3 = new RecentGame(setOf("ally3"), setOf("enemyA"));
        List<RecentGame> last3 = List.of(g1, g2, g3);

        ScoutPlayer enemyA = new ScoutPlayer("enemyA");   // enemy in all 3
        ScoutPlayer ally1 = new ScoutPlayer("ally1");     // ally in 2
        ScoutPlayer mixed = new ScoutPlayer("mixed");     // ally once, enemy once
        ScoutPlayer stranger = new ScoutPlayer("nobody"); // not seen
        ScoutPlayer blank = new ScoutPlayer("");          // no puuid -> untouched

        // "mixed" appears as ally in g1 and enemy in g2.
        g1.allyPuuids.add("mixed");
        g2.enemyPuuids.add("mixed");

        List<ScoutPlayer> roster = new ArrayList<>(List.of(enemyA, ally1, mixed, stranger, blank));
        applyRecentOverlap(roster, last3);

        assert enemyA.playedRecently && enemyA.playedRecentlyCount == 3
                : "enemyA should be flagged in 3 games, got " + enemyA.playedRecentlyCount;
        assert Boolean.TRUE.equals(enemyA.playedRecentlyAsEnemy) : "enemyA was always an enemy";

        assert ally1.playedRecently && ally1.playedRecentlyCount == 2
                : "ally1 should be flagged in 2 games, got " + ally1.playedRecentlyCount;
        assert Boolean.FALSE.equals(ally1.playedRecentlyAsEnemy) : "ally1 was always a teammate";

        assert mixed.playedRecently && mixed.playedRecentlyCount == 2 : "mixed in 2 games";
        assert mixed.playedRecentlyAsEnemy == null : "mixed (ally+enemy) -> asEnemy null";

        assert !stranger.playedRecently && stranger.playedRecentlyCount == 0 : "stranger not seen";
        assert stranger.playedRecentlyAsEnemy == null : "unseen -> asEnemy null";

        assert !blank.playedRecently : "blank puuid left untouched";

        // Report-level alert convenience reflects the flags.
        ScoutReport rep = new ScoutReport("InProgress",
                new ArrayList<>(List.of(ally1, mixed, blank, stranger)),
                new ArrayList<>(List.of(enemyA)));
        assert rep.playedRecentlyPlayers().size() == 3
                : "alert list should be ally1 + mixed + enemyA = 3, got " + rep.playedRecentlyPlayers().size();

        // Null-safety: null games list flags nobody, doesn't throw.
        ScoutPlayer solo = new ScoutPlayer("x");
        applyRecentOverlap(new ArrayList<>(List.of(solo)), null);
        assert !solo.playedRecently : "null games -> not flagged";

        // ---- phase normalization + rank formatting (pure) ----
        assert normalizePhase("\"ChampSelect\"").equals("ChampSelect") : "strip quotes";
        assert normalizePhase("InProgress").equals("InProgress") : "plain passes through";
        assert normalizePhase("{}").equals("None") : "empty JSON -> None";
        assert normalizePhase(null).equals("None") : "null -> None";

        assert formatRank("GOLD", "II", 45, 120, 95).equals("Gold II 45 LP (120W/95L)")
                : "solo format, got " + formatRank("GOLD", "II", 45, 120, 95);
        assert formatRank("", "", 0, 0, 0).equals("Unranked") : "no tier -> Unranked";
        assert formatRank("CHALLENGER", "I", 1200, 300, 250).equals("Challenger 1200 LP (300W/250L)")
                : "apex drops division, got " + formatRank("CHALLENGER", "I", 1200, 300, 250);
        assert formatRank("SILVER", "IV", 0, 0, 0).equals("Silver IV 0 LP")
                : "no games -> no W/L suffix, got " + formatRank("SILVER", "IV", 0, 0, 0);

        System.out.println("ScoutReport self-check passed.");
    }

    private static Set<String> setOf(String... s) {
        return new HashSet<>(List.of(s));
    }
}
