package model;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.List;

/**
 * Presence detection for the "Only notify when away" gate. Uses native Windows calls (JNA, already a
 * dependency) so the check is cheap enough to run inline on the socket thread:
 *
 *  - GetLastInputInfo: ms since the last keyboard/mouse input. Under the threshold => the user is
 *    actively using the PC, so hold the notification.
 *  - SHQueryUserNotificationState: Windows' own "should I show a toast" state. A fullscreen app /
 *    game / presentation reads as busy (e.g. a fullscreen video the user is watching) => hold,
 *    UNLESS the fullscreen window is League itself (a game that auto-launched while the user is
 *    away should still notify).
 *
 * Known limitation: a WINDOWED video (small YouTube in a browser) can't be told apart from being
 * away - no input, not fullscreen - so it will still notify. Only fullscreen video is suppressed.
 *
 * Fails OPEN: if any native call is unavailable, allow the notification rather than silently
 * swallowing every alert. Mirrors {@link ClientWindow}'s direct-dll-mapping style so availability
 * never depends on jna-platform's higher-level wrappers.
 */
public final class WindowsPresence {
    // QUERY_USER_NOTIFICATION_STATE values that mean a fullscreen surface is in the foreground.
    private static final int QUNS_BUSY = 2;
    private static final int QUNS_RUNNING_D3D_FULL_SCREEN = 3;
    private static final int QUNS_PRESENTATION_MODE = 4;

    private interface U extends StdCallLibrary {
        U I = Native.load("user32", U.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean GetLastInputInfo(LASTINPUTINFO plii);

        HWND GetForegroundWindow();

        int GetWindowThreadProcessId(HWND hWnd, IntByReference pid);
    }

    private interface K extends StdCallLibrary {
        K I = Native.load("kernel32", K.class, W32APIOptions.DEFAULT_OPTIONS);

        int GetTickCount();
    }

    private interface Shell extends StdCallLibrary {
        Shell I = Native.load("shell32", Shell.class, W32APIOptions.DEFAULT_OPTIONS);

        int SHQueryUserNotificationState(IntByReference pquns);
    }

    @Structure.FieldOrder({"cbSize", "dwTime"})
    public static class LASTINPUTINFO extends Structure {
        public int cbSize;
        public int dwTime;
    }

    private WindowsPresence() {
    }

    /** True when a notification should be sent now: the user has been idle at least thresholdMs and
     *  is not sitting in a non-League fullscreen surface (e.g. a video). */
    public static boolean shouldNotify(long thresholdMs) {
        try {
            if (idleMillis() < thresholdMs) {
                return false; // actively using keyboard / mouse
            }
            if (fullscreenBusy() && !foregroundIsLeague()) {
                return false; // watching a fullscreen video / using a fullscreen app
            }
            return true;
        } catch (Throwable t) {
            System.out.println("[Presence] check failed, allowing notify: " + t);
            return true; // fail open
        }
    }

    static long idleMillis() {
        LASTINPUTINFO lii = new LASTINPUTINFO();
        lii.cbSize = lii.size();
        if (!U.I.GetLastInputInfo(lii)) {
            return Long.MAX_VALUE; // can't tell -> treat as away
        }
        // Both are 32-bit unsigned tick counts; int subtraction handles wraparound, mask to unsigned.
        int diff = K.I.GetTickCount() - lii.dwTime;
        return diff & 0xFFFFFFFFL;
    }

    private static boolean fullscreenBusy() {
        IntByReference state = new IntByReference();
        int hr = Shell.I.SHQueryUserNotificationState(state);
        if (hr != 0) {
            return false; // call failed -> don't suppress on this basis
        }
        int s = state.getValue();
        return s == QUNS_BUSY || s == QUNS_RUNNING_D3D_FULL_SCREEN || s == QUNS_PRESENTATION_MODE;
    }

    private static boolean foregroundIsLeague() {
        HWND fg = U.I.GetForegroundWindow();
        if (fg == null) {
            return false;
        }
        IntByReference pidRef = new IntByReference();
        U.I.GetWindowThreadProcessId(fg, pidRef);
        int pid = pidRef.getValue();
        if (pid <= 0) {
            return false;
        }
        return ProcessHandle.of(pid)
                .flatMap(h -> h.info().command())
                .map(c -> {
                    String l = c.toLowerCase();
                    return l.contains("league of legends")
                            || l.contains("leagueclient")
                            || l.contains("riotclient");
                })
                .orElse(false);
    }
}
