package net.minecraftforge.common;

import net.minecraftforge.common.config.Config;
import zone.rong.mixinbooter.Tags;

@Config(modid = ForgeVersion.MOD_ID, name = ForgeVersion.MOD_ID + "_early")
public class ForgeEarlyConfig {

    public static int OPENGL_VERSION_MAJOR = 4;
    public static int OPENGL_VERSION_MINOR = 6;
    public static boolean OPENGL_COMPAT_PROFILE = true;

    //TODO : make CATEGORY?
    //TODO : make the config display name lowcase?
    
    public static boolean WINDOW_START_MAXIMIZED = false;

    @Config.Comment("Should exclusive fullscreen mode replaced with borderless fullscreen mode")
    public static boolean WINDOW_BORDERLESS_REPLACES_FULLSCREEN = false;

    @Config.Comment("Windows-only - should borderless window have height increased by 1 to solve flickering on un-focusing")
    public static boolean WINDOW_BORDERLESS_WINDOWS_COMPATIBILITY = true;

    @Config.Comment("Enable KHR_debug in the OpenGL context for advanced debugging capabilities")
    public static boolean OPENGL_DEBUG_CONTEXT = false;

    @Config.Comment({
        "Disable the Fugue/Scalar coremod presence check at startup.",
        "When enabled, the patch mod check is skipped and always returns true.",
        "When disabled, checks whether Fugue (https://www.curseforge.com/minecraft/mc-mods/fugue)",
        "and Scalar (https://www.curseforge.com/minecraft/mc-mods/scalar-legacy) are present.",
        "Only disable this if you know what you are doing!"
    })
    public static boolean DISABLE_PATCH_MOD_CHECK = false;

    public static boolean CUSTOM_BUILT_IN_MOD_VERSION = false;
    public static String CONFIG_ANY_TIME_VERSION = "3.0";
    public static String MIXIN_BOOTER_VERSION = Tags.VERSION;

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
        @Config.Comment("Disable flash after loaded")
        public boolean DISABLE_FLASH_AFTER_LOADED = false;
    }
}
