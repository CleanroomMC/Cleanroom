package net.minecraftforge.common;

import net.minecraftforge.common.config.Config;

@Config(modid = ForgeVersion.MOD_ID, name = ForgeVersion.MOD_ID + "_early")
public class ForgeEarlyConfig {

    public static int OPENGL_VERSION_MAJOR = 4;
    public static int OPENGL_VERSION_MINOR = 6;
    public static boolean OPENGL_COMPAT_PROFILE = true;

    public static boolean RAW_INPUT = true;

    //TODO : make CATEGORY?
    //TODO : make the config display name lowcase?
    
    public static boolean WINDOW_START_MAXIMIZED = false;
    public static boolean WINDOW_START_FOCUSED = true;
    public static boolean WINDOW_START_ICONIFIED = false;

    @Config.Comment("Enable this when you want to run the game on Wayland (WARNING: GLFW's wayland support is buggy)")
    public static boolean FORCE_WAYLAND = false;

    public static boolean WINDOW_CENTERED = true;

    @Config.Comment("Should the window have decorations (titlebar, border, close button)")
    public static boolean WINDOW_DECORATED = true;

    @Config.Comment("Should exclusive fullscreen mode replaced with borderless fullscreen mode")
    public static boolean WINDOW_BORDERLESS_REPLACES_FULLSCREEN = false;

    @Config.Comment("Windows-only - should borderless window have height increased by 1 to solve flickering on un-focusing")
    public static boolean WINDOW_BORDERLESS_WINDOWS_COMPATIBILITY = true;

    @Config.Comment("Enable KHR_debug in the OpenGL context for advanced debugging capabilities")
    public static boolean OPENGL_DEBUG_CONTEXT = false;

    @Config.Comment("Make the framebuffer use the sRGB color space")
    public static boolean OPENGL_SRGB_CONTEXT = false;

    @Config.Comment("Make the framebuffer double-buffered (will cause visual artifacts if disabled)")
    public static boolean OPENGL_DOUBLEBUFFER = true;

    @Config.Comment("Enable GL_KHR_no_error to use faster driver code, but which can cause memory corruption in case of OpenGL errors")
    public static boolean OPENGL_CONTEXT_NO_ERROR = false;

    public static boolean DECORATED = true;

    public static boolean INPUT_INVERT_WHEEL = false;

    public static double INPUT_SCROLL_SPEED = 1.0;

    @Config.Comment("Allow text character input when Ctrl+Left Alt are pressed (disables special escape code handling for this combination of keys)")
    public static boolean INPUT_CTRL_ALT_TEXT = false;

    @Config.Comment("Allows AltGr use in Ctrl+key special key combinations (disables text character input handling when AltGr is pressed)")
    public static boolean INPUT_ALTGR_ESCAPE_CODES = false;

    @Config.Comment("Linux-only - change the X11 class name, which is used by your window manager to identify the running application")
    public static String X11_CLASS_NAME = "minecraft";

    @Config.Comment("OSX-only - identifier used to save and restore the window position and size")
    public static String COCOA_FRAME_NAME = "minecraft";
    @Config.Comment("OSX-only - Specifies whether to use full resolution framebuffers on Retina displays")
    public static boolean COCOA_RETINA_FRAMEBUFFER = false;

    public static boolean CUSTOM_BUILT_IN_MOD_VERSION = false;
    public static String CONFIG_ANY_TIME_VERSION = "3.0";
    public static String MIXIN_BOOTER_VERSION = "10.7";

    @Config.Comment("""
            Mods in this list have one or more of the problems list below:
            - Integrated into Cleanroom so shouldn't load it from mods
            - Too invasive or hacky that it is unpatchable
            - Have a better replacement that compats e.g. JEID vs REID
            """)
    public static String[] LOADING_PLUGIN_BLACKLIST = new String[] {
            "com.cleanroommc.configanytime.ConfigAnytimePlugin",
            "zone.rong.mixinbooter.MixinBooterPlugin",
            "ilib.asm.Loader",
            "org.dimdev.jeid.JEIDLoadingPlugin",
            "lain.mods.skins.init.forge.asm.Plugin",
            "advancedshader.core.Core",
            "net.shadowfacts.forgelin.preloader.ForgelinPlugin",
            "com.cleanroommc.relauncher.CleanroomEntrypoint",
    };

    public static CategoryOpenAlContext OPENAL_CONTEXT = new CategoryOpenAlContext();
    public static class CategoryOpenAlContext{
        @Config.Comment("Enable HRTF sound support")
        public boolean ENABLE_HRTF = false;
    }

    public static ModernWindowsStyles MODERN_WINDOWS_STYLES = new ModernWindowsStyles();
    public static class ModernWindowsStyles {
        @Config.Comment("Enable immersive dark mode, it will follow windows theme settings")
        public boolean USE_IMMERSIVE_DARK_MODE = true;
        @Config.Comment("Backdrop type [0:auto 1:none 2:mica 3:acrylic 4:tabbed]")
        public int SYSTEM_BACKDROP_TYPE;
        @Config.Comment("Window corner type [0:default 1:do_not_round 2:round 3:round_small]")
        public int WINDOW_CORNER;
        @Config.Comment("Use default corner")
        public boolean USE_DEFAULT_BORDER = true;
        @Config.Comment("Hide window border")
        public boolean HIDE_WINDOW_BORDER;
        @Config.Comment("Window border color [RGB hex in int]")
        public int BORDER_COLOR;
        @Config.Comment("Use default caption color")
        public boolean USE_DEFAULT_CAPTION = true;
        @Config.Comment("Window caption color [RGB hex in int]")
        public int CAPTION_COLOR;
        @Config.Comment("Use default text color")
        public boolean USE_DEFAULT_TEXT = true;
        @Config.Comment("Window text color [RGB hex in int]")
        public int TEXT_COLOR;
        @Config.Comment("Use cleanroom embed loading progressbar handling")
        public boolean UPDATE_WINDOWS_TASKBAR_PROGRESS = true;
    }
}
