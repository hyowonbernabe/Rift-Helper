package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Turns pairwise "A or B?" answers into a full order via merge sort, caching every answer so a
 * re-run reuses prior answers. That cache is what makes the survey resumable mid-sort: a resumed
 * run replays cached pairs instantly and only asks the human for pairs not seen yet.
 */
public class SurveyRanker {
    /** Returns {@code <0} if a should come first, {@code >0} if b first, {@code 0} for a tie. */
    public interface Asker {
        int ask(String a, String b);
    }

    public static List<String> rank(List<String> items, Map<String, String> cache, Asker asker) {
        if (items.size() <= 1) {
            return new ArrayList<>(items);
        }
        int mid = items.size() / 2;
        List<String> left = rank(items.subList(0, mid), cache, asker);
        List<String> right = rank(items.subList(mid, items.size()), cache, asker);
        List<String> out = new ArrayList<>(items.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), cache, asker) <= 0) {
                out.add(left.get(i++));
            } else {
                out.add(right.get(j++));
            }
        }
        while (i < left.size()) {
            out.add(left.get(i++));
        }
        while (j < right.size()) {
            out.add(right.get(j++));
        }
        return out;
    }

    private static int compare(String a, String b, Map<String, String> cache, Asker asker) {
        String key = AramSurveyData.pairKey(a, b);
        String cached = cache.get(key);
        if (cached == null) {
            int r = asker.ask(a, b);
            cached = (r == 0) ? "TIE" : (r < 0 ? a : b);
            cache.put(key, cached);
        }
        if (cached.equals("TIE")) {
            return 0;
        }
        return cached.equals(a) ? -1 : 1;
    }

    // Self-check: java -ea model.SurveyRanker
    public static void main(String[] args) {
        // Ground-truth order by a hidden score; asker returns comparison and counts calls.
        Map<String, Integer> score = Map.of("A", 5, "B", 4, "C", 3, "D", 2, "E", 1);
        int[] calls = {0};
        Map<String, String> cache = new java.util.HashMap<>();
        SurveyRanker.Asker asker = (a, b) -> {
            calls[0]++;
            return Integer.compare(score.get(b), score.get(a));
        };

        List<String> input = List.of("C", "A", "E", "B", "D");
        List<String> ranked = rank(new ArrayList<>(input), cache, asker);
        assert ranked.equals(List.of("A", "B", "C", "D", "E")) : "ranked=" + ranked;

        // Resume property: re-running the SAME input order replays the identical pair sequence, all
        // now cached, so it asks zero new questions. (A different input order may need new pairs -
        // that is expected and fine; resume always uses the same stable tier order.)
        int before = calls[0];
        List<String> again = rank(new ArrayList<>(input), cache, asker);
        assert again.equals(List.of("A", "B", "C", "D", "E"));
        assert calls[0] == before : "resume (same input) must ask nothing new (extra=" + (calls[0] - before) + ")";

        // Tie handling: equal pair stays in input order.
        Map<String, String> c2 = new java.util.HashMap<>();
        List<String> t = rank(new ArrayList<>(List.of("X", "Y")), c2, (a, b) -> 0);
        assert t.equals(List.of("X", "Y")) : "tie keeps input order";

        System.out.println("SurveyRanker self-check passed.");
    }
}
