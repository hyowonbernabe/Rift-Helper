package model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-claim event / battlepass rewards via the LCU so the client's unclaimed-reward notifications
 * stay cleared. Passes are enumerated live, so new passes (new eventIds each act/season) are covered
 * automatically with no configuration. Fully tolerant: bad JSON, missing fields, or a disconnected
 * client are skipped rather than thrown.
 */
public class Rewards {

    /** POST reward-track/claim-all for every current event pass. Returns how many passes accepted the
     *  claim (HTTP 200/204). A pass with nothing to claim is a harmless no-op. */
    public static int claimAllPasses() {
        int claimed = 0;
        for (String eventId : parseEventIds(LCUGet.getFromClient("/lol-event-hub/v1/events"))) {
            int code = LCUPost.postToClient("/lol-event-hub/v1/events/" + eventId + "/reward-track/claim-all");
            if (code == 200 || code == 204) {
                claimed++;
            }
        }
        return claimed;
    }

    /** Extract eventId strings from the /lol-event-hub/v1/events array. Tolerant of the "{}" body a
     *  GET returns on error, malformed JSON, and entries missing an eventId. */
    static List<String> parseEventIds(String eventsJson) {
        List<String> ids = new ArrayList<>();
        try {
            JsonElement root = JsonParser.parseString(eventsJson);
            if (root.isJsonArray()) {
                for (JsonElement el : root.getAsJsonArray()) {
                    try {
                        String id = el.getAsJsonObject().get("eventId").getAsString();
                        if (id != null && !id.isBlank()) {
                            ids.add(id);
                        }
                    } catch (Exception ignore) {
                        // skip a malformed event entry
                    }
                }
            }
        } catch (Exception ignore) {
            // non-JSON / "{}" -> no events
        }
        return ids;
    }

    // Self-check: java -ea model.Rewards
    public static void main(String[] args) {
        assert parseEventIds("[{\"eventId\":\"a\"},{\"eventId\":\"b\"}]").equals(List.of("a", "b"));
        assert parseEventIds("{}").isEmpty() : "non-array body -> empty";
        assert parseEventIds("garbage").isEmpty() : "bad json -> empty";
        assert parseEventIds("[{\"nope\":1},{\"eventId\":\"c\"}]").equals(List.of("c")) : "skip missing eventId";
        assert parseEventIds("[]").isEmpty();
        System.out.println("Rewards self-check passed.");
    }
}
