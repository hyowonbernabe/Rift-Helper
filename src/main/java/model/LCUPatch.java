package model;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LCUPatch {
    public static int patchToClient(String endpoint, String jsonBody) {
        try {
            // Construct the request URL
            String urlString = "https://127.0.0.1:" + LCUAuth.port + endpoint;
            URL url = new URL(urlString);

            // Open an HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setDoOutput(true); // Enables sending request body

            // Set authentication headers
            String auth = "riot:" + LCUAuth.token;
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/json");

            // Send the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            return connection.getResponseCode();
        } catch (Exception e) {
            System.out.println("Error sending PATCH request to " + endpoint);
            return -1;
        }
    }
}
