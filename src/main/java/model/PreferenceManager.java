package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.prefs.Preferences;

public class PreferenceManager {
    private static final String PREF_AUTO_LOCK_TOP_PRIORITY_1 = "autoLockTopPriority1";
    private static final String PREF_AUTO_LOCK_TOP_PRIORITY_2 = "autoLockTopPriority2";
    private static final String PREF_AUTO_LOCK_TOP_PRIORITY_3 = "autoLockTopPriority3";
    private static final String PREF_AUTO_LOCK_TOP_PRIORITY_4 = "autoLockTopPriority4";
    private static final String PREF_AUTO_LOCK_TOP_PRIORITY_5 = "autoLockTopPriority5";
    private static final String PREF_AUTO_LOCK_JUNGLE_PRIORITY_1 = "autoLockJunglePriority1";
    private static final String PREF_AUTO_LOCK_JUNGLE_PRIORITY_2 = "autoLockJunglePriority2";
    private static final String PREF_AUTO_LOCK_JUNGLE_PRIORITY_3 = "autoLockJunglePriority3";
    private static final String PREF_AUTO_LOCK_JUNGLE_PRIORITY_4 = "autoLockJunglePriority4";
    private static final String PREF_AUTO_LOCK_JUNGLE_PRIORITY_5 = "autoLockJunglePriority5";
    private static final String PREF_AUTO_LOCK_MID_PRIORITY_1 = "autoLockMidPriority1";
    private static final String PREF_AUTO_LOCK_MID_PRIORITY_2 = "autoLockMidPriority2";
    private static final String PREF_AUTO_LOCK_MID_PRIORITY_3 = "autoLockMidPriority3";
    private static final String PREF_AUTO_LOCK_MID_PRIORITY_4 = "autoLockMidPriority4";
    private static final String PREF_AUTO_LOCK_MID_PRIORITY_5 = "autoLockMidPriority5";
    private static final String PREF_AUTO_LOCK_BOT_PRIORITY_1 = "autoLockBotPriority1";
    private static final String PREF_AUTO_LOCK_BOT_PRIORITY_2 = "autoLockBotPriority2";
    private static final String PREF_AUTO_LOCK_BOT_PRIORITY_3 = "autoLockBotPriority3";
    private static final String PREF_AUTO_LOCK_BOT_PRIORITY_4 = "autoLockBotPriority4";
    private static final String PREF_AUTO_LOCK_BOT_PRIORITY_5 = "autoLockBotPriority5";
    private static final String PREF_AUTO_LOCK_SUPPORT_PRIORITY_1 = "autoLockSupportPriority1";
    private static final String PREF_AUTO_LOCK_SUPPORT_PRIORITY_2 = "autoLockSupportPriority2";
    private static final String PREF_AUTO_LOCK_SUPPORT_PRIORITY_3 = "autoLockSupportPriority3";
    private static final String PREF_AUTO_LOCK_SUPPORT_PRIORITY_4 = "autoLockSupportPriority4";
    private static final String PREF_AUTO_LOCK_SUPPORT_PRIORITY_5 = "autoLockSupportPriority5";
    private static final String PREF_AUTO_BAN_PRIORITY_1 = "autoBannerPriority1";
    private static final String PREF_AUTO_BAN_PRIORITY_2 = "autoBannerPriority2";
    private static final String PREF_AUTO_BAN_PRIORITY_3 = "autoBannerPriority3";
    private static final String PREF_AUTO_BAN_PRIORITY_4 = "autoBannerPriority4";
    private static final String PREF_AUTO_BAN_PRIORITY_5 = "autoBannerPriority5";
    private static final String PREF_AUTO_LOCK_ARENA_PRIORITY_1 = "autoLockArenaPriority1";
    private static final String PREF_AUTO_LOCK_ARENA_PRIORITY_2 = "autoLockArenaPriority2";
    private static final String PREF_AUTO_LOCK_ARENA_PRIORITY_3 = "autoLockArenaPriority3";
    private static final String PREF_AUTO_LOCK_ARENA_PRIORITY_4 = "autoLockArenaPriority4";
    private static final String PREF_AUTO_LOCK_ARENA_PRIORITY_5 = "autoLockArenaPriority5";
    private static final String PREF_AUTO_BAN_ARENA_PRIORITY_1 = "autoBanArenaPriority1";
    private static final String PREF_AUTO_BAN_ARENA_PRIORITY_2 = "autoBanArenaPriority2";
    private static final String PREF_AUTO_BAN_ARENA_PRIORITY_3 = "autoBanArenaPriority3";
    private static final String PREF_AUTO_BAN_ARENA_PRIORITY_4 = "autoBanArenaPriority4";
    private static final String PREF_AUTO_BAN_ARENA_PRIORITY_5 = "autoBanArenaPriority5";
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
    private static final String PREF_SYSTEM_TRAY = "systemTray";
    private static final String PREF_AUTO_LOCK_LANE_CHOICE = "autoLockLaneChoice";
    private static final String PREF_AUTO_CHECK_UPDATE = "autoCheckUpdate";

    private static Preferences prefs;

    static {
        prefs = Preferences.userNodeForPackage(PreferenceManager.class);
    }

    public static void setAutoLockTopPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_TOP_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_TOP_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_TOP_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_TOP_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_TOP_PRIORITY_5, priority[4]);
        }
    }

    public static void setAutoLockJunglePriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_JUNGLE_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_JUNGLE_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_JUNGLE_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_JUNGLE_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_JUNGLE_PRIORITY_5, priority[4]);
        }
    }

    public static void setAutoLockMidPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_MID_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_MID_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_MID_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_MID_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_MID_PRIORITY_5, priority[4]);
        }
    }

    public static void setAutoLockBotPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_BOT_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_BOT_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_BOT_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_BOT_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_BOT_PRIORITY_5, priority[4]);
        }
    }

    public static void setAutoLockSupportPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_SUPPORT_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_SUPPORT_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_SUPPORT_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_SUPPORT_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_SUPPORT_PRIORITY_5, priority[4]);
        }
    }

    public static String[] getAutoSwapTopPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_TOP_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_TOP_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_TOP_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_TOP_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_TOP_PRIORITY_5, null),
        };
    }

    public static String[] getAutoSwapJunglePriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_5, null),
        };
    }

    public static String[] getAutoLockMidPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_MID_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_MID_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_MID_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_MID_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_MID_PRIORITY_5, null),
        };
    }

    public static String[] getAutoLockBotPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_BOT_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_BOT_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_BOT_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_BOT_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_BOT_PRIORITY_5, null),
        };
    }

    public static String[] getAutoLockSupportPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_SUPPORT_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_SUPPORT_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_SUPPORT_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_SUPPORT_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_SUPPORT_PRIORITY_5, null),
        };
    }

    public static String[] getAutoLockJunglePriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_JUNGLE_PRIORITY_5, null),
        };
    }

    public static void setAutoBanPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_BAN_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_BAN_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_BAN_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_BAN_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_BAN_PRIORITY_5, priority[4]);
        }
    }

    public static String[] getAutoBanPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_BAN_PRIORITY_1, null),
                prefs.get(PREF_AUTO_BAN_PRIORITY_2, null),
                prefs.get(PREF_AUTO_BAN_PRIORITY_3, null),
                prefs.get(PREF_AUTO_BAN_PRIORITY_4, null),
                prefs.get(PREF_AUTO_BAN_PRIORITY_5, null),
        };
    }

    public static void setAutoLockArenaPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_5, priority[4]);
        }
    }

    public static String[] getAutoLockArenaPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_5, null),
        };
    }

    public static void setAutoBanArenaPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_LOCK_ARENA_PRIORITY_5, priority[4]);
        }
    }

    public static String[] getAutoBanArenaPriority() {
        return new String[]{
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_1, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_2, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_3, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_4, null),
                prefs.get(PREF_AUTO_LOCK_ARENA_PRIORITY_5, null),
        };
    }

    public static void setAutoSwapPriority(String[] priority) {
        if (priority[0] != null && !priority[0].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_1, priority[0]);
        }
        if (priority[1] != null && !priority[1].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_2, priority[1]);
        }
        if (priority[2] != null && !priority[2].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_3, priority[2]);
        }
        if (priority[3] != null && !priority[3].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_4, priority[3]);
        }
        if (priority[4] != null && !priority[4].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_5, priority[4]);
        }
        if (priority[5] != null && !priority[5].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_6, priority[5]);
        }
        if (priority[6] != null && !priority[6].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_7, priority[6]);
        }
        if (priority[7] != null && !priority[7].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_8, priority[7]);
        }
        if (priority[8] != null && !priority[8].isEmpty()) {
            prefs.put(PREF_AUTO_SWAP_PRIORITY_9, priority[8]);
        }
        if (priority[9] != null && !priority[9].isEmpty()) {
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

    public static void setAutoLockLaneChoice(int value) {
        prefs.putInt(PREF_AUTO_LOCK_LANE_CHOICE, value);
    }

    public static int getAutoLockLaneChoice() {
        return prefs.getInt(PREF_AUTO_LOCK_LANE_CHOICE, 2);
    }

    public static void setSystemTray(boolean value) {
        prefs.putBoolean(PREF_SYSTEM_TRAY, value);
    }

    public static boolean getSystemTray() {
        return prefs.getBoolean(PREF_SYSTEM_TRAY, true);
    }

    public static void setAutoCheckUpdate(boolean value) {
        prefs.putBoolean(PREF_AUTO_CHECK_UPDATE, value);
    }

    public static boolean getAutoCheckUpdate() {
        return prefs.getBoolean(PREF_AUTO_CHECK_UPDATE, true);
    }

    public static void exportPreferences() {
        try {
            String currentDir = CurrentDirectory.getCurrentDirectory();
            File outputFile = new File(currentDir, "rift_helper_preferences.xml");

            prefs.exportNode(new FileOutputStream(outputFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void importPreferences(FileInputStream input) {
        try {
            prefs.importPreferences(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetPreferences() {
        try {
            prefs.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}