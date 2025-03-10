package model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Matchmaking {
    private long estimatedQueueTime;
    private boolean isCurrentlyInQueue;
    private String lobbyId;
    private int queueId;
    private ReadyCheck readyCheck;
    private String searchState;
    private double timeInQueue;

    public Matchmaking() {}

    public Matchmaking(long estimatedQueueTime, boolean isCurrentlyInQueue, String lobbyId, int queueId, ReadyCheck readyCheck, String searchState, double isTimeInQueue) {
        this.estimatedQueueTime = estimatedQueueTime;
        this.isCurrentlyInQueue = isCurrentlyInQueue;
        this.lobbyId = lobbyId;
        this.queueId = queueId;
        this.readyCheck = readyCheck;
        this.searchState = searchState;
        this.timeInQueue = isTimeInQueue;
    }

    public long getEstimatedQueueTime() {
        return estimatedQueueTime;
    }

    public void setEstimatedQueueTime(long estimatedQueueTime) {
        this.estimatedQueueTime = estimatedQueueTime;
    }

    public boolean isCurrentlyInQueue() {
        return isCurrentlyInQueue;
    }

    public void setCurrentlyInQueue(boolean currentlyInQueue) {
        isCurrentlyInQueue = currentlyInQueue;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public ReadyCheck getReadyCheck() {
        return readyCheck;
    }

    public void setReadyCheck(ReadyCheck readyCheck) {
        this.readyCheck = readyCheck;
    }

    public String getSearchState() {
        return searchState;
    }

    public void setSearchState(String searchState) {
        this.searchState = searchState;
    }

    public double isTimeInQueue() {
        return timeInQueue;
    }

    public void setTimeInQueue(double timeInQueue) {
        this.timeInQueue = timeInQueue;
    }

    public static Matchmaking parseFromJson(String eventData) {
        try {
            JsonObject rootObject = JsonParser.parseString(eventData).getAsJsonObject();
            JsonElement dataElement = rootObject.getAsJsonObject("OnJsonApiEvent_lol-matchmaking_v1_search").get("data");
            return new Gson().fromJson(dataElement, Matchmaking.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "estimatedQueueTime=" + estimatedQueueTime +
                ", isCurrentlyInQueue=" + isCurrentlyInQueue +
                ", lobbyId='" + lobbyId + '\'' +
                ", queueId=" + queueId +
                ", readyCheck=" + readyCheck +
                ", searchState='" + searchState + '\'' +
                ", timeInQueue=" + timeInQueue +
                '}';
    }
}
