package model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public class Session {
    private List<List<Actions>> actions;
    private boolean allowBattleBoost;
    private boolean allowDuplicatePicks;
    private boolean allowLockedEvents;
    private boolean allowRerolling;
    private boolean allowSkinSelection;
    private Bans bans;
    private int[] benchChampions;
    private boolean benchEnabled;
    private int boostableSkinCount;
    private int counter;
    private int gameId;
    private boolean hasSimultaneousBans;
    private boolean hasSimultaneousPicks;
    private boolean isCustomGame;
    private int localPlayerCellId;
    private int lockedEventIndex;
    private List<MyTeam> myTeam;
    private List<PickOrderSwaps> pickOrderSwaps;
    private int recoveryCounter;
    private int rerollsRemaining;
    private boolean skipChampionSelect;
    private List<TheirTeam> theirTeam;
    private Timer timer;

    public Session() {}

    public Session(List<List<Actions>> actions, boolean allowBattleBoost, boolean allowDuplicatePicks, boolean allowLockedEvents,
                   boolean allowRerolling, boolean allowSkinSelection, Bans bans, int[] benchChampions, boolean benchEnabled,
                   int boostableSkinCount, int counter, int gameId, boolean hasSimultaneousBans, boolean hasSimultaneousPicks,
                   boolean isCustomGame, int localPlayerCellId, int lockedEventIndex, List<MyTeam> myTeam, List<PickOrderSwaps> pickOrderSwaps,
                   int recoveryCounter, int rerollsRemaining, boolean skipChampionSelect, List<TheirTeam> theirTeam, Timer timer) {
        this.actions = actions;
        this.allowBattleBoost = allowBattleBoost;
        this.allowDuplicatePicks = allowDuplicatePicks;
        this.allowLockedEvents = allowLockedEvents;
        this.allowRerolling = allowRerolling;
        this.allowSkinSelection = allowSkinSelection;
        this.bans = bans;
        this.benchChampions = benchChampions;
        this.benchEnabled = benchEnabled;
        this.boostableSkinCount = boostableSkinCount;
        this.counter = counter;
        this.gameId = gameId;
        this.hasSimultaneousBans = hasSimultaneousBans;
        this.hasSimultaneousPicks = hasSimultaneousPicks;
        this.isCustomGame = isCustomGame;
        this.localPlayerCellId = localPlayerCellId;
        this.lockedEventIndex = lockedEventIndex;
        this.myTeam = myTeam;
        this.pickOrderSwaps = pickOrderSwaps;
        this.recoveryCounter = recoveryCounter;
        this.rerollsRemaining = rerollsRemaining;
        this.skipChampionSelect = skipChampionSelect;
        this.theirTeam = theirTeam;
        this.timer = timer;
    }

    public List<List<Actions>> getActions() {
        return actions;
    }

    public void setActions(List<List<Actions>> actions) {
        this.actions = actions;
    }

    public boolean isAllowBattleBoost() {
        return allowBattleBoost;
    }

    public void setAllowBattleBoost(boolean allowBattleBoost) {
        this.allowBattleBoost = allowBattleBoost;
    }

    public boolean isAllowDuplicatePicks() {
        return allowDuplicatePicks;
    }

    public void setAllowDuplicatePicks(boolean allowDuplicatePicks) {
        this.allowDuplicatePicks = allowDuplicatePicks;
    }

    public boolean isAllowLockedEvents() {
        return allowLockedEvents;
    }

    public void setAllowLockedEvents(boolean allowLockedEvents) {
        this.allowLockedEvents = allowLockedEvents;
    }

    public boolean isAllowRerolling() {
        return allowRerolling;
    }

    public void setAllowRerolling(boolean allowRerolling) {
        this.allowRerolling = allowRerolling;
    }

    public boolean isAllowSkinSelection() {
        return allowSkinSelection;
    }

    public void setAllowSkinSelection(boolean allowSkinSelection) {
        this.allowSkinSelection = allowSkinSelection;
    }

    public Bans getBans() {
        return bans;
    }

    public void setBans(Bans bans) {
        this.bans = bans;
    }

    public int[] getBenchChampions() {
        return benchChampions;
    }

    public void setBenchChampions(int[] benchChampions) {
        this.benchChampions = benchChampions;
    }

    public boolean isBenchEnabled() {
        return benchEnabled;
    }

    public void setBenchEnabled(boolean benchEnabled) {
        this.benchEnabled = benchEnabled;
    }

    public int getBoostableSkinCount() {
        return boostableSkinCount;
    }

    public void setBoostableSkinCount(int boostableSkinCount) {
        this.boostableSkinCount = boostableSkinCount;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public boolean isHasSimultaneousBans() {
        return hasSimultaneousBans;
    }

    public void setHasSimultaneousBans(boolean hasSimultaneousBans) {
        this.hasSimultaneousBans = hasSimultaneousBans;
    }

    public boolean isHasSimultaneousPicks() {
        return hasSimultaneousPicks;
    }

    public void setHasSimultaneousPicks(boolean hasSimultaneousPicks) {
        this.hasSimultaneousPicks = hasSimultaneousPicks;
    }

    public boolean isCustomGame() {
        return isCustomGame;
    }

    public void setCustomGame(boolean customGame) {
        isCustomGame = customGame;
    }

    public int getLocalPlayerCellId() {
        return localPlayerCellId;
    }

    public void setLocalPlayerCellId(int localPlayerCellId) {
        this.localPlayerCellId = localPlayerCellId;
    }

    public int getLockedEventIndex() {
        return lockedEventIndex;
    }

    public void setLockedEventIndex(int lockedEventIndex) {
        this.lockedEventIndex = lockedEventIndex;
    }

    public List<MyTeam> getMyTeam() {
        return myTeam;
    }

    public void setMyTeam(List<MyTeam> myTeam) {
        this.myTeam = myTeam;
    }

    public List<PickOrderSwaps> getPickOrderSwaps() {
        return pickOrderSwaps;
    }

    public void setPickOrderSwaps(List<PickOrderSwaps> pickOrderSwaps) {
        this.pickOrderSwaps = pickOrderSwaps;
    }

    public int getRecoveryCounter() {
        return recoveryCounter;
    }

    public void setRecoveryCounter(int recoveryCounter) {
        this.recoveryCounter = recoveryCounter;
    }

    public int getRerollsRemaining() {
        return rerollsRemaining;
    }

    public void setRerollsRemaining(int rerollsRemaining) {
        this.rerollsRemaining = rerollsRemaining;
    }

    public boolean isSkipChampionSelect() {
        return skipChampionSelect;
    }

    public void setSkipChampionSelect(boolean skipChampionSelect) {
        this.skipChampionSelect = skipChampionSelect;
    }

    public List<TheirTeam> getTheirTeam() {
        return theirTeam;
    }

    public void setTheirTeam(List<TheirTeam> theirTeam) {
        this.theirTeam = theirTeam;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public static Session parseFromJson(String eventData) {
        try {
            JsonObject rootObject = JsonParser.parseString(eventData).getAsJsonObject();
            JsonElement dataElement = rootObject.getAsJsonObject("OnJsonApiEvent_lol-champ-select_v1_session").get("data");
            return new Gson().fromJson(dataElement, Session.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "actions=" + actions +
                ", allowBattleBoost=" + allowBattleBoost +
                ", allowDuplicatePicks=" + allowDuplicatePicks +
                ", allowLockedEvents=" + allowLockedEvents +
                ", allowRerolling=" + allowRerolling +
                ", allowSkinSelection=" + allowSkinSelection +
                ", bans=" + bans +
                ", benchChampions=" + benchChampions +
                ", benchEnabled=" + benchEnabled +
                ", boostableSkinCount=" + boostableSkinCount +
                ", counter=" + counter +
                ", gameId=" + gameId +
                ", hasSimultaneousBans=" + hasSimultaneousBans +
                ", hasSimultaneousPicks=" + hasSimultaneousPicks +
                ", isCustomGame=" + isCustomGame +
                ", localPlayerCellId=" + localPlayerCellId +
                ", lockedEventIndex=" + lockedEventIndex +
                ", myTeam=" + myTeam +
                ", pickOrderSwaps=" + pickOrderSwaps +
                ", recoveryCounter=" + recoveryCounter +
                ", rerollsRemaining=" + rerollsRemaining +
                ", skipChampionSelect=" + skipChampionSelect +
                ", theirTeam=" + theirTeam +
                ", timer=" + timer +
                '}';
    }
}
