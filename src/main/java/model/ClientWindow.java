package model;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.awt.Rectangle;

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

        boolean GetWindowRect(HWND hWnd, RECT rect);

        boolean IsIconic(HWND hWnd);

        HWND GetForegroundWindow();

        short GetAsyncKeyState(int vKey);

        int GetWindowLong(HWND hWnd, int nIndex);

        int SetWindowLong(HWND hWnd, int nIndex, int dwNewLong);
    }

    private static final int GWL_EXSTYLE = -20;
    private static final int WS_EX_LAYERED = 0x00080000;
    private static final int WS_EX_TRANSPARENT = 0x00000020;

    /**
     * Toggle mouse click-through on one of OUR overlay windows. When on, clicks pass through to the
     * window below (the client); when off, the window receives clicks normally. Keeps WS_EX_LAYERED
     * set (Java uses it for the per-pixel-transparent overlay) and only flips WS_EX_TRANSPARENT.
     */
    public static void setClickThrough(HWND hwnd, boolean on) {
        if (hwnd == null) {
            return;
        }
        try {
            int ex = U.I.GetWindowLong(hwnd, GWL_EXSTYLE) | WS_EX_LAYERED;
            ex = on ? (ex | WS_EX_TRANSPARENT) : (ex & ~WS_EX_TRANSPARENT);
            U.I.SetWindowLong(hwnd, GWL_EXSTYLE, ex);
        } catch (Throwable ignored) {
            // user32 unavailable / non-Windows: leave the window as-is
        }
    }

    /** True if the given virtual-key is physically down right now (global, no focus needed). Used to
     *  detect the overlay drag chord. vk = Windows virtual-key code (which matches Java's VK_* for the
     *  keys we care about: modifiers, letters, digits, function/caps keys). */
    public static boolean isKeyDown(int vk) {
        try {
            return (U.I.GetAsyncKeyState(vk) & 0x8000) != 0;
        } catch (Throwable t) {
            return false;
        }
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

    /**
     * The client's visible top-level window handle, or null if the client is not running / not
     * visible. Same enumerate-and-match-PID rule as {@link #minimize()}; used by the overlay to
     * track the client's geometry. Returns the first visible LeagueClientUx-owned window.
     */
    public static HWND findHwnd() {
        final HWND[] out = {null};
        try {
            U.I.EnumWindows((hwnd, data) -> {
                if (U.I.IsWindowVisible(hwnd)) {
                    IntByReference pidRef = new IntByReference();
                    U.I.GetWindowThreadProcessId(hwnd, pidRef);
                    if (isLeagueClient(pidRef.getValue())) {
                        out[0] = hwnd;
                        return false; // stop at the first match
                    }
                }
                return true;
            }, null);
        } catch (Throwable t) {
            return null;
        }
        return out[0];
    }

    /** Screen bounds (physical pixels) of the given window, or null if degenerate/unavailable. */
    public static Rectangle boundsOf(HWND hwnd) {
        if (hwnd == null) {
            return null;
        }
        RECT r = new RECT();
        if (!U.I.GetWindowRect(hwnd, r)) {
            return null;
        }
        int w = r.right - r.left;
        int h = r.bottom - r.top;
        if (w <= 0 || h <= 0) {
            return null;
        }
        return new Rectangle(r.left, r.top, w, h);
    }

    public static boolean isMinimizedOf(HWND hwnd) {
        return hwnd != null && U.I.IsIconic(hwnd);
    }

    /** True if the given window (or a window handle equal to it) is the OS foreground window. */
    public static boolean isForeground(HWND hwnd) {
        if (hwnd == null) {
            return false;
        }
        HWND fg = U.I.GetForegroundWindow();
        return fg != null && Pointer.nativeValue(hwnd.getPointer()) == Pointer.nativeValue(fg.getPointer());
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
