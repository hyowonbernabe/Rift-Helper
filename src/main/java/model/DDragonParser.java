package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDragonParser {
    private static final String DDRAGON_URL = "https://ddragon.leagueoflegends.com/cdn/15.4.1/data/en_US/champion.json";
    private static final Map<Integer, String> CHAMPION_MAP = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_ID_MAP = new HashMap<>();

    static {
        loadChampionData();
    }

    private static void loadChampionData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(new URL(DDRAGON_URL));
            JsonNode data = root.get("data");

            if (data != null) {
                for (JsonNode champNode : data) {
                    int key = champNode.get("key").asInt();
                    String name = champNode.get("name").asText();

                    CHAMPION_MAP.put(key, name);
                    NAME_TO_ID_MAP.put(name, key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> fetchChampionNames() {
        List<String> championNames = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(new URL(DDRAGON_URL));
            JsonNode data = root.get("data");

            if (data != null) {
                for (JsonNode champ : data) {
                    championNames.add(champ.get("name").asText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return championNames;
    }

    public static String getChampionName(int championId) {
        return CHAMPION_MAP.getOrDefault(championId, "Unknown Champion");
    }

    public static int getChampionId(String championName) {
        return NAME_TO_ID_MAP.getOrDefault(championName, -1);
    }
}