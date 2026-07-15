package view;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Bench button that shows the champion's square icon above its name. The controller only ever calls
 * setText(championName); overriding it here loads the matching icon, so no controller change is needed.
 */
public class ChampionButton extends JButton {
    private String champion;

    public ChampionButton() {
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setFocusPainted(false);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.champion = text;
        if (text == null || text.isBlank()) {
            setIcon(null);
            return;
        }
        final String requested = text;
        ChampionIcons.load(text, 32, icon -> {
            // ignore a stale load if the bench slot changed champion in the meantime
            if (requested.equals(champion)) {
                setIcon(icon);
            }
        });
    }
}
