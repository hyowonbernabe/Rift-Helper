package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamSelection {
    @JsonProperty("actorCellId")
    private int actorCellId;

    @JsonProperty("championId")
    private int championId;

    @JsonProperty("completed")
    private boolean completed;

    @JsonProperty("id")
    private int id;

    @JsonProperty("isAllyAction")
    private boolean isAllyAction;

    @JsonProperty("isInProgress")
    private boolean isInProgress;

    @JsonProperty("pickTurn")
    private int pickTurn;

    @JsonProperty("type")
    private String type;

    public TeamSelection() {}

    public TeamSelection(int actorCellId, int championId, boolean completed, int id, boolean isAllyAction, boolean isInProgress, int pickTurn, String type) {
        this.actorCellId = actorCellId;
        this.championId = championId;
        this.completed = completed;
        this.id = id;
        this.isAllyAction = isAllyAction;
        this.isInProgress = isInProgress;
        this.pickTurn = pickTurn;
        this.type = type;
    }

    public int getActorCellId() {
        return actorCellId;
    }

    public void setActorCellId(int actorCellId) {
        this.actorCellId = actorCellId;
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        this.championId = championId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAllyAction() {
        return isAllyAction;
    }

    public void setAllyAction(boolean allyAction) {
        isAllyAction = allyAction;
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(boolean inProgress) {
        isInProgress = inProgress;
    }

    public int getPickTurn() {
        return pickTurn;
    }

    public void setPickTurn(int pickTurn) {
        this.pickTurn = pickTurn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnoreProperties
    public static List<TeamSelection> parseFromJson(String eventData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(eventData);
            JsonNode actionsNode = rootNode
                    .path("OnJsonApiEvent_lol-champ-select_v1_session")
                    .path("data")
                    .path("actions");

            if (actionsNode.isArray() && actionsNode.size() > 0 && actionsNode.get(0).isArray()) {
                actionsNode = actionsNode.get(0);
            }

            return objectMapper.readValue(actionsNode.toString(), new TypeReference<List<TeamSelection>>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "actorCellId=" + actorCellId +
                ", championId=" + championId +
                ", completed=" + completed +
                ", id=" + id +
                ", isAllyAction=" + isAllyAction +
                ", isInProgress=" + isInProgress +
                ", pickTurn=" + pickTurn +
                ", type='" + type + '\'' +
                '}';
    }
}
