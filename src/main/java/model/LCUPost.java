package model;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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

            return connection.getResponseCode();
        } catch (Exception e) {
            System.out.println("Error sending POST request to \" + endpoint");
            return -1;
        }
    }

    public static int postToClientWithBody(String endpoint, String jsonBody) {
        try {
            // Construct the request URL
            String urlString = "https://127.0.0.1:" + LCUAuth.port + endpoint;
            URL url = new URL(urlString);

            // Open an HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Set authentication headers
            String auth = "riot:" + LCUAuth.token;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/json");

            // Write the JSON body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            return connection.getResponseCode();
        } catch (Exception e) {
            System.out.println("Error sending POST request with body: " + e.getMessage());
            return -1;
        }
    }
}
