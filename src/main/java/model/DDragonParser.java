package model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads champion data from Riot's Data Dragon (DDragon) and serves champion square icons.
 *
 * DDragon layout:
 *   versions:      https://ddragon.leagueoflegends.com/api/versions.json           (array, [0] = latest)
 *   champion list: https://ddragon.leagueoflegends.com/cdn/{ver}/data/en_US/champion.json
 *   square icon:   https://ddragon.leagueoflegends.com/cdn/{ver}/img/champion/{id}.png
 *
 * champion.json "data" is keyed by the DDragon id (e.g. "MonkeyKing"); each entry has
 * numeric "key", display "name", and "id" (also image.full without ".png") used for the icon URL.
 */
public class DDragonParser {
    private static final String VERSIONS_URL = "https://ddragon.leagueoflegends.com/api/versions.json";
    private static final String FALLBACK_VERSION = "16.14.1";
    private static String version = FALLBACK_VERSION;

    private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();      // numeric key -> display name
    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();      // display name -> numeric key
    private static final Map<String, String> NAME_TO_IMAGE_ID = new HashMap<>(); // display name -> DDragon id (icon)
    private static final List<String> NAMES = new ArrayList<>();                 // display names, sorted A-Z

    private static final Map<String, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>();
    private static final Path DISK_CACHE = Paths.get(System.getProperty("user.home"), ".rift-helper", "champion-icons");

    static {
        resolveVersion();
        loadChampionData();
    }

    private static void resolveVersion() {
        try (InputStreamReader reader = new InputStreamReader(new URL(VERSIONS_URL).openStream())) {
            var versions = JsonParser.parseReader(reader).getAsJsonArray();
            if (!versions.isEmpty()) {
                version = versions.get(0).getAsString();
            }
        } catch (Exception e) {
            System.out.println("DDragon version lookup failed, using fallback " + FALLBACK_VERSION);
        }
    }

    private static void loadChampionData() {
        String url = "https://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/champion.json";
        // TreeMap gives an alphabetically sorted name list for the champion pickers.
        Map<String, String> sortedNameToImage = new TreeMap<>();
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            if (data != null) {
                for (String key : data.keySet()) {
                    JsonObject champ = data.getAsJsonObject(key);
                    int numericId = champ.get("key").getAsInt();
                    String name = champ.get("name").getAsString();
                    String imageId = champ.get("id").getAsString();

                    ID_TO_NAME.put(numericId, name);
                    NAME_TO_ID.put(name, numericId);
                    sortedNameToImage.put(name, imageId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NAME_TO_IMAGE_ID.putAll(sortedNameToImage);
        NAMES.addAll(sortedNameToImage.keySet());
    }

    public static List<String> fetchChampionNames() {
        return new ArrayList<>(NAMES);
    }

    public static String getChampionName(int championId) {
        return ID_TO_NAME.getOrDefault(championId, "Unknown Champion");
    }

    public static int getChampionId(String championName) {
        return NAME_TO_ID.getOrDefault(championName, -1);
    }

    public static String getVersion() {
        return version;
    }

    /**
     * Warm the icon cache in the background so pickers/bench render instantly instead of loading
     * on first display. Reads from the on-disk cache after the first run, so this is cheap.
     */
    public static void prefetchIcons(int... sizes) {
        Thread t = new Thread(() -> {
            for (String name : NAMES) {
                for (int size : sizes) {
                    getChampionIcon(name, size);
                }
            }
        }, "champ-icon-prefetch");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Champion square icon by display name, scaled to size x size, cached in memory and on disk.
     * Performs network I/O on the first miss, so call off the EDT (see ChampionIcons helper).
     * Returns null if the name is unknown or the download fails.
     */
    public static ImageIcon getChampionIcon(String championName, int size) {
        String imageId = NAME_TO_IMAGE_ID.get(championName);
        if (imageId == null) {
            return null;
        }
        String cacheKey = imageId + "@" + size;
        ImageIcon cached = ICON_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        return loadAndScale(imageId, cacheKey, size);
    }

    /** Cache-only lookup (no network/disk I/O). Safe to call on the EDT, e.g. from a list cell renderer. */
    public static ImageIcon getCachedChampionIcon(String championName, int size) {
        String imageId = NAME_TO_IMAGE_ID.get(championName);
        if (imageId == null) {
            return null;
        }
        return ICON_CACHE.get(imageId + "@" + size);
    }

    private static ImageIcon loadAndScale(String imageId, String cacheKey, int size) {
        try {
            byte[] png = loadPng(imageId);
            if (png == null) {
                return null;
            }
            Image scaled = new ImageIcon(png).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);
            ICON_CACHE.put(cacheKey, icon);
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] loadPng(String imageId) {
        Path file = DISK_CACHE.resolve(version + "_" + imageId + ".png");
        try {
            if (Files.exists(file)) {
                return Files.readAllBytes(file);
            }
        } catch (Exception ignore) {
            // fall through to network
        }
        String url = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/champion/" + imageId + ".png";
        try (InputStream in = new URL(url).openStream()) {
            byte[] bytes = in.readAllBytes();
            try {
                Files.createDirectories(DISK_CACHE);
                Files.write(file, bytes);
            } catch (Exception ignore) {
                // disk cache is best-effort
            }
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }
}
