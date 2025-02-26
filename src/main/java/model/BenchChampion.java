package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BenchChampion {
    private int championId;

    @JsonProperty("isPriority")
    private boolean isPriority;

    public BenchChampion() {
    }

    public BenchChampion(int championId, boolean isPriority) {
        this.championId = championId;
        this.isPriority = isPriority;
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        this.championId = championId;
    }

    public boolean isPriority() {
        return isPriority;
    }

    public void setPriority(boolean isPriority) {
        this.isPriority = isPriority;
    }

    @Override
    public String toString() {
        return championId + "";
    }

    @JsonIgnoreProperties
    public static List<BenchChampion> parseFromJson(String eventData) {
        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse JSON string into a JsonNode
            JsonNode rootNode = objectMapper.readTree(eventData);

            // Navigate to benchChampions inside "data"
            JsonNode benchChampionsNode = rootNode
                    .path("OnJsonApiEvent_lol-champ-select_v1_session")
                    .path("data")
                    .path("benchChampions");

            // Convert JSON array to List<BenchChampion>
            return objectMapper.readValue(benchChampionsNode.toString(), new TypeReference<List<BenchChampion>>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}