package model;

/**
 * One entry in the "Top 5 Ranked Choices" view: a champion in the effective auto-swap order
 * (Priority list first, then Survey), with why-it-matters context for the current champ select.
 *
 * @param championId champion id (name/icon resolved in the view via DDragonParser/ChampionIcons)
 * @param rank       1-based position in the effective order
 * @param fromPriority true if this entry came from the manual Priority list (shown with a star)
 * @param onBench    true if the champion is currently on the bench (swappable right now)
 * @param swapTarget true if this is the champion the app swapped to / would swap to (best available)
 */
public record RankedChoice(int championId, int rank, boolean fromPriority, boolean onBench, boolean swapTarget) {
}
