package model;

import com.google.gson.Gson;

public class GsonParser {
    public static int[] parseFromJsonIntArray(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, int[].class);
    }
}
