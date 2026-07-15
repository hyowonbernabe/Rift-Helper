package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Lobby / end-of-game LCU actions for the auto game-start loop.
 *
 * All calls are best-effort: LCU POSTs return a status code we tolerate (for example the
 * matchmaking search fails with a non-2xx when the user is not the lobby leader, which we ignore).
 */
public class Lobby {

    /** Number of players currently in the lobby, or 0 if not in a lobby / on error. */
    public static int memberCount() {
        String json = LCUGet.getFromClient("/lol-lobby/v2/lobby");
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray members = root.getAsJsonArray("members");
            return members == null ? 0 : members.size();
        } catch (Exception e) {
            return 0; // "{}" (not in a lobby) or malformed
        }
    }

    /**
     * Press "Play Again": returns the whole party to the lobby, keeping the group (unlike recreating
     * a lobby, which would kick everyone). Does NOT dismiss stats first - that skips past the lobby
     * to phase "None" and makes play-again 500. Retries briefly because the endpoint 500s until the
     * end-of-game scoreboard has settled.
     */
    public static void playAgain() {
        for (int i = 0; i < 6; i++) {
            int code = LCUPost.postToClient("/lol-lobby/v2/play-again");
            System.out.println("[AutoSkip] play-again attempt " + (i + 1) + " -> " + code);
            if (code >= 200 && code < 300) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** Start matchmaking. No-op in effect if not the lobby leader (POST just errors). */
    public static void startSearch() {
        LCUPost.postToClient("/lol-lobby/v2/lobby/matchmaking/search");
    }
}
