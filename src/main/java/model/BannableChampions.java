package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BannableChampions {
    @JsonProperty("championID")
    private int championID;


    public int getChampionID() {
        return championID;
    }

    public void setChampionID(int championID) {
        this.championID = championID;
    }

    public BannableChampions() {}

    public BannableChampions(int championID) {
        this.championID = championID;
    }

    @JsonIgnoreProperties
    public static List<BannableChampions> parseFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventData, new TypeReference<List<BannableChampions>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return championID + "";
    }
}
