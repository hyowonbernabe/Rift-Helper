package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import view.RiftHelperMainView;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class UpdateChecker {
    private static final String CURRENT_VERSION = "1.4.0";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/hyowonbernabe/Rift-Helper/main/version.json";
    private static final String RELEASES_PAGE = "https://github.com/hyowonbernabe/Rift-Helper/releases";
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/hyowonbernabe/Rift-Helper/releases/latest";

    public static void checkForUpdate(RiftHelperMainView view) {
        try {
            JsonObject info = fetchJson(VERSION_URL);
            String latest = info.has("latest") ? info.get("latest").getAsString().trim() : null;
            String pageUrl = info.has("url") ? info.get("url").getAsString().trim() : RELEASES_PAGE;
            // Only prompt when the published version is strictly newer than this build.
            if (latest == null || !isNewer(latest, CURRENT_VERSION)) {
                return;
            }
            promptUpdate(view, latest, pageUrl);
        } catch (Exception e) {
            System.err.println("Failed to check for updates: " + e.getMessage());
        }
    }

    private static void promptUpdate(RiftHelperMainView view, String latest, String pageUrl) {
        String[] options = {"Update Now", "Open Download Page", "Later"};
        String message = "<html><b>A new version (" + latest + ") is available.</b><br>"
                + "You are on " + CURRENT_VERSION + ".</html>";
        int choice = JOptionPane.showOptionDialog(view, message, "Update Available",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            downloadAndInstall(view, pageUrl);
        } else if (choice == 1) {
            browse(pageUrl);
        }
    }

    private static void downloadAndInstall(RiftHelperMainView view, String pageUrl) {
        JDialog progress = new JDialog(view, "Updating", true);
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        panel.add(new JLabel("Downloading the latest version..."), BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        panel.add(bar, BorderLayout.CENTER);
        progress.setContentPane(panel);
        progress.pack();
        progress.setLocationRelativeTo(view);

        SwingWorker<Path, Void> worker = new SwingWorker<>() {
            @Override
            protected Path doInBackground() throws Exception {
                JsonObject release = fetchJson(LATEST_RELEASE_API);
                JsonArray assets = release.getAsJsonArray("assets");
                String downloadUrl = null;
                String name = null;
                if (assets != null) {
                    // Prefer an installer (.msi) then a standalone .exe.
                    outer:
                    for (String ext : new String[]{".msi", ".exe"}) {
                        for (int i = 0; i < assets.size(); i++) {
                            JsonObject asset = assets.get(i).getAsJsonObject();
                            String n = asset.get("name").getAsString();
                            if (n.toLowerCase(Locale.ROOT).endsWith(ext)) {
                                name = n;
                                downloadUrl = asset.get("browser_download_url").getAsString();
                                break outer;
                            }
                        }
                    }
                }
                if (downloadUrl == null) {
                    return null;
                }
                String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
                Path out = Files.createTempFile("RiftHelper-", "-" + safe);
                HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "RiftHelper");
                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
                return out;
            }

            @Override
            protected void done() {
                progress.dispose();
                try {
                    Path file = get();
                    if (file == null) {
                        fallback(view, pageUrl, "no installer found in the latest release");
                        return;
                    }
                    // Launch the installer and quit so it can replace the running files.
                    Desktop.getDesktop().open(file.toFile());
                    System.exit(0);
                } catch (Exception ex) {
                    fallback(view, pageUrl, ex.getMessage());
                }
            }
        };
        worker.execute();
        progress.setVisible(true);
    }

    private static void fallback(RiftHelperMainView view, String pageUrl, String reason) {
        int r = JOptionPane.showConfirmDialog(view,
                "<html>Automatic update failed" + (reason != null ? " (" + reason + ")" : "") + ".<br>"
                        + "Open the download page instead?</html>",
                "Update", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            browse(pageUrl);
        }
    }

    private static void browse(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            System.err.println("Could not open browser: " + e.getMessage());
        }
    }

    /** True if {@code latest} is a strictly higher dotted version than {@code current}. */
    static boolean isNewer(String latest, String current) {
        int[] a = parse(latest);
        int[] b = parse(current);
        int n = Math.max(a.length, b.length);
        for (int i = 0; i < n; i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }

    private static int[] parse(String version) {
        String[] parts = version.replaceAll("[^0-9.]", "").split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    private static JsonObject fetchJson(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "RiftHelper");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(8000);
        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
