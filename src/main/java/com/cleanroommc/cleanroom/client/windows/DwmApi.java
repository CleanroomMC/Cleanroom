/// Copyright under MIT https://github.com/LemonCaramel/Mica
package com.cleanroommc.cleanroom.client.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import net.minecraftforge.common.ForgeEarlyConfig;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.NativeType;
import java.util.List;

public interface DwmApi extends Library {

    /* DWM API */
    DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);
    int INT_SIZE = 4;

    /* BOOL */
    int BOOL_FALSE = 0;
    int BOOL_TRUE = 1;

    /* DWMWINDOWATTRIBUTE */
    int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    int DWMWA_WINDOW_CORNER_PREFERENCE = 33;
    int DWMWA_BORDER_COLOR = 34;
    int DWMWA_CAPTION_COLOR = 35;
    int DWMWA_TEXT_COLOR = 36;
    int DWMWA_SYSTEMBACKDROP_TYPE = 38;

    /* DWM_SYSTEMBACKDROP_TYPE */
    enum DWM_SYSTEMBACKDROP_TYPE {
        DWMSBT_AUTO(0, "auto"), // 0 Auto
        DWMSBT_NONE(1, "none"), // 1 None
        DWMSBT_MAINWINDOW(2, "mica"), // 2 Mica
        DWMSBT_TRANSIENTWINDOW(3, "acrylic"), // 3 Acrylic
        DWMSBT_TABBEDWINDOW(4, "tabbed"); // 4 Tabbed

        public final int value;
        public final String translate;

        DWM_SYSTEMBACKDROP_TYPE(int value, String translate) {
            this.value = value;
            this.translate = translate;
        }

        public static DWM_SYSTEMBACKDROP_TYPE fromInt(int v) {
            for (var e : values()) {
                if (e.value == v) return e;
            }
            throw new IllegalArgumentException("Unknown value: " + v);
        }
    }

    /* DWM_WINDOW_CORNER_PREFERENCE */
    enum DWM_WINDOW_CORNER_PREFERENCE {
        DWMWCP_DEFAULT(0, "default"), // 0
        DWMWCP_DONOTROUND(1, "do_not_round"), // 1
        DWMWCP_ROUND(2, "round"), // 2
        DWMWCP_ROUNDSMALL(3, "round_small"); // 3

        public final int value;
        public final String translate;

        DWM_WINDOW_CORNER_PREFERENCE(int value, String translate) {
            this.value = value;
            this.translate = translate;
        }

        public static DWM_WINDOW_CORNER_PREFERENCE fromInt(int v) {
            for (var e : values()) {
                if (e.value == v) return e;
            }
            throw new IllegalArgumentException("Unknown value: " + v);
        }
    }

    /* DWMWA_BORDER_COLOR_OPTION */
    int DWMWA_COLOR_NONE = 0xFFFFFFFE;
    int DWMWA_COLOR_DEFAULT = 0xFFFFFFFF;

    @NativeType("HRESULT") // Err
    int DwmSetWindowAttribute(
            HWND hwnd,
            int dwAttribute,
            PointerType pvAttribute,
            int cbAttribute
    );

    static void updateDwm(final boolean fullscreen, final long window) {
        // Check build number
        if (!WindowsProperties.checkCompatibility()) {
            return;
        }

        final HWND hwnd = new HWND(Pointer.createConstant(GLFWNativeWin32.glfwGetWin32Window(window)));
        if (fullscreen) {
            DwmApi.disableWindowEffect(hwnd);
            return;
        }

        // DWMWA_USE_IMMERSIVE_DARK_MODE
        final boolean useImmersiveDarkMode = ForgeEarlyConfig.MODERN_WINDOWS_STYLES.USE_IMMERSIVE_DARK_MODE && WindowsTheme.isAppsDarkTheme();
        INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, new IntByReference(useImmersiveDarkMode ? BOOL_TRUE : BOOL_FALSE), INT_SIZE);

        // DWMWA_SYSTEMBACKDROP_TYPE
        if (WindowsProperties.buildNumber >= WindowsProperties.BACKDROP_BUILD_NUM) {
            final DWM_SYSTEMBACKDROP_TYPE systemBackdropType = DWM_SYSTEMBACKDROP_TYPE.fromInt(ForgeEarlyConfig.MODERN_WINDOWS_STYLES.SYSTEM_BACKDROP_TYPE);
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, new IntByReference(systemBackdropType.ordinal()), INT_SIZE);
        }

        // DWMWA_WINDOW_CORNER_PREFERENCE
        final DWM_WINDOW_CORNER_PREFERENCE windowCorner = DWM_WINDOW_CORNER_PREFERENCE.fromInt(ForgeEarlyConfig.MODERN_WINDOWS_STYLES.WINDOW_CORNER);
        INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, new IntByReference(windowCorner.ordinal()), INT_SIZE);

        // DWMWA_BORDER_COLOR
        if (ForgeEarlyConfig.MODERN_WINDOWS_STYLES.USE_DEFAULT_BORDER) {
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, new IntByReference(DWMWA_COLOR_DEFAULT), INT_SIZE);
        } else if (ForgeEarlyConfig.MODERN_WINDOWS_STYLES.HIDE_WINDOW_BORDER) {
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, new IntByReference(DWMWA_COLOR_NONE), INT_SIZE);
        } else {
            final int borderColor = convert(ForgeEarlyConfig.MODERN_WINDOWS_STYLES.BORDER_COLOR);
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, new IntByReference(borderColor), INT_SIZE);
        }

        // DWMWA_CAPTION_COLOR
        if (ForgeEarlyConfig.MODERN_WINDOWS_STYLES.USE_DEFAULT_CAPTION) {
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, new IntByReference(DWMWA_COLOR_DEFAULT), INT_SIZE);
        } else {
            final int captionColor = convert(ForgeEarlyConfig.MODERN_WINDOWS_STYLES.CAPTION_COLOR);
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, new IntByReference(captionColor), INT_SIZE);
        }

        // DWMWA_TEXT_COLOR
        if (ForgeEarlyConfig.MODERN_WINDOWS_STYLES.USE_DEFAULT_TEXT) {
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, new IntByReference(DWMWA_COLOR_DEFAULT), INT_SIZE);
        } else {
            final int textColor = convert(ForgeEarlyConfig.MODERN_WINDOWS_STYLES.TEXT_COLOR);
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, new IntByReference(textColor), INT_SIZE);
        }
    }

    static void disableWindowEffect(final HWND hwnd) {
        // ... DWMWA_USE_IMMERSIVE_DARK_MODE
        if (WindowsProperties.buildNumber >= WindowsProperties.BACKDROP_BUILD_NUM) {
            INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, new IntByReference(DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO.ordinal()), INT_SIZE);
        }
        INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, new IntByReference(DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT.ordinal()), INT_SIZE);
        INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, new IntByReference(DWMWA_COLOR_DEFAULT), INT_SIZE);
        INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, new IntByReference(DWMWA_COLOR_DEFAULT), INT_SIZE);
        INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, new IntByReference(DWMWA_COLOR_DEFAULT), INT_SIZE);
    }

    private static int convert(final int color) {
        // Ignore Alpha
        final int b = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int r = color & 0xFF;

        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /* ======================================== */

    @NativeType("HRESULT") // Err
    int DwmExtendFrameIntoClientArea(
            HWND hwnd,
            MARGINS pMarInset
    );

    class MARGINS extends Structure {

        public int cxLeftWidth;
        public int cxRightWidth;
        public int cyTopHeight;
        public int cyBottomHeight;

        public MARGINS(final int cxLeftWidth, final int cxRightWidth, final int cyTopHeight, final int cyBottomHeight) {
            this.cxLeftWidth = cxLeftWidth;
            this.cxRightWidth = cxRightWidth;
            this.cyTopHeight = cyTopHeight;
            this.cyBottomHeight = cyBottomHeight;
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight");
        }
    }
}