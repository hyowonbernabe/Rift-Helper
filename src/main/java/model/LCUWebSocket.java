package model;

import javax.websocket.*;
import java.net.URI;
import java.util.Base64;

@ClientEndpoint
public class LCUWebSocket {

    public static void startListening() {
        connectToWebSocket();
    }

    private static void connectToWebSocket() {
        try {
            String url = "wss://127.0.0.1:" + LCUAuth.port + "/";
            URI websocketURI = new URI(url);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(new LCUWebSocket(), websocketURI);
        } catch (Exception e) {
            System.err.println("WebSocket connection failed: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to LCU WebSocket.");

        try {
            // Authenticate
            String auth = "riot:" + LCUAuth.token;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            session.getBasicRemote().sendText("{\"event\":\"auth\",\"data\":\"Basic " + encodedAuth + "\"}");

            // Subscribe to champ select updates
            session.getBasicRemote().sendText("[5, \"OnJsonApiEvent_lol-champ-select_v1_session\"]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received WebSocket Message: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }
}