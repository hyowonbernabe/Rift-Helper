package model;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.util.Arrays;

public class Parse {
    public static int parseInt(JsonNode node) {
        if (node == null || node.isNull()) return 0;
        try {
            return node.isInt() ? node.asInt() : Integer.parseInt(node.asText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean parseBoolean(JsonNode node) {
        if (node == null || node.isNull()) return false;
        String text = node.asText().trim().toLowerCase();
        return text.equals("true") || text.equals("1");
    }
}
