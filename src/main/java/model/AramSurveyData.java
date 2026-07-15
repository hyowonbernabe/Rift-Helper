package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Serializable state of the ARAM preference survey. Plain data; no I/O here. */
public class AramSurveyData {
    public int schema = 1;
    public String ddragonVersion = "";
    public Map<String, String> tiers = new LinkedHashMap<>();             // name -> tier (main/like/fine/never)
    public Map<String, List<String>> rankedOrder = new LinkedHashMap<>(); // tier -> ordered names
    public Map<String, String> comparisons = new LinkedHashMap<>();       // "a|b" (sorted) -> winner or "TIE"
    public Map<String, Boolean> stageDone = new LinkedHashMap<>();        // tier -> ranked?
    public long completedAt = 0L;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static AramSurveyData empty() {
        return new AramSurveyData();
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static AramSurveyData fromJson(String json) {
        AramSurveyData d = GSON.fromJson(json, AramSurveyData.class);
        if (d == null) {
            d = new AramSurveyData();
        }
        if (d.tiers == null) {
            d.tiers = new LinkedHashMap<>();
        }
        if (d.rankedOrder == null) {
            d.rankedOrder = new LinkedHashMap<>();
        }
        if (d.comparisons == null) {
            d.comparisons = new LinkedHashMap<>();
        }
        if (d.stageDone == null) {
            d.stageDone = new LinkedHashMap<>();
        }
        return d;
    }

    public AramSurveyData deepCopy() {
        return fromJson(toJson());
    }

    /** Stable key for a pair of champions, order-independent. */
    public static String pairKey(String a, String b) {
        return (a.compareTo(b) <= 0) ? a + "|" + b : b + "|" + a;
    }

    /** Names assigned to a tier, in the insertion order of the tiers map. */
    public List<String> namesInTier(String tier) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : tiers.entrySet()) {
            if (tier.equals(e.getValue())) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    /** decided = champions with any tier assigned (including never). */
    public int decidedCount() {
        return tiers.size();
    }

    // Self-check: java -ea model.AramSurveyData
    public static void main(String[] args) {
        AramSurveyData d = empty();
        d.tiers.put("Ahri", "main");
        d.tiers.put("Lux", "like");
        d.tiers.put("Garen", "never");
        d.comparisons.put(pairKey("Zed", "Ahri"), "Ahri");
        assert pairKey("Zed", "Ahri").equals("Ahri|Zed") : "pairKey must sort";
        assert pairKey("Ahri", "Zed").equals("Ahri|Zed");
        assert d.decidedCount() == 3;
        assert d.namesInTier("main").equals(List.of("Ahri"));
        AramSurveyData round = fromJson(d.toJson());
        assert round.decidedCount() == 3 : "json round-trip";
        assert round.comparisons.get("Ahri|Zed").equals("Ahri");
        System.out.println("AramSurveyData self-check passed.");
    }
}
