package model;

/**
 * Pure per-tick decision for the Troll Swap toggle: an infinite bench cycle that must bail out to
 * the champion you started on shortly before the game locks. No threads, no I/O here - just the
 * timing logic, so it can be unit-checked.
 */
public class TrollToggleDecider {

    public enum Action { STOP, SWAP_NEXT, WAIT }

    /**
     * @param ready           true if we are in an ARAM bench with a usable bench and a captured original
     * @param timeLeftMs      adjustedTimeLeftInPhase from the session timer (MAX_VALUE if unknown)
     * @param stopMs          bail-out threshold; at or below this, abort the cycle and return to original
     * @param msSinceLastSwap ms elapsed since the last bench swap
     * @param delayMs         configured delay between bench swaps
     */
    public static Action decide(boolean ready, int timeLeftMs, int stopMs,
                                long msSinceLastSwap, int delayMs) {
        if (!ready) {
            return Action.WAIT;
        }
        if (timeLeftMs <= stopMs) {
            return Action.STOP; // deadline wins over everything: cut the cycle, go to original now
        }
        if (msSinceLastSwap >= delayMs) {
            return Action.SWAP_NEXT;
        }
        return Action.WAIT;
    }

    /**
     * Choose the next champion to troll-swap to. The rotation is your original PLUS the current bench
     * (deduped, original first), so the champ you started on is cycled back to like any other and is
     * never abandoned on the bench for the whole loop. Scans forward from {@code startIdx} for the
     * first candidate that is currently on the bench and not the one you already hold.
     *
     * @return {championId, nextIdx}; {@code championId} is -1 if nothing is swappable this tick.
     */
    public static int[] nextTarget(java.util.List<Integer> bench, int original, int held, int startIdx) {
        java.util.List<Integer> pool = new java.util.ArrayList<>();
        if (original > 0) {
            pool.add(original);
        }
        for (int id : bench) {
            if (!pool.contains(id)) {
                pool.add(id);
            }
        }
        if (pool.isEmpty()) {
            return new int[]{-1, startIdx};
        }
        int n = pool.size();
        int base = ((startIdx % n) + n) % n; // normalize (startIdx can grow unbounded)
        for (int step = 0; step < n; step++) {
            int i = (base + step) % n;
            int cand = pool.get(i);
            if (cand != held && bench.contains(cand)) { // must be on the bench to swap to it
                return new int[]{cand, i + 1};
            }
        }
        return new int[]{-1, startIdx}; // everything is the held champ / off-bench; try again next tick
    }

    // Self-check: java -ea model.TrollToggleDecider
    public static void main(String[] args) {
        // Not ready (no bench / no original / not in bench) -> always wait, even under the deadline.
        assert decide(false, 500, 1000, 99999, 100) == Action.WAIT;
        // Ready, at/under the deadline -> stop.
        assert decide(true, 1000, 1000, 0, 100) == Action.STOP;
        assert decide(true, 999, 1000, 0, 100) == Action.STOP;
        // Ready, plenty of time, delay elapsed -> swap.
        assert decide(true, 5000, 1000, 100, 100) == Action.SWAP_NEXT;
        // Ready, plenty of time, delay not elapsed yet -> wait.
        assert decide(true, 5000, 1000, 50, 100) == Action.WAIT;
        // Deadline beats a long-overdue swap: cut the cycle regardless of how many champs remain.
        assert decide(true, 800, 1000, 99999, 100) == Action.STOP;
        // Unknown timer (MAX_VALUE) -> never a false stop; keep cycling when due.
        assert decide(true, Integer.MAX_VALUE, 1000, 100, 100) == Action.SWAP_NEXT;

        // nextTarget: rotation includes the original (original first).
        java.util.List<Integer> bench = java.util.List.of(20, 30, 40); // O=10 held, on bench: 20/30/40
        int[] p0 = nextTarget(bench, 10, 10, 0);   // holding original -> skip it, take first bench champ
        assert p0[0] == 20 && p0[1] == 2 : java.util.Arrays.toString(p0);
        // Once original (10) is back on the bench and we hold 20, the rotation returns to it.
        int[] p1 = nextTarget(java.util.List.of(10, 30, 40), 10, 20, 0); // pool=[10,30,40], idx0 -> 10
        assert p1[0] == 10 && p1[1] == 1 : java.util.Arrays.toString(p1);
        // Skips the champ currently held.
        int[] p2 = nextTarget(java.util.List.of(10, 30, 40), 10, 10, 0); // hold original -> skip to 30
        assert p2[0] == 30 : java.util.Arrays.toString(p2);
        // Nothing swappable (bench empty, holding original) -> -1.
        int[] p3 = nextTarget(java.util.List.of(), 10, 10, 0);
        assert p3[0] == -1 : java.util.Arrays.toString(p3);
        // Unbounded index normalizes.
        int[] p4 = nextTarget(java.util.List.of(20, 30, 40), 10, 10, 999999);
        assert p4[0] > 0 : java.util.Arrays.toString(p4);

        System.out.println("TrollToggleDecider self-check passed.");
    }
}
