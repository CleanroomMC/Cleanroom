package com.cleanroommc.client.windows;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsTheme {
    private static final String PERSONALIZE_KEY =
            "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    private static final String APPS_USE_LIGHT_THEME = "AppsUseLightTheme";

    private WindowsTheme() {
    }

    private static boolean isAppsUseLightTheme() {
        try {
            if (!Advapi32Util.registryValueExists(
                    WinReg.HKEY_CURRENT_USER,
                    PERSONALIZE_KEY,
                    APPS_USE_LIGHT_THEME)) {
                return true;
            }

            int value = Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_CURRENT_USER,
                    PERSONALIZE_KEY,
                    APPS_USE_LIGHT_THEME);

            return value != 0;
        } catch (Throwable t) {
            return true;
        }
    }

    public static boolean isAppsDarkTheme() {
        return !isAppsUseLightTheme();
    }
}
