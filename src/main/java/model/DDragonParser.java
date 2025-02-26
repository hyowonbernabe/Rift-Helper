package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DDragonParser {
    private static final String DDRAGON_URL = "https://ddragon.leagueoflegends.com/cdn/14.3.1/data/en_US/champion.json";
    private static final Map<Integer, String> CHAMPION_MAP = new HashMap<>();

    static {
        loadChampionData();
    }

    private static void loadChampionData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(new URL(DDRAGON_URL));
            JsonNode data = root.get("data");

            if (data != null) {
                for (JsonNode champ : data) {
                    int key = champ.get("key").asInt();
                    String name = champ.get("name").asText();
                    CHAMPION_MAP.put(key, name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getChampionName(int championId) {
        return CHAMPION_MAP.getOrDefault(championId, "Unknown Champion");
    }
}