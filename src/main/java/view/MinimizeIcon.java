package view;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Title-bar minimize glyph, drawn as a solid horizontal bar at least 2px thick.
 *
 * <p>FlatLaf's stock {@code FlatWindowIconifyIcon} draws a 1px line; at the app's fractional UI scale
 * (default 90%) that single device-pixel line renders sub-pixel and is effectively invisible, while
 * the maximize box and close X (which have more mass) still show. Registered under
 * {@code TitlePane.iconifyIcon} it replaces the stock icon so minimize is always visible at any scale.
 * Sized to the title-bar button (44x30, scaled) so it centers exactly like the other two buttons.
 */
public final class MinimizeIcon implements Icon {

    @Override
    public int getIconWidth() {
        return UIScale.scale(44);
    }

    @Override
    public int getIconHeight() {
        return UIScale.scale(30);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fg = (c != null) ? c.getForeground() : null;
            if (fg == null) {
                fg = UIManager.getColor("TitlePane.foreground");
            }
            if (fg == null) {
                fg = Color.LIGHT_GRAY;
            }
            g2.setColor(fg);

            int lineW = UIScale.scale(10);           // match TitlePane.buttonSymbolHeight (=10)
            int thick = Math.max(2, UIScale.scale(2)); // >= 2px so it never sub-pixels away
            int lx = x + (getIconWidth() - lineW) / 2;
            int ly = y + (getIconHeight() - thick) / 2;
            g2.fillRect(lx, ly, lineW, thick);
        } finally {
            g2.dispose();
        }
    }
}
