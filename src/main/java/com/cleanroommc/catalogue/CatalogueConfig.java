package com.cleanroommc.catalogue;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = CatalogueConstants.MOD_ID)
@Mod.EventBusSubscriber(modid = CatalogueConstants.MOD_ID)
public class CatalogueConfig {
    @Config.RequiresMcRestart
    @Config.Comment({
            "The list of library mods' mod ids.",
            "They will have grey names in the mod list."
    })
    @Config.LangKey("catalogue.config.library_list")
    public static String[] libraryList = new String[]{
            "minecraft",
            "forge",
            "FML",
            "mcp",
            "cleanroom",
            "configanytime",
            "mixinbooter",
            "fugue",
            "scalar"
    };

    @Config.RequiresMcRestart
    @Config.Comment({
            "The list of ignored dependencies' mod ids.",
            "They will not be displayed when searching for dependencies/dependants."
    })
    @Config.LangKey("catalogue.config.ignored_dependencies_list")
    public static String[] ignoredDependenciesList = new String[]{
            "minecraft",
            "forge",
            "FML",
            "mcp",
            "cleanroom"
    };

    @Config.RequiresMcRestart
    @Config.Comment({
            "Whether limit the size of mods' banners."
    })
    @Config.LangKey("catalogue.config.enable_banner_limit")
    public static boolean enableBannerLimit = false;

    @Config.RequiresMcRestart
    @Config.Comment({
            "The maximum of banner's width. Will not work if Enable Banner Limit is set false."
    })
    @Config.LangKey("catalogue.config.banner_max_width")
    @Config.RangeInt(min = 0)
    public static int bannerMaxWidth = 1280;

    @Config.RequiresMcRestart
    @Config.Comment({
            "The maximum of banner's height. Will not work if Enable Banner Limit is set false."
    })
    @Config.LangKey("catalogue.config.banner_max_height")
    @Config.RangeInt(min = 0)
    public static int bannerMaxHeight = 256;

    @Config.RequiresMcRestart
    @Config.Comment({
            "Whether limit the size of mods' icons."
    })
    @Config.LangKey("catalogue.config.enable_icon_limit")
    public static boolean enableIconLimit = false;

    @Config.RequiresMcRestart
    @Config.Comment({
            "The maximum of icon's width and height. Will not work if Enable Icon Limit is set false."
    })
    @Config.LangKey("catalogue.config.icon_max_width_height")
    @Config.RangeInt(min = 0)
    public static int iconMaxWidthHeight = 256;

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(CatalogueConstants.MOD_ID)) {
            ConfigManager.sync(CatalogueConstants.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
