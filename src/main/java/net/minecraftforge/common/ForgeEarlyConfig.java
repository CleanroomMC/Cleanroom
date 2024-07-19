package net.minecraftforge.common;

import net.minecraftforge.common.config.Config;

@Config(modid = ForgeVersion.MOD_ID, name = ForgeVersion.MOD_ID + "_early")
public class ForgeEarlyConfig {
    public static boolean RAW_INPUT = true;
    public static boolean WINDOW_START_MAXIMIZED = false;
    public static boolean WINDOW_START_FOCUSED = true;
    public static boolean WINDOW_START_ICONIFIED = false;
    public static boolean WINDOW_DECORATED = true;
    public static boolean OPENGL_DEBUG_CONTEXT = false;
    public static boolean OPENGL_SRGB_CONTEXT = false;
    public static boolean OPENGL_DOUBLEBUFFER = true;
    public static boolean OPENGL_CONTEXT_NO_ERROR = false;
    public static boolean INPUT_INVERT_WHEEL = false;
    public static double INPUT_SCROLL_SPEED = 1.0;
    public static String X11_CLASS_NAME = "minecraft";
    public static String COCOA_FRAME_NAME = "minecraft";
    public static String CONFIG_ANY_TIME_VERSION = "3.0";
}
