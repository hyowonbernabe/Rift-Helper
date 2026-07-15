package model;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Minimize the League client exactly like clicking the window's "-" button: post
 * WM_SYSCOMMAND / SC_MINIMIZE so it routes through the client's own minimize handler (restores
 * normally from the taskbar). Used by Auto Minimize (DND) after each automated action.
 *
 * Deliberately dumb per user: find the client's visible top-level window, minimize it. No loop, no
 * focus juggling, no restore. (Earlier tries via /riotclient/ux-minimize corrupted the render, and
 * ShowWindow SW_MINIMIZE did not behave like the button; this mimics the button precisely.)
 */
public class ClientWindow {
    private static final String LEAGUE_IMAGE = "leagueclientux.exe";
    private static final int WM_SYSCOMMAND = 0x0112;
    private static final int SC_MINIMIZE = 0xF020;

    /** Only the user32 calls we need, mapped directly so availability never depends on jna-platform. */
    private interface U extends StdCallLibrary {
        U I = Native.load("user32", U.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean EnumWindows(WNDENUMPROC cb, Pointer data);

        int GetWindowThreadProcessId(HWND hWnd, IntByReference lpdwProcessId);

        boolean IsWindowVisible(HWND hWnd);

        boolean PostMessage(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);
    }

    public static void minimize() {
        try {
            U.I.EnumWindows((hwnd, data) -> {
                if (U.I.IsWindowVisible(hwnd)) {
                    IntByReference pidRef = new IntByReference();
                    U.I.GetWindowThreadProcessId(hwnd, pidRef);
                    if (isLeagueClient(pidRef.getValue())) {
                        U.I.PostMessage(hwnd, WM_SYSCOMMAND, new WPARAM(SC_MINIMIZE), new LPARAM(0));
                    }
                }
                return true; // keep enumerating
            }, null);
        } catch (Throwable t) {
            System.out.println("[AutoMinimize] unavailable: " + t);
        }
    }

    private static boolean isLeagueClient(int pid) {
        if (pid <= 0) {
            return false;
        }
        return ProcessHandle.of(pid)
                .flatMap(h -> h.info().command())
                .map(c -> c.toLowerCase().endsWith(LEAGUE_IMAGE))
                .orElse(false);
    }
}
