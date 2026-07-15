package view;

import javax.swing.UIManager;
import java.awt.Color;

/**
 * Palette for the redesigned UI, derived at runtime from the active FlatLaf theme so the custom
 * components blend with the native Swing widgets (same greys, same blue accent as the original).
 * Populated on first use, which happens after FlatLaf is installed in {@code main}.
 */
public final class Theme {
    public static Color BG;
    public static Color SURFACE_0;
    public static Color SURFACE_1;
    public static Color SURFACE_2;
    public static Color RAIL;
    public static Color LINE;
    public static Color LINE_SOFT;
    public static Color TEXT;
    public static Color TEXT_DIM;
    public static Color TEXT_FAINT;
    public static Color ACCENT;
    public static Color ACCENT_TEXT;
    public static Color ACCENT_SOFT;
    public static Color ON_ACCENT;
    public static Color GREEN;
    public static Color AMBER;
    public static Color RED;

    static {
        init();
    }

    /** Re-read colors from the current look and feel. Call again if the theme changes. */
    public static void init() {
        Color panel = ui("Panel.background", 0x3C3F41);
        BG = panel;
        SURFACE_1 = panel;                              // cards match the GUI background
        SURFACE_0 = ui("MenuItem.background", 0x303234); // rail / status, slightly darker
        SURFACE_2 = ui("ComboBox.background", 0x46494B); // inputs / picker face
        RAIL = SURFACE_0;
        LINE = ui("Component.borderColor", 0x616365);
        LINE_SOFT = ui("Separator.foreground", 0x505254);
        TEXT = ui("Label.foreground", 0xBBBBBB);
        TEXT_DIM = ui("Label.disabledForeground", 0x8C8C8C);
        TEXT_FAINT = mix(TEXT_DIM, SURFACE_1, 0.38);
        ACCENT = ui("Component.accentColor", 0x4B6EAF);
        ACCENT_TEXT = lighten(ACCENT, 0.38);
        ACCENT_SOFT = mix(ACCENT, SURFACE_1, 0.72);
        ON_ACCENT = new Color(0xF2F5FA);
        GREEN = new Color(0x4A9E5C);
        AMBER = new Color(0xD0A53C);
        RED = new Color(0xD16A60);
    }

    private static Color ui(String key, int fallback) {
        Color c = UIManager.getColor(key);
        return c != null ? new Color(c.getRGB(), true) : new Color(fallback);
    }

    private static Color lighten(Color c, double f) {
        return new Color(
                clamp(c.getRed() + (255 - c.getRed()) * f),
                clamp(c.getGreen() + (255 - c.getGreen()) * f),
                clamp(c.getBlue() + (255 - c.getBlue()) * f));
    }

    private static Color mix(Color a, Color b, double t) {
        return new Color(
                clamp(a.getRed() * (1 - t) + b.getRed() * t),
                clamp(a.getGreen() * (1 - t) + b.getGreen() * t),
                clamp(a.getBlue() * (1 - t) + b.getBlue() * t));
    }

    private static int clamp(double v) {
        return Math.max(0, Math.min(255, (int) Math.round(v)));
    }

    private Theme() {
    }
}
