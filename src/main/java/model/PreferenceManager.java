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
    private static final String PREF_AUTO_HONOR = "autoHonor";
    private static final String PREF_AUTO_SKIP_SCREENS = "autoSkipScreens";
    private static final String PREF_GROUP_AUTO_QUEUE = "groupAutoQueue";
    private static final String PREF_SOLO_AUTO_QUEUE = "soloAutoQueue";
    private static final String PREF_AUTO_MINIMIZE = "autoMinimize";

    private static final String[] TOP_KEYS = {PREF_AUTO_LOCK_TOP_PRIORITY_1, PREF_AUTO_LOCK_TOP_PRIORITY_2, PREF_AUTO_LOCK_TOP_PRIORITY_3, PREF_AUTO_LOCK_TOP_PRIORITY_4, PREF_AUTO_LOCK_TOP_PRIORITY_5};
    private static final String[] JUNGLE_KEYS = {PREF_AUTO_LOCK_JUNGLE_PRIORITY_1, PREF_AUTO_LOCK_JUNGLE_PRIORITY_2, PREF_AUTO_LOCK_JUNGLE_PRIORITY_3, PREF_AUTO_LOCK_JUNGLE_PRIORITY_4, PREF_AUTO_LOCK_JUNGLE_PRIORITY_5};
    private static final String[] MID_KEYS = {PREF_AUTO_LOCK_MID_PRIORITY_1, PREF_AUTO_LOCK_MID_PRIORITY_2, PREF_AUTO_LOCK_MID_PRIORITY_3, PREF_AUTO_LOCK_MID_PRIORITY_4, PREF_AUTO_LOCK_MID_PRIORITY_5};
    private static final String[] BOT_KEYS = {PREF_AUTO_LOCK_BOT_PRIORITY_1, PREF_AUTO_LOCK_BOT_PRIORITY_2, PREF_AUTO_LOCK_BOT_PRIORITY_3, PREF_AUTO_LOCK_BOT_PRIORITY_4, PREF_AUTO_LOCK_BOT_PRIORITY_5};
    private static final String[] SUPPORT_KEYS = {PREF_AUTO_LOCK_SUPPORT_PRIORITY_1, PREF_AUTO_LOCK_SUPPORT_PRIORITY_2, PREF_AUTO_LOCK_SUPPORT_PRIORITY_3, PREF_AUTO_LOCK_SUPPORT_PRIORITY_4, PREF_AUTO_LOCK_SUPPORT_PRIORITY_5};
    private static final String[] BAN_KEYS = {PREF_AUTO_BAN_PRIORITY_1, PREF_AUTO_BAN_PRIORITY_2, PREF_AUTO_BAN_PRIORITY_3, PREF_AUTO_BAN_PRIORITY_4, PREF_AUTO_BAN_PRIORITY_5};
    private static final String[] ARENA_LOCK_KEYS = {PREF_AUTO_LOCK_ARENA_PRIORITY_1, PREF_AUTO_LOCK_ARENA_PRIORITY_2, PREF_AUTO_LOCK_ARENA_PRIORITY_3, PREF_AUTO_LOCK_ARENA_PRIORITY_4, PREF_AUTO_LOCK_ARENA_PRIORITY_5};
    private static final String[] ARENA_BAN_KEYS = {PREF_AUTO_BAN_ARENA_PRIORITY_1, PREF_AUTO_BAN_ARENA_PRIORITY_2, PREF_AUTO_BAN_ARENA_PRIORITY_3, PREF_AUTO_BAN_ARENA_PRIORITY_4, PREF_AUTO_BAN_ARENA_PRIORITY_5};
    private static final String[] SWAP_KEYS = {PREF_AUTO_SWAP_PRIORITY_1, PREF_AUTO_SWAP_PRIORITY_2, PREF_AUTO_SWAP_PRIORITY_3, PREF_AUTO_SWAP_PRIORITY_4, PREF_AUTO_SWAP_PRIORITY_5, PREF_AUTO_SWAP_PRIORITY_6, PREF_AUTO_SWAP_PRIORITY_7, PREF_AUTO_SWAP_PRIORITY_8, PREF_AUTO_SWAP_PRIORITY_9, PREF_AUTO_SWAP_PRIORITY_10};

    private static Preferences prefs;

    static {
        prefs = Preferences.userNodeForPackage(PreferenceManager.class);
    }

    // Persist each slot; remove the key when the value is null/empty so cleared picks don't linger.
    private static void putList(String[] keys, String[] values) {
        for (int i = 0; i < keys.length; i++) {
            String v = (values != null && i < values.length) ? values[i] : null;
            if (v != null && !v.isEmpty()) {
                prefs.put(keys[i], v);
            } else {
                prefs.remove(keys[i]);
            }
        }
    }

    private static String[] getList(String[] keys) {
        String[] out = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            out[i] = prefs.get(keys[i], null);
        }
        return out;
    }

    public static void setAutoLockTopPriority(String[] priority) {
        putList(TOP_KEYS, priority);
    }

    public static void setAutoLockJunglePriority(String[] priority) {
        putList(JUNGLE_KEYS, priority);
    }

    public static void setAutoLockMidPriority(String[] priority) {
        putList(MID_KEYS, priority);
    }

    public static void setAutoLockBotPriority(String[] priority) {
        putList(BOT_KEYS, priority);
    }

    public static void setAutoLockSupportPriority(String[] priority) {
        putList(SUPPORT_KEYS, priority);
    }

    public static String[] getAutoSwapTopPriority() {
        return getList(TOP_KEYS);
    }

    public static String[] getAutoSwapJunglePriority() {
        return getList(JUNGLE_KEYS);
    }

    public static String[] getAutoLockJunglePriority() {
        return getList(JUNGLE_KEYS);
    }

    public static String[] getAutoLockMidPriority() {
        return getList(MID_KEYS);
    }

    public static String[] getAutoLockBotPriority() {
        return getList(BOT_KEYS);
    }

    public static String[] getAutoLockSupportPriority() {
        return getList(SUPPORT_KEYS);
    }

    public static void setAutoBanPriority(String[] priority) {
        putList(BAN_KEYS, priority);
    }

    public static String[] getAutoBanPriority() {
        return getList(BAN_KEYS);
    }

    public static void setAutoLockArenaPriority(String[] priority) {
        putList(ARENA_LOCK_KEYS, priority);
    }

    public static String[] getAutoLockArenaPriority() {
        return getList(ARENA_LOCK_KEYS);
    }

    public static void setAutoBanArenaPriority(String[] priority) {
        putList(ARENA_BAN_KEYS, priority);
    }

    public static String[] getAutoBanArenaPriority() {
        return getList(ARENA_BAN_KEYS);
    }

    public static void setAutoSwapPriority(String[] priority) {
        putList(SWAP_KEYS, priority);
    }

    public static String[] getAutoSwapPriority() {
        return getList(SWAP_KEYS);
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

    // ---- Auto game-start loop (all opt-in, default OFF) ----

    private static void putBooleanFlushed(String key, boolean value) {
        prefs.putBoolean(key, value);
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAutoHonor(boolean value) {
        putBooleanFlushed(PREF_AUTO_HONOR, value);
    }

    public static boolean getAutoHonor() {
        return prefs.getBoolean(PREF_AUTO_HONOR, false);
    }

    public static void setAutoSkipScreens(boolean value) {
        putBooleanFlushed(PREF_AUTO_SKIP_SCREENS, value);
    }

    public static boolean getAutoSkipScreens() {
        return prefs.getBoolean(PREF_AUTO_SKIP_SCREENS, false);
    }

    public static void setGroupAutoQueue(boolean value) {
        putBooleanFlushed(PREF_GROUP_AUTO_QUEUE, value);
    }

    public static boolean getGroupAutoQueue() {
        return prefs.getBoolean(PREF_GROUP_AUTO_QUEUE, false);
    }

    public static void setSoloAutoQueue(boolean value) {
        putBooleanFlushed(PREF_SOLO_AUTO_QUEUE, value);
    }

    public static boolean getSoloAutoQueue() {
        return prefs.getBoolean(PREF_SOLO_AUTO_QUEUE, false);
    }

    public static void setAutoMinimize(boolean value) {
        putBooleanFlushed(PREF_AUTO_MINIMIZE, value);
    }

    public static boolean getAutoMinimize() {
        return prefs.getBoolean(PREF_AUTO_MINIMIZE, false);
    }

    // ---- On/off state for the remaining auto toggles (so every choice survives a restart) ----

    public static void setAutoAccept(boolean value) { putBooleanFlushed("autoAccept", value); }
    public static boolean getAutoAccept() { return prefs.getBoolean("autoAccept", false); }

    public static void setAutoDecline(boolean value) { putBooleanFlushed("autoDecline", value); }
    public static boolean getAutoDecline() { return prefs.getBoolean("autoDecline", false); }

    public static void setAutoSwap(boolean value) { putBooleanFlushed("autoSwapEnabled", value); }
    public static boolean getAutoSwap() { return prefs.getBoolean("autoSwapEnabled", false); }

    public static void setAutoReroll(boolean value) { putBooleanFlushed("autoReroll", value); }
    public static boolean getAutoReroll() { return prefs.getBoolean("autoReroll", false); }

    public static void setAutoLock(boolean value) { putBooleanFlushed("autoLockEnabled", value); }
    public static boolean getAutoLock() { return prefs.getBoolean("autoLockEnabled", false); }

    public static void setAutoBan(boolean value) { putBooleanFlushed("autoBanEnabled", value); }
    public static boolean getAutoBan() { return prefs.getBoolean("autoBanEnabled", false); }

    public static void setAutoLockArena(boolean value) { putBooleanFlushed("autoLockArenaEnabled", value); }
    public static boolean getAutoLockArena() { return prefs.getBoolean("autoLockArenaEnabled", false); }

    public static void setAutoBanArena(boolean value) { putBooleanFlushed("autoBanArenaEnabled", value); }
    public static boolean getAutoBanArena() { return prefs.getBoolean("autoBanArenaEnabled", false); }

    public static void setAutoBanCrowdFavorite(boolean value) { putBooleanFlushed("autoBanCrowdFavorite", value); }
    public static boolean getAutoBanCrowdFavorite() { return prefs.getBoolean("autoBanCrowdFavorite", false); }

    public static void setAutoBravery(boolean value) { putBooleanFlushed("autoBravery", value); }
    public static boolean getAutoBravery() { return prefs.getBoolean("autoBravery", false); }

    // ---- Phone notifications (ntfy.sh). Master defaults OFF; events default ON so once the user
    //      turns the master on and sets a topic, notifications just work. ----

    public static void setNotifyEnabled(boolean value) { putBooleanFlushed("notifyEnabled", value); }
    public static boolean getNotifyEnabled() { return prefs.getBoolean("notifyEnabled", false); }

    public static void setNotifyTopic(String value) {
        if (value == null || value.isBlank()) {
            prefs.remove("notifyTopic");
        } else {
            prefs.put("notifyTopic", value.trim());
        }
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getNotifyTopic() { return prefs.get("notifyTopic", ""); }

    public static void setNotifyMatchFound(boolean value) { putBooleanFlushed("notifyMatchFound", value); }
    public static boolean getNotifyMatchFound() { return prefs.getBoolean("notifyMatchFound", true); }

    public static void setNotifyChampPicked(boolean value) { putBooleanFlushed("notifyChampPicked", value); }
    public static boolean getNotifyChampPicked() { return prefs.getBoolean("notifyChampPicked", true); }

    public static void setNotifyChampPickedAram(boolean value) { putBooleanFlushed("notifyChampPickedAram", value); }
    public static boolean getNotifyChampPickedAram() { return prefs.getBoolean("notifyChampPickedAram", true); }

    public static void setNotifyChampSwapAram(boolean value) { putBooleanFlushed("notifyChampSwapAram", value); }
    public static boolean getNotifyChampSwapAram() { return prefs.getBoolean("notifyChampSwapAram", true); }

    public static void setNotifyChampBanned(boolean value) { putBooleanFlushed("notifyChampBanned", value); }
    public static boolean getNotifyChampBanned() { return prefs.getBoolean("notifyChampBanned", true); }

    public static void setNotifyOnlyWhenAway(boolean value) { putBooleanFlushed("notifyOnlyWhenAway", value); }
    public static boolean getNotifyOnlyWhenAway() { return prefs.getBoolean("notifyOnlyWhenAway", false); }

    public static void setNotifyIdleSeconds(int value) { prefs.putInt("notifyIdleSeconds", value); try { prefs.flush(); } catch (Exception e) { e.printStackTrace(); } }
    public static int getNotifyIdleSeconds() { return prefs.getInt("notifyIdleSeconds", 30); }

    public static void setNotifyHonor(boolean value) { putBooleanFlushed("notifyHonor", value); }
    public static boolean getNotifyHonor() { return prefs.getBoolean("notifyHonor", true); }

    public static void setNotifyReturnedToLobby(boolean value) { putBooleanFlushed("notifyReturnedToLobby", value); }
    public static boolean getNotifyReturnedToLobby() { return prefs.getBoolean("notifyReturnedToLobby", true); }

    public static void setNotifyAutoQueue(boolean value) { putBooleanFlushed("notifyAutoQueue", value); }
    public static boolean getNotifyAutoQueue() { return prefs.getBoolean("notifyAutoQueue", true); }

    public static void setNotifyGameStarting(boolean value) { putBooleanFlushed("notifyGameStarting", value); }
    public static boolean getNotifyGameStarting() { return prefs.getBoolean("notifyGameStarting", true); }

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
