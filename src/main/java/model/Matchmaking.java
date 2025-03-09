package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Matchmaking {
    @JsonProperty("estimatedQueueTime")
    private long estimatedQueueTime;

    @JsonProperty("isCurrentlyInQueue")
    private boolean isCurrentlyInQueue;

    @JsonProperty("lobbyId")
    private String lobbyId;

    @JsonProperty("queueId")
    private int queueId;

    @JsonProperty("readyCheck")
    private ReadyCheck readyCheck;

    @JsonProperty("searchState")
    private String searchState;

    @JsonProperty("timeInQueue")
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

    @JsonIgnoreProperties
    public static Matchmaking parseFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(eventData);
            JsonNode dataNode = rootNode
                    .path("OnJsonApiEvent_lol-matchmaking_v1_search")
                    .path("data");

            return objectMapper.treeToValue(dataNode, Matchmaking.class);
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
