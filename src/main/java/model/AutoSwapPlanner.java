package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the effective auto-swap order (Priority list first, then Survey list, NO dedup) and picks
 * the best available bench champion, only ever improving on the best rank reached so far.
 */
public class AutoSwapPlanner {

    public static List<Integer> buildOrder(int[] priorityIds, List<Integer> surveyIds) {
        List<Integer> order = new ArrayList<>();
        if (priorityIds != null) {
            for (int id : priorityIds) {
                if (id > 0) {
                    order.add(id);
                }
            }
        }
        if (surveyIds != null) {
            order.addAll(surveyIds);
        }
        return order;
    }

    /**
     * @param order              effective ordered ids (best first)
     * @param benchIds           champion ids currently on the bench
     * @param currentRankReached best index already swapped to this champ select (start at MAX_VALUE)
     * @param outRank            length-1 array; set to the new best rank when a target is found
     * @return champion id to swap to, or -1 if nothing strictly better is available
     */
    public static int pickBenchTarget(List<Integer> order, List<Integer> benchIds,
                                       int currentRankReached, int[] outRank) {
        for (int rank = 0; rank < order.size() && rank < currentRankReached; rank++) {
            int id = order.get(rank);
            if (benchIds.contains(id)) {
                if (outRank != null) {
                    outRank[0] = rank;
                }
                return id;
            }
        }
        return -1;
    }

    // Self-check: java -ea model.AutoSwapPlanner
    public static void main(String[] args) {
        int[] priority = {10, 0, 20};                 // 0 skipped
        List<Integer> survey = List.of(20, 30, 40);   // 20 duplicated on purpose (no dedup)
        List<Integer> order = buildOrder(priority, survey);
        assert order.equals(List.of(10, 20, 20, 30, 40)) : "order=" + order;

        int[] out = {-1};
        int t = pickBenchTarget(order, List.of(40, 30), Integer.MAX_VALUE, out);
        assert t == 30 && out[0] == 3 : "t=" + t + " rank=" + out[0];

        int t2 = pickBenchTarget(order, List.of(40), 3, out);
        assert t2 == -1 : "no strict improvement";

        int t3 = pickBenchTarget(order, List.of(10, 40), 3, out);
        assert t3 == 10 && out[0] == 0;

        System.out.println("AutoSwapPlanner self-check passed.");
    }
}
