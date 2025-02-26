package model;

import java.net.HttpURLConnection;
import java.net.URL;

public class LCUPost {
    public static int postToClient(String endpoint) {
        try {
            // Construct the request URL
            String urlString = "https://127.0.0.1:" + LCUAuth.port + endpoint;
            URL url = new URL(urlString);

            // Open an HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Set authentication headers
            String auth = "riot:" + LCUAuth.token;
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/json");

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("POST request to " + endpoint + ", response code: " + responseCode);

            return responseCode;
        } catch (Exception e) {
            System.out.println("Error checking match status.");
            return -1;
        }
    }
}
