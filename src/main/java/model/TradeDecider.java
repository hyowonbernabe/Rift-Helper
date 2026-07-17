package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pure per-tick decision logic for ARAM champion-select TRADES ("Smart" behaviour). No threads,
 * no I/O, no LCU networking here - the controller parses the live session, calls {@link #decide},
 * and performs the REST calls for the returned actions. Kept pure so it can be unit-checked
 * (see {@link #main}).
 *
 * <p><b>What a trade is (2026 ARAM).</b> The champ-select session carries a {@code trades} array.
 * Each entry references the OTHER cell involved and (as observed) has:
 * <ul>
 *   <li>{@code id}     - the trade id used in the REST calls below,</li>
 *   <li>{@code cellId} - the other summoner's cell,</li>
 *   <li>{@code state}  - one of the {@code STATE_*} strings below.</li>
 * </ul>
 * The trade JSON does NOT itself carry a champion id, so the controller must join
 * {@code trade.cellId} to {@code myTeam[].cellId -> championId} and hand us that resolved champion
 * id as {@link Trade#championId()} (the champ that completing this trade would put on OUR cell).
 * That join is the one input here that is not obvious from the raw trade JSON.
 *
 * <p><b>States (VERIFY-LIVE spellings).</b> AVAILABLE, BUSY, SENT, RECEIVED, DECLINED, CANCELLED,
 * INVALID. We only ever act on AVAILABLE (candidate to request) and RECEIVED (accept/decline);
 * every other state - including any string we do not recognise - is a no-op. Because completing a
 * request flips that trade to SENT (and its peers to BUSY), a champ we requested this tick is no
 * longer AVAILABLE next tick, so we never re-request it and never spam.
 *
 * <p><b>LCU actions the controller performs from our decisions (VERIFY-LIVE).</b>
 * <pre>
 *   REQUEST -> POST /lol-champ-select/v1/session/trades/{id}/request
 *   ACCEPT  -> POST /lol-champ-select/v1/session/trades/{id}/accept
 *   DECLINE -> POST /lol-champ-select/v1/session/trades/{id}/decline
 * </pre>
 * A 204 is success (same convention as the bench-swap call).
 *
 * <p><b>Ranking.</b> Identical convention to {@link AutoSwapPlanner}: the effective order is the
 * Priority list first, then the Survey list (no dedup); lower index = higher priority; a champ not
 * in the list ranks worst (as if index {@link Integer#MAX_VALUE}). A trade is only worth accepting
 * or requesting when it yields a STRICTLY higher-ranked (lower index) champ than the one we hold -
 * never a downgrade or a lateral. We also never request a champ that is not in our lists at all.
 *
 * <p><b>Scope.</b> Trades only. "Prefer bench over trade" is handled upstream: the controller
 * checks/grabs the bench first and passes the current {@code benchChampionIds} here so we never
 * REQUEST a champ that is already grabbable from the bench.
 */
public class TradeDecider {

    public static final String STATE_AVAILABLE = "AVAILABLE";
    public static final String STATE_BUSY      = "BUSY";
    public static final String STATE_SENT      = "SENT";
    public static final String STATE_RECEIVED  = "RECEIVED";
    public static final String STATE_DECLINED  = "DECLINED";
    public static final String STATE_CANCELLED = "CANCELLED";
    public static final String STATE_INVALID   = "INVALID";

    public enum Action { REQUEST, ACCEPT, DECLINE }

    /** A trade from the session, with its target champion resolved by the controller (see class doc). */
    public record Trade(int id, String state, int championId) {}

    /** One action to take this tick. */
    public record Decision(int tradeId, Action action) {}

    /**
     * Decide every trade action to take this tick.
     *
     * @param myChampionId     champion id currently on our cell
     * @param priorityIds      Priority-list ids (0s ignored), best first; may be null
     * @param surveyIds        Survey-list ids, appended after Priority (no dedup); may be null
     * @param trades           current trades from the session; may be null/empty
     * @param benchChampionIds champ ids already grabbable from the bench - never REQUEST one of
     *                         these (the controller grabs it from the bench instead); may be null
     * @return actions to take this tick: an ACCEPT/DECLINE for every RECEIVED trade, plus at most
     *         one REQUEST for the single best AVAILABLE upgrade. Empty when nothing applies.
     *         Deterministic: identical inputs always yield identical decisions.
     */
    public static List<Decision> decide(int myChampionId,
                                        int[] priorityIds,
                                        List<Integer> surveyIds,
                                        List<Trade> trades,
                                        Set<Integer> benchChampionIds) {
        List<Decision> out = new ArrayList<>();
        if (trades == null || trades.isEmpty()) {
            return out;
        }
        List<Integer> order = AutoSwapPlanner.buildOrder(priorityIds, surveyIds);
        int myRank = rankOf(order, myChampionId);

        // Pass 1: incoming trades. Accept a strict upgrade; decline everything else received.
        for (Trade t : trades) {
            if (t == null || !STATE_RECEIVED.equalsIgnoreCase(t.state())) {
                continue;
            }
            if (rankOf(order, t.championId()) < myRank) {
                out.add(new Decision(t.id(), Action.ACCEPT));   // strictly higher-ranked champ
            } else {
                out.add(new Decision(t.id(), Action.DECLINE));  // downgrade or lateral
            }
        }

        // Pass 2: outgoing. Among AVAILABLE trades whose champ is a strict upgrade and is NOT
        // already on the bench, request the single best (lowest rank; ties -> lowest trade id).
        int bestRank = myRank;      // must strictly beat what we currently hold
        int bestTradeId = -1;
        for (Trade t : trades) {
            if (t == null || !STATE_AVAILABLE.equalsIgnoreCase(t.state())) {
                continue;
            }
            if (benchChampionIds != null && benchChampionIds.contains(t.championId())) {
                continue;           // controller grabs this from the bench instead
            }
            int r = rankOf(order, t.championId());
            if (r < bestRank || (r == bestRank && bestTradeId != -1 && t.id() < bestTradeId)) {
                bestRank = r;
                bestTradeId = t.id();
            }
        }
        if (bestTradeId != -1) {
            out.add(new Decision(bestTradeId, Action.REQUEST));
        }
        return out;
    }

    /** Rank of a champion in the effective order: its first index, or MAX_VALUE if absent/invalid. */
    private static int rankOf(List<Integer> order, int championId) {
        if (championId <= 0) {
            return Integer.MAX_VALUE;
        }
        int i = order.indexOf(championId);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    // Self-check: java -ea model.TradeDecider
    public static void main(String[] args) {
        int[] prio = {10, 20, 30};
        List<Integer> survey = List.of(40, 50);
        // Effective order [10,20,30,40,50] -> ranks: 10=0, 20=1, 30=2, 40=3, 50=4; anything else = worst.

        // accept-on-upgrade: holding 30 (rank 2), offered 10 (rank 0) -> ACCEPT.
        assert decide(30, prio, survey, List.of(new Trade(1, STATE_RECEIVED, 10)), null)
                .equals(List.of(new Decision(1, Action.ACCEPT))) : "accept upgrade";
        // accept-on-upgrade from an UNLISTED held champ (worst rank) to a listed one.
        assert decide(777, prio, survey, List.of(new Trade(1, STATE_RECEIVED, 40)), null)
                .equals(List.of(new Decision(1, Action.ACCEPT))) : "accept upgrade from unlisted";

        // decline-on-downgrade: holding 10 (rank 0), offered 50 (rank 4) -> DECLINE.
        assert decide(10, prio, survey, List.of(new Trade(1, STATE_RECEIVED, 50)), null)
                .equals(List.of(new Decision(1, Action.DECLINE))) : "decline downgrade";
        // decline-on-downgrade to an UNLISTED champ while holding a listed one.
        assert decide(10, prio, survey, List.of(new Trade(1, STATE_RECEIVED, 999)), null)
                .equals(List.of(new Decision(1, Action.DECLINE))) : "decline downgrade to unlisted";

        // decline-on-lateral: both champs unlisted (equal worst rank) -> DECLINE, not accept.
        assert decide(777, prio, survey, List.of(new Trade(1, STATE_RECEIVED, 888)), null)
                .equals(List.of(new Decision(1, Action.DECLINE))) : "decline lateral";

        // request-picks-best-available: three upgrades offered -> request the single best (10, id 2).
        assert decide(50, prio, survey,
                List.of(new Trade(1, STATE_AVAILABLE, 20),
                        new Trade(2, STATE_AVAILABLE, 10),
                        new Trade(3, STATE_AVAILABLE, 40)), null)
                .equals(List.of(new Decision(2, Action.REQUEST))) : "request best available";

        // no-request-when-champ-on-bench: the only upgrade (10) is already on the bench -> nothing.
        assert decide(50, prio, survey, List.of(new Trade(1, STATE_AVAILABLE, 10)), Set.of(10))
                .isEmpty() : "no request when champ on bench";
        // ... but the next-best not-on-bench upgrade is still requested (20, id 2).
        assert decide(50, prio, survey,
                List.of(new Trade(1, STATE_AVAILABLE, 10), new Trade(2, STATE_AVAILABLE, 20)), Set.of(10))
                .equals(List.of(new Decision(2, Action.REQUEST))) : "skip bench champ, request next best";

        // never request a champ that isn't in our lists at all.
        assert decide(50, prio, survey, List.of(new Trade(1, STATE_AVAILABLE, 999)), null)
                .isEmpty() : "no request for unlisted champ";

        // no-op on SENT / BUSY (and any unrecognised state).
        assert decide(50, prio, survey,
                List.of(new Trade(1, STATE_SENT, 10), new Trade(2, STATE_BUSY, 20)), null)
                .isEmpty() : "no-op on SENT/BUSY";
        assert decide(50, prio, survey, List.of(new Trade(1, "WEIRD_NEW_STATE", 10)), null)
                .isEmpty() : "no-op on unknown state";

        // empty / null trades -> empty decisions.
        assert decide(30, prio, survey, null, null).isEmpty() : "null trades -> empty";
        assert decide(30, prio, survey, List.of(), null).isEmpty() : "empty trades -> empty";

        // determinism / tie-break: two AVAILABLE trades give the same champ (rank 0) -> lowest id.
        assert decide(50, prio, survey,
                List.of(new Trade(5, STATE_AVAILABLE, 10), new Trade(2, STATE_AVAILABLE, 10)), null)
                .equals(List.of(new Decision(2, Action.REQUEST))) : "tie-break lowest trade id";

        // combined tick: accept one, decline one, and request the single best available.
        assert decide(30, prio, survey,
                List.of(new Trade(1, STATE_RECEIVED, 10),   // rank 0 < 2 -> ACCEPT
                        new Trade(2, STATE_RECEIVED, 50),   // rank 4 > 2 -> DECLINE
                        new Trade(3, STATE_AVAILABLE, 20),  // rank 1 < 2 -> best request
                        new Trade(4, STATE_AVAILABLE, 50)), null) // rank 4, not an upgrade -> ignored
                .equals(List.of(new Decision(1, Action.ACCEPT),
                                new Decision(2, Action.DECLINE),
                                new Decision(3, Action.REQUEST))) : "combined accept/decline/request";

        System.out.println("TradeDecider self-check passed.");
    }
}
