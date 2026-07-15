package view;

import model.DDragonParser;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Async loader for champion square icons. Icons are fetched off the EDT (network/disk on first
 * miss, then cached by DDragonParser) and delivered back on the EDT.
 */
public final class ChampionIcons {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "champ-icon-loader");
        t.setDaemon(true);
        return t;
    });
    private static final Set<String> INFLIGHT = ConcurrentHashMap.newKeySet();

    private ChampionIcons() {
    }

    /** Cache-only lookup, safe on the EDT. Returns null if not loaded yet. */
    public static ImageIcon cached(String championName, int size) {
        return DDragonParser.getCachedChampionIcon(championName, size);
    }

    /** Load for a single component (picker face, bench button). Callback runs on the EDT. */
    public static void load(String championName, int size, Consumer<ImageIcon> onLoaded) {
        if (championName == null || championName.isBlank()) {
            onLoaded.accept(null);
            return;
        }
        ImageIcon hit = cached(championName, size);
        if (hit != null) {
            onLoaded.accept(hit);
            return;
        }
        POOL.submit(() -> {
            ImageIcon icon = DDragonParser.getChampionIcon(championName, size);
            SwingUtilities.invokeLater(() -> onLoaded.accept(icon));
        });
    }

    /**
     * Ensure an icon is cached, then repaint the given component. De-duplicates concurrent
     * requests, so it is safe to call from a list cell renderer on every paint.
     */
    public static void ensure(String championName, int size, JComponent toRepaint) {
        if (championName == null || championName.isBlank() || cached(championName, size) != null) {
            return;
        }
        String key = championName + "@" + size;
        if (!INFLIGHT.add(key)) {
            return;
        }
        POOL.submit(() -> {
            DDragonParser.getChampionIcon(championName, size);
            INFLIGHT.remove(key);
            SwingUtilities.invokeLater(toRepaint::repaint);
        });
    }
}
