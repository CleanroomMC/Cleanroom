package net.minecraftforge.common;

import net.minecraftforge.common.config.Config;

@Config(modid = ForgeVersion.MOD_ID, name = ForgeVersion.MOD_ID + "_early")
public class ForgeEarlyConfig {

    public static boolean RAW_INPUT = true;

    //TODO : make CATEGORY?
    //TODO : make the config display name lowcase?
    
    public static boolean WINDOW_START_MAXIMIZED = false;
    public static boolean WINDOW_START_FOCUSED = true;
    public static boolean WINDOW_START_ICONIFIED = false;

    @Config.Comment("Should the window have decorations (titlebar, border, close button)")
    public static boolean WINDOW_DECORATED = true;

    @Config.Comment("Enable KHR_debug in the OpenGL context for advanced debugging capabilities")
    public static boolean OPENGL_DEBUG_CONTEXT = false;

    @Config.Comment("Make the framebuffer use the sRGB color space")
    public static boolean OPENGL_SRGB_CONTEXT = false;

    @Config.Comment("Make the framebuffer double-buffered (will cause visual artifacts if disabled)")
    public static boolean OPENGL_DOUBLEBUFFER = true;

    @Config.Comment("Enable GL_KHR_no_error to use faster driver code, but which can cause memory corruption in case of OpenGL errors")
    public static boolean OPENGL_CONTEXT_NO_ERROR = false;

    @Config.Comment("Only use this if you have custom glfw natives")
    public static boolean FORCE_WAYLAND = false;

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

    public static String CONFIG_ANY_TIME_VERSION = "3.0";

    public static String[] LOADING_PLUGIN_BLACKLIST = new String[] { "com.cleanroommc.configanytime.ConfigAnytimePlugin", "zone.rong.mixinbooter.MixinBooterPlugin"};

    public static CategoryOpenglContext OPENAL_CONTEXT = new CategoryOpenglContext();
    public static class CategoryOpenglContext{
        @Config.Comment("Enable HRTF sound support")
        public boolean ENABLE_HRTF = false;
    }


}
