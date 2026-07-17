package view;

import com.formdev.flatlaf.util.UIScale;
import model.DDragonParser;
import model.RankedChoice;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Font;
import java.util.List;

/**
 * Renders the "Top 5 Ranked Choices" list: each row is rank, champion icon + name, a star if the
 * champion came from the manual Priority list (vs Survey), and a live status (swapped in / on bench /
 * not available). Reused in both the ARAM GUI tab and the ARAM top-5 overlay. Transparent so it sits
 * on whatever card contains it.
 */
public class RankedChoicesView extends JPanel {
    private final Font fRow;
    private final Font fStatus;

    public RankedChoicesView() {
        super(new MigLayout("insets 0, wrap 1, gap 3, fillx", "[grow,fill]"));
        setOpaque(false);
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        fRow = base.deriveFont(12f);
        fStatus = base.deriveFont(11f);
        setChoices(List.of());
    }

    public void setChoices(List<RankedChoice> choices) {
        removeAll();
        if (choices == null || choices.isEmpty()) {
            JLabel none = new JLabel("Auto Swap off");
            none.setFont(fStatus);
            none.setForeground(Theme.TEXT_FAINT);
            add(none, "growx");
        } else {
            for (RankedChoice c : choices) {
                add(row(c), "growx");
            }
        }
        revalidate();
        repaint();
    }

    private JComponent row(RankedChoice c) {
        JPanel row = new JPanel(new MigLayout("insets 1 0 1 0, gap 7, fillx", "[18!][22!][grow,fill][]"));
        row.setOpaque(false);

        JLabel rank = new JLabel("#" + c.rank());
        rank.setFont(fStatus);
        rank.setForeground(Theme.TEXT_DIM);
        row.add(rank);

        JLabel icon = new JLabel();
        String name = DDragonParser.getChampionName(c.championId());
        ChampionIcons.load(name, UIScale.scale(18), icon::setIcon);
        row.add(icon);

        JLabel nm = new JLabel(name);
        nm.setFont(fRow);
        nm.setForeground(Theme.TEXT);
        if (c.fromPriority()) {
            nm.setIcon(Icons.of(Icons.G.STAR, UIScale.scale(10), Theme.AMBER));
            nm.setHorizontalTextPosition(SwingConstants.RIGHT);
            nm.setIconTextGap(UIScale.scale(5));
        }
        row.add(nm, "growx");

        JLabel status = new JLabel(statusText(c));
        status.setFont(fStatus);
        status.setForeground(c.swapTarget() ? Theme.GREEN : c.onBench() ? Theme.TEXT_DIM : Theme.TEXT_FAINT);
        row.add(status);

        return row;
    }

    private String statusText(RankedChoice c) {
        if (c.swapTarget()) {
            return "swapped in";
        }
        if (c.onBench()) {
            return "on bench";
        }
        return "not available";
    }
}
