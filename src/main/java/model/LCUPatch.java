package model;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.util.Base64;

public class LCUPatch {
    public static int patchToClientWithBody(String endpoint, String jsonBody) {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, TrustAllStrategy.INSTANCE);

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE);

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build()) {

                String urlString = "https://127.0.0.1:" + LCUAuth.port + endpoint;
                HttpPatch httpPatch = new HttpPatch(urlString);

                String auth = "riot:" + LCUAuth.token;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                httpPatch.setHeader("Authorization", "Basic " + encodedAuth);
                httpPatch.setHeader("Content-Type", "application/json");

                httpPatch.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

                try (CloseableHttpResponse response = httpClient.execute(httpPatch)) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    return responseCode;
                }
            }
        } catch (Exception e) {
            System.out.println("Error sending PATCH request with body to " + endpoint + ": " + e.getMessage());
            return -1;
        }
    }
}