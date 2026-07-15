package model;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Publishes phone notifications via ntfy.sh (https://ntfy.sh). The phone subscribes to a user-chosen
 * topic in the ntfy app; the desktop app publishes by POST https://ntfy.sh/&lt;topic&gt; with the
 * message as the body and optional Title / Priority (1-5) / Tags (comma-separated emoji shortcodes)
 * headers.
 *
 * ntfy.sh is an EXTERNAL host, so every publish runs on a daemon single-thread executor: a slow or
 * hung network call can never stall the LCU socket event handlers that call this. Failures are
 * swallowed (logged, never thrown) so a notification hiccup never breaks the automation loop.
 *
 * Public topics need no auth; an auth token could be added later but is not required now.
 */
public final class Ntfy {
    // Single daemon thread: publishes are fire-and-forget and must not block the caller (the LCU
    // socket thread) or keep the JVM alive on exit.
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ntfy-publish");
        t.setDaemon(true);
        return t;
    });

    private Ntfy() {
    }

    /** Fire-and-forget publish. No-op when the topic is blank; never throws. */
    public static void publish(String topic, String title, String message, int priority, String tags) {
        if (topic == null || topic.isBlank()) {
            return;
        }
        final String t = topic.trim();
        final int p = clampPriority(priority);
        EXEC.submit(() -> send(t, title, message, p, tags));
    }

    private static void send(String topic, String title, String message, int priority, String tags) {
        try {
            // SSLBypass installs a trust-all SSLContext at startup; ntfy.sh has a valid public cert,
            // so this validates fine either way.
            URL url = new URL("https://ntfy.sh/" + topic);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (title != null && !title.isBlank()) {
                connection.setRequestProperty("Title", title);
            }
            connection.setRequestProperty("Priority", String.valueOf(priority));
            if (tags != null && !tags.isBlank()) {
                connection.setRequestProperty("Tags", tags);
            }

            byte[] body = (message == null ? "" : message).getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body);
            }

            int code = connection.getResponseCode();
            System.out.println("[Ntfy] " + topic + " -> " + code);
        } catch (Exception e) {
            System.out.println("[Ntfy] publish failed: " + e.getMessage());
        }
    }

    /** Pure: ntfy priorities are 1..5; clamp anything outside that range. */
    static int clampPriority(int p) {
        if (p < 1) {
            return 1;
        }
        if (p > 5) {
            return 5;
        }
        return p;
    }

    // Pure-logic self-check: java -ea model.Ntfy
    public static void main(String[] args) {
        assert clampPriority(0) == 1 : "0 -> 1";
        assert clampPriority(-4) == 1 : "negative -> 1";
        assert clampPriority(1) == 1;
        assert clampPriority(3) == 3;
        assert clampPriority(5) == 5;
        assert clampPriority(6) == 5 : "over max -> 5";
        System.out.println("Ntfy.clampPriority self-check passed.");
    }
}
