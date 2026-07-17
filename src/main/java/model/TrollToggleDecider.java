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
    public static java.util.List<Integer> buildRotation(int original, java.util.List<Integer> bench) {
        java.util.List<Integer> r = new java.util.ArrayList<>();
        if (original > 0) {
            r.add(original);
        }
        if (bench != null) {
            for (int id : bench) {
                if (id > 0 && !r.contains(id)) {
                    r.add(id);
                }
            }
        }
        return r;
    }

    /**
     * Next champion to swap to, from the FIXED {@code rotation} captured at the start of a run: scan
     * forward from {@code startIdx} for the first entry that is not the champ you currently hold (you
     * cannot swap to yourself). In the closed original+bench pool every non-held entry is on the bench,
     * so walking the fixed rotation deterministically visits every champion - no slot is skipped. This
     * replaces the old approach of indexing the live bench, whose slots reshuffle on each swap and
     * caused champs to be missed. Returns {championId, nextIdx}; id -1 if there is nothing to swap to.
     */
    public static int[] nextTarget(java.util.List<Integer> rotation, int held, int startIdx) {
        if (rotation == null || rotation.isEmpty()) {
            return new int[]{-1, startIdx};
        }
        int n = rotation.size();
        int base = ((startIdx % n) + n) % n; // normalize (startIdx grows unbounded)
        for (int step = 0; step < n; step++) {
            int i = (base + step) % n;
            int cand = rotation.get(i);
            if (cand > 0 && cand != held) {
                return new int[]{cand, i + 1};
            }
        }
        return new int[]{-1, startIdx}; // rotation is only the held champ
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

        // buildRotation: original first, then bench, deduped.
        java.util.List<Integer> rot = buildRotation(10, java.util.List.of(20, 30, 40));
        assert rot.equals(java.util.List.of(10, 20, 30, 40)) : rot.toString();
        assert buildRotation(10, java.util.List.of(10, 20)).equals(java.util.List.of(10, 20)) : "dedup original";

        // nextTarget over the FIXED rotation: a full lap visits every champ, no slot skipped.
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        int idx = 0, held = 10; // start holding the original
        for (int k = 0; k < 4; k++) {
            int[] pk = nextTarget(rot, held, idx);
            assert pk[0] > 0 : java.util.Arrays.toString(pk);
            seen.add(pk[0]);
            held = pk[0];   // we now hold what we swapped to
            idx = pk[1];
        }
        assert seen.containsAll(java.util.List.of(10, 20, 30, 40)) : "every champ visited: " + seen;
        assert seen.contains(20) : "2nd slot must be reached: " + seen; // the reported skip
        // Rotation of only the held champ -> nothing to swap to.
        assert nextTarget(java.util.List.of(10), 10, 0)[0] == -1 : "only held -> -1";
        // Unbounded index normalizes.
        assert nextTarget(rot, 999, 999999)[0] > 0 : "index normalizes";

        System.out.println("TrollToggleDecider self-check passed.");
    }
}
