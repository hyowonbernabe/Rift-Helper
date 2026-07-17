package view;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Font;

/**
 * Dense lobby overlay content: the eight Lobby-section toggles, compacted, on a rounded translucent
 * {@link OverlayCard}. Each switch is built over the SAME public Enable/Disable {@link JButton} pair
 * the main window uses, so toggling here and toggling in the main window drive identical state and
 * stay in sync automatically (no extra controller wiring). Smaller and tighter than the main window
 * per design; labels are shortened for density.
 */
public class LobbyOverlayPanel extends OverlayCard {

    public LobbyOverlayPanel(RiftHelperMainView v) {
        super(new MigLayout("insets 9 12 10 12, wrap 1, gap 3, fillx", "[grow,fill]"), 14);

        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        Font fRow = base.deriveFont(12f);
        Font fTitle = base.deriveFont(Font.BOLD, 13f);
        Font fTag = base.deriveFont(11f);

        JPanel head = new JPanel(new MigLayout("insets 0 0 2 0, gap 7", "[]7[]push", "[]"));
        head.setOpaque(false);
        JLabel title = new JLabel("Rift Helper");
        title.setFont(fTitle);
        title.setForeground(Theme.TEXT);
        title.setIcon(Icons.of(Icons.G.LOBBY, 13, Theme.ACCENT));
        title.setIconTextGap(6);
        JLabel tag = new JLabel("Lobby");
        tag.setFont(fTag);
        tag.setForeground(Theme.TEXT_FAINT);
        head.add(title);
        head.add(tag);
        add(head, "growx, gapbottom 2");

        add(row("Auto Accept", fRow, v.buttonAutoAcceptEnable, v.buttonAutoAcceptDisable), "growx");
        add(row("Auto Decline", fRow, v.buttonAutoDeclineEnable, v.buttonAutoDeclineDisable), "growx");
        add(row("Auto Minimize", fRow, v.buttonAutoMinimizeEnable, v.buttonAutoMinimizeDisable), "growx");
        add(row("Auto Honor", fRow, v.buttonAutoHonorEnable, v.buttonAutoHonorDisable), "growx");
        add(row("Auto Skip Screens", fRow, v.buttonAutoSkipScreensEnable, v.buttonAutoSkipScreensDisable), "growx");
        add(row("Group Auto Queue", fRow, v.buttonGroupAutoQueueEnable, v.buttonGroupAutoQueueDisable), "growx");
        add(row("Solo Auto Queue", fRow, v.buttonSoloAutoQueueEnable, v.buttonSoloAutoQueueDisable), "growx");
        add(row("Auto Claim Passes", fRow, v.buttonAutoClaimPassesEnable, v.buttonAutoClaimPassesDisable), "growx");
    }

    private JComponent row(String label, Font font, JButton enable, JButton disable) {
        JPanel row = new JPanel(new MigLayout("insets 2 0 2 0, gap 10, fillx", "[grow,fill][]"));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(font);
        l.setForeground(Theme.TEXT);
        row.add(l, "growx");
        row.add(new ToggleSwitch(enable, disable));
        return row;
    }
}
