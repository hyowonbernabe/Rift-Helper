package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSelection {
    @JsonProperty("assignedPosition")
    private String assignedPosition;

    @JsonProperty("cellId")
    private int cellId;

    @JsonProperty("championId")
    private int championId;

    @JsonProperty("spell1Id")
    private int spell1Id;

    @JsonProperty("spell2Id")
    private int spell2Id;

    @JsonProperty("summonerId")
    private long summonerId;

    @JsonProperty("team")
    private int team;

    public UserSelection() {}

    public UserSelection(String assignedPosition, int cellId, int championId, int spell1Id, int spell2Id, long summonerId, int team) {
        this.assignedPosition = assignedPosition;
        this.cellId = cellId;
        this.championId = championId;
        this.spell1Id = spell1Id;
        this.spell2Id = spell2Id;
        this.summonerId = summonerId;
        this.team = team;
    }

    public int getAssignedPosition() {
        if (assignedPosition == null || assignedPosition.isEmpty()) {
            return -1;
        }

        switch (assignedPosition.toLowerCase()) {
            case "top":
                return 0;
            case "jungle":
                return 1;
            case "middle":
                return 2;
            case "bottom":
                return 3;
            case "support":
                return 4;
            default:
                try {
                    return Integer.parseInt(assignedPosition);
                } catch (NumberFormatException e) {
                    return -1;
                }
        }
    }

    public void setAssignedPosition(String assignedPosition) {
        this.assignedPosition = assignedPosition;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        this.championId = championId;
    }

    public int getSpell1Id() {
        return spell1Id;
    }

    public void setSpell1Id(int spell1Id) {
        this.spell1Id = spell1Id;
    }

    public int getSpell2Id() {
        return spell2Id;
    }

    public void setSpell2Id(int spell2Id) {
        this.spell2Id = spell2Id;
    }

    public long getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(long summonerId) {
        this.summonerId = summonerId;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    @JsonIgnoreProperties
    public static UserSelection parseFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventData, UserSelection.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnoreProperties
    public static int parseFromJsonLocalCellId(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(eventData);
            JsonNode localCellIdNode = rootNode
                    .path("OnJsonApiEvent_lol-champ-select_v1_session")
                    .path("data")
                    .path("localPlayerCellId");

            return localCellIdNode.asInt(-1);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "assignedPosition='" + assignedPosition + '\'' +
                ", cellId=" + cellId +
                ", championId=" + championId +
                ", spell1Id=" + spell1Id +
                ", spell2Id=" + spell2Id +
                ", summonerId=" + summonerId +
                ", team=" + team +
                '}';
    }
}
