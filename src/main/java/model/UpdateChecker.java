package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String CURRENT_VERSION = "1.3.0";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/hyowonbernabe/Rift-Helper/main/version.json\n";

    public static void checkForUpdate() {
        try {
            VersionInfo latestVersionInfo = fetchLatestVersionInfo(VERSION_URL);

            if (!CURRENT_VERSION.equals(latestVersionInfo.latest)) {
                JOptionPane.showMessageDialog(null,
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

        try (InputStream inputStream = conn.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, VersionInfo.class);
        }
    }

    private static class VersionInfo {
        public String latest;
        public String url;
    }
}