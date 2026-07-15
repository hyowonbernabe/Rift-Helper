package view;

import javax.swing.JButton;

/**
 * Reusable "swap two entries" behavior over a row of {@link ChampionPicker}s.
 *
 * Clicking the swap button arms swap mode (the button label flips to "Cancel swap"). While armed,
 * the first picker click highlights it, and a second click on a different picker exchanges the two
 * pickers' selected champion names, clears the highlight, disarms, and runs {@code onSwap} so the
 * owner can persist the change. Clicking the same picker again cancels its selection; clicking the
 * button again cancels swap mode entirely.
 *
 * The pickers cooperate through their lightweight swap hook: {@link ChampionPicker#setSwapArmedCheck}
 * lets a picker ask whether swap mode is armed (so an armed click selects-for-swap instead of opening
 * the dropdown) and {@link ChampionPicker#setSwapClickHandler} routes that click back here.
 */
public class SwapController {
    private final JButton swapButton;
    private final ChampionPicker[] pickers;
    private final Runnable onSwap;
    private final String idleText;

    private boolean armed;
    private int firstIndex = -1;

    public SwapController(JButton swapButton, ChampionPicker[] pickers, Runnable onSwap) {
        this.swapButton = swapButton;
        this.pickers = pickers;
        this.onSwap = onSwap;
        this.idleText = swapButton.getText();

        swapButton.addActionListener(e -> {
            if (armed) {
                disarm();
            } else {
                arm();
            }
        });

        for (int i = 0; i < pickers.length; i++) {
            final int index = i;
            pickers[i].setSwapArmedCheck(() -> armed);
            pickers[i].setSwapClickHandler(() -> onPickerClicked(index));
        }
    }

    private void arm() {
        armed = true;
        firstIndex = -1;
        swapButton.setText("Cancel swap");
    }

    /** Clear any highlight and return the button to its resting state. */
    private void disarm() {
        armed = false;
        if (firstIndex >= 0 && firstIndex < pickers.length) {
            pickers[firstIndex].setSwapHighlight(false);
        }
        firstIndex = -1;
        swapButton.setText(idleText);
    }

    private void onPickerClicked(int index) {
        if (!armed) {
            return;
        }
        if (firstIndex < 0) {
            // First selection: highlight it and wait for the partner.
            firstIndex = index;
            pickers[index].setSwapHighlight(true);
            return;
        }
        if (index == firstIndex) {
            // Clicking the same picker again cancels its selection (stay armed).
            pickers[index].setSwapHighlight(false);
            firstIndex = -1;
            return;
        }
        // Second selection: exchange the two pickers' names.
        String a = pickers[firstIndex].getSelectedName();
        String b = pickers[index].getSelectedName();
        pickers[firstIndex].setSelectedName(b);
        pickers[index].setSelectedName(a);
        pickers[firstIndex].setSwapHighlight(false);
        firstIndex = -1;
        armed = false;
        swapButton.setText(idleText);
        if (onSwap != null) {
            onSwap.run();
        }
    }
}
