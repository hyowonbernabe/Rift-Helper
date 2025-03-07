package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {

    @JsonProperty("allowRerolling")
    private boolean allowRerolling;

    @JsonProperty("benchEnabled")
    private boolean benchEnabled;

    @JsonProperty("hasSimultaneousBans")
    private boolean hasSimultaneousBans;

    @JsonProperty("hasSimultaneousPicks")
    private boolean hasSimultaneousPicks;

    @JsonProperty("isCustomGame")
    private boolean isCustomGame;

    public Session() {}

    public Session(boolean allowRerolling, boolean benchEnabled, boolean hasSimultaneousBans, boolean hasSimultaneousPicks, boolean isCustomGame) {
        this.allowRerolling = allowRerolling;
        this.benchEnabled = benchEnabled;
        this.hasSimultaneousBans = hasSimultaneousBans;
        this.hasSimultaneousPicks = hasSimultaneousPicks;
        this.isCustomGame = isCustomGame;
    }


    public boolean isAllowRerolling() {
        return allowRerolling;
    }

    public void setAllowRerolling(boolean allowRerolling) {
        this.allowRerolling = allowRerolling;
    }

    public boolean isBenchEnabled() {
        return benchEnabled;
    }

    public void setBenchEnabled(boolean benchEnabled) {
        this.benchEnabled = benchEnabled;
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

    @JsonIgnoreProperties
    public static List<Session> parseFromJson(String eventData) {
        List<Session> session = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<JsonNode> jsonNodes = objectMapper.readValue(eventData, new TypeReference<List<JsonNode>>() {});

            for (JsonNode node : jsonNodes) {
                boolean allowRerolling = Parse.parseBoolean(node.get("allowRerolling"));
                boolean benchEnabled = Parse.parseBoolean(node.get("benchEnabled"));
                boolean hasSimultaneousBans = Parse.parseBoolean(node.get("hasSimultaneousBans"));
                boolean hasSimultaneousPicks = Parse.parseBoolean(node.get("hasSimultaneousPicks"));
                boolean isCustomGame = Parse.parseBoolean(node.get("isCustomGame"));

                session.add(new Session(allowRerolling, benchEnabled, hasSimultaneousBans, hasSimultaneousPicks, isCustomGame));
            }
        } catch (Exception e) {
            System.out.println("Error parsing Shards Loot JSON: " + e.getMessage());
        }

        return session;
    }

    @Override
    public String toString() {
        return "{" +
                "allowRerolling=" + allowRerolling +
                ", benchEnabled=" + benchEnabled +
                ", hasSimultaneousBans=" + hasSimultaneousBans +
                ", hasSimultaneousPicks=" + hasSimultaneousPicks +
                ", isCustomGame=" + isCustomGame +
                '}';
    }
}
