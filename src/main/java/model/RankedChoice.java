package model;

/**
 * One entry in the "Top 5 Ranked Choices" view. This is NOT the user's static wishlist: it is the
 * top 5 of the champions actually OBTAINABLE in the current game (the champion you hold, the bench,
 * and your teammates' champions), ranked by the user's preference order (Priority list first, then
 * Survey). It explains the best pick available this game and how to get it.
 *
 * @param championId  champion id (name/icon resolved in the view)
 * @param rank        1-based position within the top-5 (best obtainable first)
 * @param fromPriority true if this champion came from the manual Priority list (shown with a star)
 * @param location    where the champion is: yours, on the bench (swap), or a teammate's (trade)
 * @param recommended true for the single best obtainable pick (rank 1) - what you should end up on
 */
public record RankedChoice(int championId, int rank, boolean fromPriority, Location location, boolean recommended) {
    public enum Location { MINE, BENCH, TEAMMATE }
}
