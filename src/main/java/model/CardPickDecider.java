package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pure decision for the NEW ARAM Champion Cards moment (patch 25.13, July 2026): at champ-select
 * start you are offered 2 champion cards (5% + 0.15%/owned-champ chance of a 3rd "blessed" card)
 * and have ~12s to pick ONE to hold; the unchosen cards fall to the shared team bench (which Rift
 * Helper's existing {@link AutoSwapPlanner} bench logic then handles as before).
 *
 * <p>Given the offered card champion ids, the user's ranking, and mastery, this returns which card
 * to pick. No network, no threads, no state - inputs in, an id (or {@link #NO_PICK}) out - so it is
 * deterministic, idempotent, and unit-checkable via {@link #main(String[])} under {@code -ea}.
 *
 * <p><b>Ranking rule</b> (matches {@link AutoSwapPlanner}'s engine exactly): the effective order is
 * the Priority list first, then the Survey list, with NO dedup so the first occurrence of an id
 * wins; lower index = higher priority; a champ in neither list ranks worst. Among the offered
 * cards, pick the one with the best (lowest) rank. If none of the offered cards is in either list,
 * fall back to the offered card with the highest mastery points. If mastery is all-zero/unknown
 * too, return {@link #NO_PICK} - do not force a pick; leave the choice to the user.
 *
 * <p><b>LCU endpoint (UNCONFIRMED - controller must verify with Rift Explorer against a live ARAM
 * champ-select, then wire it up).</b> The card-offer shape is new and undocumented as of this
 * writing. Candidates for where the offered cards appear:
 * <ul>
 *   <li>{@code GET /lol-champ-select/v1/session} with a NEW field - a {@code cards} array, a
 *       (re-purposed) {@code benchChampions} array, or a per-cell offered-champions array on the
 *       local player's {@code myTeam} cell;</li>
 *   <li>possibly a dedicated {@code GET /lol-champ-select/v1/...cards} sub-resource.</li>
 * </ul>
 * Candidates for how the pick is submitted:
 * <ul>
 *   <li>a bench-style swap: {@code POST /lol-champ-select/v1/session/bench/swap/{championId}}
 *       (the same call the existing bench logic uses); or</li>
 *   <li>a champ-select action PATCH ({@code PATCH /lol-champ-select/v1/session/actions/{id}} with
 *       the chosen {@code championId}).</li>
 * </ul>
 * On the first ARAM champ-select entry the controller should log the raw session JSON to stdout
 * (same pattern {@link Honor#honorFriends()} uses to reveal its unconfirmed ballot shape) so the
 * true field/endpoint names surface on the first live run. This class stays pure regardless of
 * which endpoint wins.
 */
public class CardPickDecider {

    /** Returned when no card should be auto-picked (nothing ranked, no mastery signal). */
    public static final int NO_PICK = -1;

    /**
     * Decide which offered champion card to pick.
     *
     * @param offeredCardIds champion ids of the offered cards (length 2 or 3; order irrelevant)
     * @param priorityIds    the user's ordered priority champion-id list (best first; 0 entries skipped)
     * @param surveyIds      the user's ordered survey champion-id list (best first)
     * @param masteryPoints  championId -&gt; mastery points, used only for the fallback (may be null)
     * @return the champion id of the card to pick, or {@link #NO_PICK} to leave it to the user
     */
    public static int decidePick(List<Integer> offeredCardIds, int[] priorityIds,
                                 List<Integer> surveyIds, Map<Integer, Long> masteryPoints) {
        if (offeredCardIds == null || offeredCardIds.isEmpty()) {
            return NO_PICK;
        }
        List<Integer> order = buildOrder(priorityIds, surveyIds);

        // 1) Best (lowest) rank in the combined Priority+Survey order wins. Strict '<' keeps the
        //    first-encountered offered card on ties, so the result is deterministic.
        int bestId = NO_PICK;
        int bestRank = Integer.MAX_VALUE;
        for (Integer offered : offeredCardIds) {
            if (offered == null || offered <= 0) {
                continue;
            }
            int rank = rankOf(offered, order);
            if (rank < bestRank) {
                bestRank = rank;
                bestId = offered;
            }
        }
        if (bestRank != Integer.MAX_VALUE) {
            return bestId;
        }

        // 2) None of the offered cards is ranked: fall back to the highest-mastery offered card.
        int masteryId = NO_PICK;
        long bestMastery = 0L;
        for (Integer offered : offeredCardIds) {
            if (offered == null || offered <= 0) {
                continue;
            }
            long m = masteryOf(offered, masteryPoints);
            if (m > bestMastery) {
                bestMastery = m;
                masteryId = offered;
            }
        }
        // 3) No mastery signal either -> leave the pick to the user.
        return masteryId;
    }

    /** Effective order: Priority ids (skipping 0) first, then Survey ids, NO dedup. */
    static List<Integer> buildOrder(int[] priorityIds, List<Integer> surveyIds) {
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

    /** Rank of a champion in the effective order (first occurrence), or MAX_VALUE if absent. */
    static int rankOf(int championId, List<Integer> order) {
        int idx = order.indexOf(championId);
        return idx < 0 ? Integer.MAX_VALUE : idx;
    }

    private static long masteryOf(int championId, Map<Integer, Long> masteryPoints) {
        if (masteryPoints == null) {
            return 0L;
        }
        Long m = masteryPoints.get(championId);
        return m == null ? 0L : m;
    }

    // Self-check: java -ea model.CardPickDecider
    public static void main(String[] args) {
        int[] priority = {10, 20};
        List<Integer> survey = List.of(30, 40);

        // (a) picks the higher-priority of two ranked cards (10 @ rank 0 beats 20 @ rank 1),
        //     independent of the offered order.
        assert decidePick(List.of(20, 10), priority, survey, null) == 10 : "priority winner";
        assert decidePick(List.of(10, 20), priority, survey, null) == 10 : "priority winner (order-independent)";

        // (b) survey used when priority misses: priority has neither, survey ranks 30 above 40.
        assert decidePick(List.of(40, 30), new int[]{99}, survey, null) == 30 : "survey used when priority misses";

        // Priority still outranks survey when both appear (10 @ rank 0 vs 30 @ rank 2).
        assert decidePick(List.of(30, 10), priority, survey, null) == 10 : "priority outranks survey";

        // (c) mastery fallback when neither list has any offered card.
        Map<Integer, Long> mastery = new java.util.HashMap<>();
        mastery.put(50, 5000L);
        mastery.put(60, 90000L);
        assert decidePick(List.of(50, 60), priority, survey, mastery) == 60 : "mastery fallback picks highest";

        // (d) sentinel when nothing ranked and no mastery (null map, and all-zero map).
        assert decidePick(List.of(50, 60), new int[]{99}, List.of(88), null) == NO_PICK : "no rank, null mastery -> NO_PICK";
        Map<Integer, Long> zeroMastery = new java.util.HashMap<>();
        zeroMastery.put(50, 0L);
        zeroMastery.put(60, 0L);
        assert decidePick(List.of(50, 60), new int[]{99}, List.of(88), zeroMastery) == NO_PICK : "no rank, zero mastery -> NO_PICK";

        // (e) 3-card blessed offer: best rank across all three is chosen.
        assert decidePick(List.of(70, 40, 10), priority, survey, null) == 10 : "3-card: priority winner";
        assert decidePick(List.of(70, 40, 30), new int[]{99}, survey, null) == 30 : "3-card: survey winner";
        Map<Integer, Long> m3 = new java.util.HashMap<>();
        m3.put(70, 111L);
        m3.put(71, 222L);
        m3.put(72, 100L);
        assert decidePick(List.of(70, 71, 72), new int[]{99}, List.of(88), m3) == 71 : "3-card: mastery fallback";

        // Edge cases: empty / null offer -> NO_PICK; non-positive ids ignored.
        assert decidePick(List.of(), priority, survey, null) == NO_PICK : "empty offer -> NO_PICK";
        assert decidePick(null, priority, survey, null) == NO_PICK : "null offer -> NO_PICK";
        assert decidePick(java.util.Arrays.asList(0, 10), priority, survey, null) == 10 : "non-positive id ignored";

        // buildOrder / rankOf: Priority first, then Survey, no dedup; first occurrence wins.
        List<Integer> order = buildOrder(new int[]{10, 0, 20}, List.of(20, 30));
        assert order.equals(List.of(10, 20, 20, 30)) : "order=" + order;
        assert rankOf(20, order) == 1 : "first occurrence wins";
        assert rankOf(999, order) == Integer.MAX_VALUE : "absent -> worst rank";

        // Deterministic / idempotent: same inputs -> same output across calls.
        assert decidePick(List.of(20, 10), priority, survey, null)
                == decidePick(List.of(20, 10), priority, survey, null) : "idempotent";

        System.out.println("CardPickDecider self-check passed.");
    }
}
