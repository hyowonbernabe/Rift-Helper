package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Auto-honor: after a game, honor every ballot-eligible player who is on the friends list, up to
 * the 4-vote maximum.
 *
 * The exact ballot / friends JSON shapes and the honor-player request body are confirmed against a
 * live client: {@link #honorFriends()} prints the raw ballot and friends payloads and every honor
 * response code to stdout, so the first real run reveals the true field names if a guess is off.
 * Matching is done by summonerId OR puuid, so it works whichever identifier the ballot carries.
 */
public class Honor {
    private static final int MAX_HONORS = 4;

    /** A ballot-eligible player, identified by whichever ids are present. */
    public record Player(long summonerId, String puuid) {}

    public static int honorFriends() {
        // The ballot is empty for the first moment after PreEndOfGame fires (eligible lists not yet
        // populated, gameId still 0). Poll until it fills. Bounded so Arena / no-honor modes, where
        // it never populates, just fall through and skip.
        String ballotJson = "{}";
        List<Player> eligible = new ArrayList<>();
        for (int attempt = 0; attempt < 8 && eligible.isEmpty(); attempt++) {
            ballotJson = LCUGet.getFromClient("/lol-honor-v2/v1/ballot");
            eligible = parseEligiblePlayers(ballotJson);
            if (eligible.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.out.println("[AutoHonor] ballot=" + ballotJson);

        if (eligible.isEmpty()) {
            System.out.println("[AutoHonor] no eligible players (Arena / no honor, or ballot never populated); skipping");
            return 0;
        }

        String friendsJson = LCUGet.getFromClient("/lol-chat/v1/friends");
        Set<Long> friendSummonerIds = new HashSet<>();
        Set<String> friendPuuids = new HashSet<>();
        parseFriends(friendsJson, friendSummonerIds, friendPuuids);

        int votes = Math.min(parseVotes(ballotJson), MAX_HONORS);
        List<Player> toHonor = friendsToHonor(eligible, friendSummonerIds, friendPuuids, votes);
        System.out.println("[AutoHonor] eligible=" + eligible.size() + " friends=" + friendSummonerIds.size()
                + " votes=" + votes + " toHonor=" + toHonor.size());

        int honored = 0;
        for (Player p : toHonor) {
            String body = honorBody(p);
            int code = LCUPost.postToClientWithBody("/lol-honor-v2/v1/honor-player", body);
            System.out.println("[AutoHonor] honor " + body + " -> " + code);
            if (code >= 200 && code < 300) {
                honored++;
            }
        }
        return honored;
    }

    /** Honor votes available for this game (votePool.votes), defaulting to the max if absent. */
    private static int parseVotes(String ballotJson) {
        try {
            JsonObject root = JsonParser.parseString(ballotJson).getAsJsonObject();
            if (root.has("votePool") && root.get("votePool").isJsonObject()) {
                JsonObject vp = root.getAsJsonObject("votePool");
                if (vp.has("votes") && !vp.get("votes").isJsonNull()) {
                    return vp.get("votes").getAsInt();
                }
            }
        } catch (Exception ignored) {
        }
        return MAX_HONORS;
    }

    /** Pure: eligible players who are friends (by summonerId or puuid), deduped, capped at max. */
    static List<Player> friendsToHonor(List<Player> eligible, Set<Long> friendSummonerIds,
                                       Set<String> friendPuuids, int max) {
        List<Player> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Player p : eligible) {
            boolean isFriend = (p.summonerId() > 0 && friendSummonerIds.contains(p.summonerId()))
                    || (p.puuid() != null && !p.puuid().isEmpty() && friendPuuids.contains(p.puuid()));
            if (!isFriend) {
                continue;
            }
            String key = p.summonerId() > 0 ? "s" + p.summonerId() : "p" + p.puuid();
            if (seen.add(key)) {
                out.add(p);
                if (out.size() >= max) {
                    break;
                }
            }
        }
        return out;
    }

    private static String honorBody(Player p) {
        if (p.summonerId() > 0) {
            return "{\"summonerId\":" + p.summonerId() + "}";
        }
        return "{\"puuid\":\"" + p.puuid() + "\"}";
    }

    // Ballot lists eligible players under one of these arrays depending on client version/mode.
    private static final String[] ELIGIBLE_KEYS = {"eligiblePlayers", "eligibleAllies", "eligibleOpponents"};

    private static List<Player> parseEligiblePlayers(String ballotJson) {
        List<Player> players = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(ballotJson).getAsJsonObject();
            for (String key : ELIGIBLE_KEYS) {
                if (!root.has(key) || !root.get(key).isJsonArray()) {
                    continue;
                }
                for (JsonElement el : root.getAsJsonArray(key)) {
                    if (!el.isJsonObject()) {
                        continue;
                    }
                    JsonObject o = el.getAsJsonObject();
                    players.add(new Player(asLong(o, "summonerId"), asString(o, "puuid")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }

    private static void parseFriends(String friendsJson, Set<Long> summonerIds, Set<String> puuids) {
        try {
            JsonArray arr = JsonParser.parseString(friendsJson).getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                long sid = asLong(o, "summonerId");
                if (sid > 0) {
                    summonerIds.add(sid);
                }
                String puuid = asString(o, "puuid");
                if (puuid != null && !puuid.isEmpty()) {
                    puuids.add(puuid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long asLong(JsonObject o, String key) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private static String asString(JsonObject o, String key) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Pure-logic self-check: java -ea model.Honor
    public static void main(String[] args) {
        List<Player> eligible = List.of(
                new Player(1L, "pa"),   // friend by summonerId
                new Player(2L, "pb"),   // not a friend
                new Player(0L, "pc"),   // friend by puuid only
                new Player(3L, "pd"),   // friend
                new Player(4L, "pe"),   // friend
                new Player(5L, "pf"));  // friend, but over the cap of 4
        Set<Long> fSids = new HashSet<>(List.of(1L, 3L, 4L, 5L));
        Set<String> fPuuids = new HashSet<>(List.of("pc"));

        List<Player> result = friendsToHonor(eligible, fSids, fPuuids, MAX_HONORS);
        assert result.size() == 4 : "expected 4 (capped), got " + result.size();
        assert result.get(0).summonerId() == 1L;
        assert result.get(1).puuid().equals("pc");
        assert result.get(2).summonerId() == 3L;
        assert result.get(3).summonerId() == 4L;
        assert friendsToHonor(List.of(new Player(9L, "z")), fSids, fPuuids, 4).isEmpty()
                : "non-friend should not be honored";
        System.out.println("Honor.friendsToHonor self-check passed.");
    }
}
