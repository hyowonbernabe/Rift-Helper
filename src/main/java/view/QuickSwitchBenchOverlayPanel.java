package view;

import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;

/**
 * ARAM top-center overlay: a compact clickable copy of the Quick Switch Bench. Each button mirrors the
 * matching main-window bench button (icon + name) and, when clicked, fires that button so the existing
 * swap logic runs unchanged. The controller calls {@link #refresh()} whenever the bench updates.
 */
public class QuickSwitchBenchOverlayPanel extends OverlayCard {
    private final JButton[] main;               // the main window's bench buttons (source of truth)
    private final ChampionButton[] mirror;

    public QuickSwitchBenchOverlayPanel(RiftHelperMainView v) {
        super(new MigLayout("insets 8 10 9 10, wrap 1, gap 4, fillx", "[grow,fill]"), 14);
        this.main = v.getButtonBench();
        this.mirror = new ChampionButton[main.length];

        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        JLabel title = new JLabel("Quick Switch Bench");
        title.setFont(base.deriveFont(Font.BOLD, 13f));
        title.setForeground(Theme.TEXT);
        title.setIcon(Icons.of(Icons.G.SWAP, 13, Theme.ACCENT));
        title.setIconTextGap(6);
        add(title, "growx, gapbottom 2");

        javax.swing.JPanel grid = new javax.swing.JPanel(new MigLayout("insets 0, wrap 5, gap 5", "[]", "[]"));
        grid.setOpaque(false);
        for (int i = 0; i < mirror.length; i++) {
            final int idx = i;
            ChampionButton b = new ChampionButton();
            b.setPreferredSize(new Dimension(UIScale.scale(46), UIScale.scale(46)));
            b.setFont(b.getFont().deriveFont(9f));
            b.addActionListener(e -> main[idx].doClick()); // reuse the main bench's swap action
            mirror[i] = b;
            grid.add(b, "hidemode 3");
        }
        add(grid, "growx");
        refresh();
    }

    /** Copy champion text (icon loads from it) + visibility from the main bench, then resize to fit. */
    public void refresh() {
        for (int i = 0; i < mirror.length; i++) {
            String text = main[i].getText();
            boolean has = text != null && !text.isBlank();
            mirror[i].setText(has ? text : null);
            mirror[i].setVisible(has);
        }
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.pack();
        }
    }
}
