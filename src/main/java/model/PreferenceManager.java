package model;

import javax.swing.*;
import java.io.FileOutputStream;
import java.util.prefs.Preferences;

public class PreferenceManager {
    private static final String PREF_AUTO_SWAP_PRIORITY_1 = "autoSwapPriority1";
    private static final String PREF_AUTO_SWAP_PRIORITY_2 = "autoSwapPriority2";
    private static final String PREF_AUTO_SWAP_PRIORITY_3 = "autoSwapPriority3";
    private static final String PREF_AUTO_SWAP_PRIORITY_4 = "autoSwapPriority4";
    private static final String PREF_AUTO_SWAP_PRIORITY_5 = "autoSwapPriority5";
    private static final String PREF_AUTO_SWAP_PRIORITY_6 = "autoSwapPriority6";
    private static final String PREF_AUTO_SWAP_PRIORITY_7 = "autoSwapPriority7";
    private static final String PREF_AUTO_SWAP_PRIORITY_8 = "autoSwapPriority8";
    private static final String PREF_AUTO_SWAP_PRIORITY_9 = "autoSwapPriority9";
    private static final String PREF_AUTO_SWAP_PRIORITY_10 = "autoSwapPriority10";
    private static final String PREF_CENTER_GUI = "centerGUI";
    private static final String PREF_AUTO_SWAP_SLOTS = "autoSwapSlots";
    private static final String PREF_ALWAYS_ON_TOP = "alwaysOnTop";

    private static Preferences prefs;

    static {
        prefs = Preferences.userNodeForPackage(PreferenceManager.class);
    }

    public static void setAutoSwapPriority(String[] priority) {
        if (priority[0] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_5, priority[4]);
        }
        if (priority[5] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_6, priority[5]);
        }
        if (priority[6] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_7, priority[6]);
        }
        if (priority[7] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_8, priority[7]);
        }
        if (priority[8] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_9, priority[8]);
        }
        if (priority[9] != null) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_10, priority[9]);
        }
    }

    public static String[] getAutoSwapPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_SWAP_PRIORITY_1, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_2, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_3, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_4, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_5, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_6, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_7, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_8, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_9, null),
                prefs.get(PREF_AUTO_SWAP_PRIORITY_10, null)
        };
    }

    public static void setCenterGUI(boolean value) {
        prefs.putBoolean(PREF_CENTER_GUI, value);
    }

    public static boolean getCenterGUI() {
        return prefs.getBoolean(PREF_CENTER_GUI, false);
    }

    public static void setAutoSwapSlots(int value) {
        prefs.putInt(PREF_AUTO_SWAP_SLOTS, value);
    }

    public static int getAutoSwapSlots() {
        return prefs.getInt(PREF_AUTO_SWAP_SLOTS, 5);
    }

    public static void setAlwaysOnTop(boolean value) {
        prefs.putBoolean(PREF_ALWAYS_ON_TOP, value);
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getAlwaysOnTop() {
        return prefs.getBoolean(PREF_ALWAYS_ON_TOP, false);
    }

    public static void exportPreferences() {
        try {
            prefs.exportNode(new FileOutputStream("rift_helper_preferences.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}