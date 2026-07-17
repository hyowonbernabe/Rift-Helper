package view;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Reusable overlay surface: a rounded card that reads on top of the League client. Colors come from
 * {@link Theme} so it matches the main window; the shape is rounded and the whole card (fill, border,
 * AND its child controls) is faded to {@link #setOpacity a caller-set opacity} by compositing the
 * entire paint at that alpha. This works on the per-pixel-transparent overlay window without the
 * fragile {@link java.awt.Window#setOpacity}. Foundation piece: every overlay panel reuses this.
 */
public class OverlayCard extends JPanel {
    private final int radius;
    private volatile float opacity = 0.5f;

    public OverlayCard(LayoutManager layout, int radius) {
        super(layout);
        this.radius = radius;
        setOpaque(false); // the window is per-pixel transparent; we paint our own rounded fill
    }

    /** Whole-card opacity (0..1), including child controls. Repaints. */
    public void setOpacity(float o) {
        float clamped = Math.max(0f, Math.min(o, 1f));
        if (clamped != opacity) {
            opacity = clamped;
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Fade the entire subtree (background + border + children) uniformly.
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        super.paint(g2);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int r = UIScale.scale(radius);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(Theme.SURFACE_1); // opaque fill; overall translucency comes from paint()'s composite
        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);

        g2.setColor(Theme.LINE);
        g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);

        g2.dispose();
    }
}
