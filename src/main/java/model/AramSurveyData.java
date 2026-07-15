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
    public List<String> flatOrder = new ArrayList<>();                   // authoritative swap order (editable)
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
        if (d.flatOrder == null) {
            d.flatOrder = new ArrayList<>();
        }
        return d;
    }

    private static final String[] SWAP_TIERS = {"main", "like", "fine"}; // never is excluded

    /** Derive the swap order from tiers + rankedOrder: main, then like, then fine (never excluded).
     *  Within a tier, use rankedOrder if present, else tier membership order. */
    public List<String> deriveFlat() {
        List<String> out = new ArrayList<>();
        for (String tier : SWAP_TIERS) {
            List<String> ranked = rankedOrder.get(tier);
            if (ranked != null && !ranked.isEmpty()) {
                out.addAll(ranked);
            } else {
                out.addAll(namesInTier(tier));
            }
        }
        return out;
    }

    /** Reconcile {@link #flatOrder} with the current tiers: keep the existing manual order, append
     *  any newly-decided non-never champions (in derived position), and drop champions that are now
     *  never/undecided. Called after the survey dialog runs and after manual edits. */
    public void syncFlatOrder() {
        List<String> derived = deriveFlat();
        java.util.Set<String> valid = new java.util.LinkedHashSet<>(derived);
        List<String> merged = new ArrayList<>();
        for (String n : flatOrder) {
            if (valid.contains(n) && !merged.contains(n)) {
                merged.add(n);
            }
        }
        for (String n : derived) {
            if (!merged.contains(n)) {
                merged.add(n);
            }
        }
        flatOrder = merged;
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

        // deriveFlat / syncFlatOrder
        AramSurveyData f = empty();
        f.tiers.put("Ahri", "main");
        f.tiers.put("Zed", "main");
        f.tiers.put("Lux", "like");
        f.tiers.put("Teemo", "never");
        f.rankedOrder.put("main", new ArrayList<>(List.of("Zed", "Ahri")));
        assert f.deriveFlat().equals(List.of("Zed", "Ahri", "Lux")) : "deriveFlat=" + f.deriveFlat();
        f.flatOrder = new ArrayList<>(List.of("Ahri", "Zed")); // manual order, Lux missing, no never
        f.syncFlatOrder();
        assert f.flatOrder.equals(List.of("Ahri", "Zed", "Lux")) : "sync=" + f.flatOrder;

        System.out.println("AramSurveyData self-check passed.");
    }
}
