package model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class DDragonParser {
    private static final String DDRAGON_URL = "https://ddragon.leagueoflegends.com/cdn/15.4.1/data/en_US/champion.json";
    private static final Map<Integer, String> CHAMPION_MAP = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_ID_MAP = new HashMap<>();

    static {
        loadChampionData();
    }

    private static void loadChampionData() {
        try (InputStreamReader reader = new InputStreamReader(new URL(DDRAGON_URL).openStream())) {
            Gson gson = new Gson();
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");

            if (data != null) {
                for (String key : data.keySet()) {
                    JsonObject champNode = data.getAsJsonObject(key);
                    int champId = champNode.get("key").getAsInt();
                    String champName = champNode.get("name").getAsString();

                    CHAMPION_MAP.put(champId, champName);
                    NAME_TO_ID_MAP.put(champName, champId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> fetchChampionNames() {
        List<String> championNames = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(new URL(DDRAGON_URL).openStream())) {
            Gson gson = new Gson();
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");

            if (data != null) {
                for (String key : data.keySet()) {
                    championNames.add(data.getAsJsonObject(key).get("name").getAsString());
                }
            }
        } catch (Exception e) {
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