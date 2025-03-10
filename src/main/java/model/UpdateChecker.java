package model;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import view.RiftHelperMainView;

import javax.swing.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String CURRENT_VERSION = "1.3.1";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/hyowonbernabe/Rift-Helper/main/version.json\n";

    public static void checkForUpdate(RiftHelperMainView riftHelperMainView) {
        try {
            VersionInfo latestVersionInfo = fetchLatestVersionInfo(VERSION_URL);

            if (!CURRENT_VERSION.equals(latestVersionInfo.latest)) {
                JOptionPane.showMessageDialog(riftHelperMainView,
                        "A new version (" + latestVersionInfo.latest + ") is available!\nCheck it out: " + latestVersionInfo.url,
                        "Update Available",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Failed to check for updates: " + e.getMessage());
        }
    }

    private static VersionInfo fetchLatestVersionInfo(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream());
             JsonReader jsonReader = new JsonReader(reader)) {

            Gson gson = new Gson();
            return gson.fromJson(jsonReader, VersionInfo.class);
        }
    }

    private static class VersionInfo {
        public String latest;
        public String url;
    }
}