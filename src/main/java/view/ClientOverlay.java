package view;

import com.sun.jna.platform.win32.WinDef.HWND;
import model.ClientWindow;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;

/**
 * Foundation for every in-client overlay. A frameless, per-pixel-transparent, always-on-top window
 * that docks an {@link OverlayCard} onto the League client and follows it: each tick it mirrors the
 * client's position/size, anchors (or uses a saved custom offset), and shows itself only while a
 * caller-supplied predicate holds AND the client is the focused, non-minimized foreground window.
 *
 * Adds two shared behaviors every overlay inherits:
 *  - Opacity: unhovered vs hovered (hover = mouse within the overlay bounds).
 *  - Drag: while the global drag chord ({@link #setDragKeys}) is held, a glass pane lets the user
 *    drag the overlay anywhere; the resulting client-relative offset is reported via
 *    {@link #setOnMoved} so the caller can persist it, and applied via {@link #setCustomOffset}.
 *
 * Never takes focus (focusableWindowState=false), so clicking a control does not steal focus from the
 * client. Windows-only (geometry via {@link ClientWindow}); elsewhere it simply never shows.
 */
public class ClientOverlay extends JWindow {

    public enum Anchor { BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT, TOP_CENTER }

    /** Drag chord as Windows virtual-key codes; ALL must be held. Shared by every overlay. */
    private static volatile int[] dragKeys = {0x12, 0x14}; // VK_MENU (Alt) + VK_CAPITAL (Caps Lock)

    public static void setDragKeys(int[] keys) {
        if (keys != null && keys.length > 0) {
            dragKeys = keys.clone();
        }
    }

    private static boolean isDragChordHeld() {
        int[] keys = dragKeys;
        for (int vk : keys) {
            if (!ClientWindow.isKeyDown(vk)) {
                return false;
            }
        }
        return keys.length > 0;
    }

    private final OverlayCard card;
    private final Anchor anchor;
    private final int margin;
    private final BooleanSupplier visibleWhen;
    private final Timer timer;
    private final DragGlass glass = new DragGlass();

    private float unhoveredOpacity = 0.5f;
    private float hoveredOpacity = 0.8f;
    private boolean hovered;

    // Client-relative offset (physical px) the user dragged to; null = use the anchor.
    private volatile Integer offsetX;
    private volatile Integer offsetY;
    private java.util.function.BiConsumer<Integer, Integer> onMoved = (x, y) -> { };

    private boolean dragging; // glass currently active (chord held)

    public ClientOverlay(OverlayCard content, Anchor anchor, int margin, BooleanSupplier visibleWhen) {
        this.card = content;
        this.anchor = anchor;
        this.margin = margin;
        this.visibleWhen = visibleWhen;

        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        try {
            setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {
            // translucency unsupported: fall back to opaque
        }
        setContentPane(content);
        setGlassPane(glass);
        glass.setVisible(false);
        pack();

        content.setOpacity(unhoveredOpacity);

        this.timer = new Timer(60, e -> tick());
        this.timer.setRepeats(true);
    }

    public void setOpacities(float unhovered, float hovered) {
        this.unhoveredOpacity = clamp01(unhovered);
        this.hoveredOpacity = clamp01(hovered);
        card.setOpacity(this.hovered ? this.hoveredOpacity : this.unhoveredOpacity);
    }

    /** Restore a previously saved client-relative offset (physical px), or clear with nulls. */
    public void setCustomOffset(Integer x, Integer y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    /** Called (x, y client-relative physical px) whenever the user finishes dragging. */
    public void setOnMoved(java.util.function.BiConsumer<Integer, Integer> cb) {
        this.onMoved = cb != null ? cb : (x, y) -> { };
    }

    public void start() {
        timer.start();
    }

    private void tick() {
        if (!visibleWhen.getAsBoolean()) {
            hideOverlay();
            return;
        }
        HWND client = ClientWindow.findHwnd();
        if (client == null || ClientWindow.isMinimizedOf(client) || !ClientWindow.isForeground(client)) {
            hideOverlay();
            return;
        }
        Rectangle b = ClientWindow.boundsOf(client);
        if (b == null) {
            hideOverlay();
            return;
        }

        double s = getGraphicsConfiguration().getDefaultTransform().getScaleX();
        Dimension sz = getSize();
        int cwPhys = (int) Math.round(sz.width * s);
        int chPhys = (int) Math.round(sz.height * s);

        // Drag chord: toggle the glass pane so the user can move the overlay from anywhere on it.
        boolean chord = isDragChordHeld();
        if (chord != dragging) {
            dragging = chord;
            glass.setVisible(chord);
            if (!chord) {
                // finished dragging: persist wherever it ended up
                if (offsetX != null && offsetY != null) {
                    onMoved.accept(offsetX, offsetY);
                }
            }
        }

        // Position: saved custom offset (client-relative) if present, else the anchor.
        Point tlPhys;
        if (offsetX != null && offsetY != null) {
            tlPhys = new Point(b.x + offsetX, b.y + offsetY);
        } else {
            int mPhys = (int) Math.round(margin * s);
            tlPhys = anchorTopLeftPhys(anchor, b, cwPhys, chPhys, mPhys);
        }
        // physical -> Java (logical) coords. Exact at 100%; approximate for a single uniform HiDPI
        // scale. ponytail: uniform-scale ceiling; per-monitor DPI is the upgrade path.
        if (!dragging) {
            // while actively dragging, the glass owns the position; don't fight it
            setLocation((int) Math.round(tlPhys.x / s), (int) Math.round(tlPhys.y / s));
        }

        // Hover opacity (mouse anywhere within the overlay bounds).
        boolean nowHover = isVisible() && getBounds().contains(MouseInfo.getPointerInfo().getLocation());
        if (nowHover != hovered) {
            hovered = nowHover;
            card.setOpacity(hovered ? hoveredOpacity : unhoveredOpacity);
        }

        if (!isVisible()) {
            setVisible(true);
        }
    }

    private void hideOverlay() {
        if (isVisible()) {
            setVisible(false);
        }
        if (dragging) {
            dragging = false;
            glass.setVisible(false);
        }
    }

    /** Physical top-left for the content given the client rect and anchor. Pure function (testable). */
    static Point anchorTopLeftPhys(Anchor anchor, Rectangle client, int cwPhys, int chPhys, int mPhys) {
        int left = client.x + mPhys;
        int right = client.x + client.width - cwPhys - mPhys;
        int top = client.y + mPhys;
        int bottom = client.y + client.height - chPhys - mPhys;
        int centerX = client.x + (client.width - cwPhys) / 2;
        return switch (anchor) {
            case BOTTOM_LEFT -> new Point(left, bottom);
            case BOTTOM_RIGHT -> new Point(right, bottom);
            case TOP_LEFT -> new Point(left, top);
            case TOP_RIGHT -> new Point(right, top);
            case TOP_CENTER -> new Point(centerX, top);
        };
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(v, 1f));
    }

    /** Transparent, MOVE-cursor pane shown only while the drag chord is held; drags the whole window
     *  and records the client-relative offset so it can be saved and restored. */
    private class DragGlass extends JComponent {
        private Point pressScreen;
        private Point windowAtPress;

        DragGlass() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pressScreen = e.getLocationOnScreen();
                    windowAtPress = ClientOverlay.this.getLocationOnScreen();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (pressScreen == null) {
                        return;
                    }
                    Point now = e.getLocationOnScreen();
                    int nx = windowAtPress.x + (now.x - pressScreen.x);
                    int ny = windowAtPress.y + (now.y - pressScreen.y);
                    ClientOverlay.this.setLocation(nx, ny);
                    // record client-relative offset in physical px
                    HWND client = ClientWindow.findHwnd();
                    Rectangle b = client == null ? null : ClientWindow.boundsOf(client);
                    if (b != null) {
                        double s = getGraphicsConfiguration().getDefaultTransform().getScaleX();
                        offsetX = (int) Math.round(nx * s) - b.x;
                        offsetY = (int) Math.round(ny * s) - b.y;
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // fully transparent: it only intercepts mouse events during drag mode
        }
    }

    /** Self-check for the anchor math. Run: java -ea view.ClientOverlay */
    public static void main(String[] args) {
        Rectangle client = new Rectangle(320, 156, 1280, 720);
        int m = 16;
        Point bl = anchorTopLeftPhys(Anchor.BOTTOM_LEFT, client, 300, 400, m);
        assert bl.x == 320 + 16 : "BL x";
        assert bl.y == 156 + 720 - 400 - 16 : "BL y";
        Point tr = anchorTopLeftPhys(Anchor.TOP_RIGHT, client, 300, 400, m);
        assert tr.x == 320 + 1280 - 300 - 16 : "TR x";
        assert tr.y == 156 + 16 : "TR y";
        assert bl.x >= client.x && bl.y >= client.y : "inside origin";
        assert bl.x + 300 <= client.x + client.width : "inside right";
        assert bl.y + 400 <= client.y + client.height : "inside bottom";
        System.out.println("ClientOverlay anchor math OK");
    }
}
