/// Copyright under MIT https://github.com/LemonCaramel/Mica
package com.cleanroommc.client.windows;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;

public class WindowsProperties {
    public static final int MINIMUM_BUILD_NUM = 22000;
    public static final int BACKDROP_BUILD_NUM = 22621;

    public static int majorVersion = Integer.MIN_VALUE;
    public static int buildNumber = Integer.MIN_VALUE;

    public static long handle = Long.MIN_VALUE;

    /**
     * Check compatibility.
     *
     * @return if {@code true}, it is compatible.
     */
    public static boolean checkCompatibility() {
        return (WindowsProperties.majorVersion >= 10 && WindowsProperties.buildNumber >= WindowsProperties.MINIMUM_BUILD_NUM);
    }

    /** Converts an operating-system window handle to JNA's HWND wrapper. */
    public static HWND hwnd(long nativeWindow) {
        return new HWND(Pointer.createConstant(nativeWindow));
    }
}
