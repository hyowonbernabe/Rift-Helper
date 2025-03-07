package model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Champions {
    public static int[] parseChampionIdsFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventData, int[].class);
        } catch (IOException e) {
            e.printStackTrace();
            return new int[0]; // Return an empty array in case of an error
        }
    }
}
