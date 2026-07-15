package view;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

/**
 * A switch that adapts the existing two-button (Enable / Disable) model without changing controller
 * logic. It reflects the real state from the buttons' enabled property and forwards clicks to them:
 *
 *   OFF state: Enable button is enabled -> a click calls enableButton.doClick()
 *   ON  state: Enable button is disabled (the controller disables it after enabling) -> disableButton.doClick()
 *
 * The visual only changes when the controller actually flips the buttons' enabled state, so blocked
 * actions (e.g. a mutual-exclusion dialog that returns without changing state) leave the switch put.
 */
public class ToggleSwitch extends JComponent {
    private final JButton enableButton;
    private final JButton disableButton;

    public ToggleSwitch(JButton enableButton, JButton disableButton) {
        this.enableButton = enableButton;
        this.disableButton = disableButton;

        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = new Dimension(UIScale.scale(42), UIScale.scale(24));
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                fireToggle();
            }
        });

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "toggle");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "toggle");
        getActionMap().put("toggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireToggle();
            }
        });

        PropertyChangeListener repaintOnStateChange = e -> repaint();
        enableButton.addPropertyChangeListener("enabled", repaintOnStateChange);
        disableButton.addPropertyChangeListener("enabled", repaintOnStateChange);
    }

    private boolean isOn() {
        // Controller disables the Enable button once the feature is on.
        return !enableButton.isEnabled();
    }

    private void fireToggle() {
        requestFocusInWindow();
        if (isOn()) {
            if (disableButton.isEnabled()) {
                disableButton.doClick();
            }
        } else {
            if (enableButton.isEnabled()) {
                enableButton.doClick();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean on = isOn();
        int trackW = UIScale.scale(40);
        int trackH = UIScale.scale(22);
        int inset = UIScale.scale(3);
        int x = (getWidth() - trackW) / 2;
        int y = (getHeight() - trackH) / 2;

        g2.setColor(on ? Theme.ACCENT_SOFT : Theme.SURFACE_2);
        g2.fillRoundRect(x, y, trackW, trackH, trackH, trackH);
        g2.setColor(on ? Theme.ACCENT : Theme.LINE);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawRoundRect(x, y, trackW, trackH, trackH, trackH);

        int knob = trackH - inset * 2;
        int kx = on ? x + trackW - knob - inset : x + inset;
        int ky = y + inset;
        g2.setColor(on ? Theme.ACCENT : Theme.TEXT_FAINT);
        g2.fillOval(kx, ky, knob, knob);

        if (isFocusOwner()) {
            g2.setColor(on ? Theme.ACCENT : Theme.TEXT_DIM);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x - inset, y - inset, trackW + inset * 2, trackH + inset * 2, trackH, trackH);
        }
        g2.dispose();
    }
}
