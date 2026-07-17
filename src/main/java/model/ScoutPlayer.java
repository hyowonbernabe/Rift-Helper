package model;

/**
 * Per-player scouting DTO surfaced by {@link ScoutReport}. Pure data holder (public mutable
 * fields, in the same spirit as {@code AramSeeder.ChampInfo}) so the view can render it directly
 * and {@code ScoutReport}'s pure matcher can fill in the "played recently" flags.
 *
 * <p>Every field degrades gracefully: strings are {@code ""}/null and numbers 0 when the client
 * did not expose the value (hidden names in champ select, bots / co-op accounts with no rank or
 * match history, champions not yet locked in, etc.). Nothing here throws.
 */
public class ScoutPlayer {

    /** Riot puuid. The stable key everything is matched on. May be "" if the client hid it. */
    public String puuid = "";

    /** Display / game name, or "" if unknown (hidden pick, not yet fetched). */
    public String summonerName = "";

    /** Summoner level, 0 if unknown. */
    public int level;

    /**
     * true = on the local player's team, false = enemy, null = undetermined (e.g. champ-select
     * enemies before name reveal, or an in-progress game where our own puuid could not be located).
     */
    public Boolean isAlly;

    /** Locked / in-game champion id (DDragon numeric key), or null if not yet picked / unknown. */
    public Integer championId;

    /** Formatted ranked-solo line (e.g. "Gold II 45 LP (120W/95L)"), "Unranked", or "" if unknown. */
    public String soloRank = "";

    /** Formatted ranked-flex line, "Unranked", or "" if unknown. */
    public String flexRank = "";

    /** Wins across the player's last ~5 games (any queue). 0 if none / unavailable. */
    public int recentWins;

    /** Losses across the player's last ~5 games (any queue). 0 if none / unavailable. */
    public int recentLosses;

    /** Highest-mastery champion id (DDragon numeric key), or null if unavailable. */
    public Integer topMasteryChampId;

    /** Points on {@link #topMasteryChampId}, 0 if unavailable. */
    public long topMasteryPoints;

    /** True if this player also appeared in the local player's last few games. */
    public boolean playedRecently;

    /** How many of the local player's last 3 games this player appeared in (0-3). */
    public int playedRecentlyCount;

    /**
     * Whether the overlap was against this player: TRUE if every recent overlap had them on the
     * enemy side, FALSE if every overlap had them as a teammate, null if unknown or mixed.
     */
    public Boolean playedRecentlyAsEnemy;

    public ScoutPlayer() {
    }

    public ScoutPlayer(String puuid) {
        this.puuid = puuid == null ? "" : puuid;
    }

    /** Convenience: locked champion display name via DDragon, or null if no champion is known. */
    public String championName() {
        return championId == null ? null : DDragonParser.getChampionName(championId);
    }

    /** Convenience: top-mastery champion display name via DDragon, or null if none. */
    public String topMasteryChampName() {
        return topMasteryChampId == null ? null : DDragonParser.getChampionName(topMasteryChampId);
    }

    /** True if either ranked queue resolved to something other than "" / "Unranked". */
    public boolean hasAnyRank() {
        return isRealRank(soloRank) || isRealRank(flexRank);
    }

    private static boolean isRealRank(String s) {
        return s != null && !s.isEmpty() && !"Unranked".equalsIgnoreCase(s);
    }

    @Override
    public String toString() {
        return "ScoutPlayer{" +
                "name='" + summonerName + '\'' +
                ", lvl=" + level +
                ", isAlly=" + isAlly +
                ", championId=" + championId +
                ", solo='" + soloRank + '\'' +
                ", flex='" + flexRank + '\'' +
                ", recent=" + recentWins + "W/" + recentLosses + "L" +
                ", topMasteryChampId=" + topMasteryChampId +
                ", topMasteryPoints=" + topMasteryPoints +
                ", playedRecently=" + playedRecently +
                " (x" + playedRecentlyCount + ", asEnemy=" + playedRecentlyAsEnemy + ")" +
                ", puuid='" + puuid + '\'' +
                '}';
    }
}
