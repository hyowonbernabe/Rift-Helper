package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RerollsRemaining {
    @JsonProperty("rerollsRemaining")
    private int rerolls;

    public void setRerolls(int rerolls) {
        this.rerolls = rerolls;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public String toString() {
        return rerolls + "";
    }

    @JsonIgnoreProperties
    public static int parseFromJson(String eventData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(eventData);

            return rootNode
                    .path("OnJsonApiEvent_lol-champ-select_v1_session")
                    .path("data")
                    .path("rerollsRemaining")
                    .asInt();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
