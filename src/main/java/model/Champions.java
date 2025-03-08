package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Champions {
    public static int[] parseChampionIdsFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(eventData);
            if (rootNode.isArray()) {
                return objectMapper.treeToValue(rootNode, int[].class);
            } else {
                return new int[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new int[0];
        }
    }
}
