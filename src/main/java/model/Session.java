package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public static Session parseFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventData, Session.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
