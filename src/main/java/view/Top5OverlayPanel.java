package view;

import model.RankedChoice;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Font;
import java.util.List;

/**
 * ARAM top-right overlay: the "Top 5 Ranked Choices" list on a rounded translucent card. Just a title
 * plus a shared {@link RankedChoicesView}; the controller pushes the live list via {@link #setChoices}.
 */
public class Top5OverlayPanel extends OverlayCard {
    private final RankedChoicesView view = new RankedChoicesView();

    public Top5OverlayPanel() {
        super(new MigLayout("insets 9 12 10 12, wrap 1, gap 4, fillx", "[grow,fill]"), 14);

        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        JLabel title = new JLabel("Top 5 Ranked");
        title.setFont(base.deriveFont(Font.BOLD, 13f));
        title.setForeground(Theme.TEXT);
        title.setIcon(Icons.of(Icons.G.SWAP, 13, Theme.ACCENT));
        title.setIconTextGap(6);
        add(title, "growx, gapbottom 2");
        add(view, "growx");
    }

    public void setChoices(List<RankedChoice> choices) {
        view.setChoices(choices);
        pack0();
    }

    // Content height changes with the number of rows; ask the hosting window to resize to fit.
    private void pack0() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.pack();
        }
    }
}
