package com.cleanroommc.client.modlist;

import net.minecraftforge.common.config.Config;

@Config(modid = ModListConstants.OWNER_MOD_ID, name = "catalogue")
public final class ModListConfig {
    @Config.Comment({
            "Whether to enable the mod list.",
            "Disabling it stops Cleanroom from redirecting Forge's mod list calls."
    })
    @Config.LangKey("cleanroom.config.enable")
    public static boolean enable = true;

    @Config.RequiresMcRestart
    @Config.Comment({
            "The list of library mods' mod ids.",
            "They will have grey names in the mod list."
    })
    @Config.LangKey("cleanroom.config.library_list")
    public static String[] libraryList = new String[]{
            "forge",
            "FML",
            "mcp",
            "cleanroom",
            "cleanmix",
            "configanytime",
            "mixinbooter",
            "fugue",
            "scalar",
            "kirino_ecs",
            "kirino_engine",
            "kirino_gl"
    };

    @Config.RequiresMcRestart
    @Config.Comment({
            "The list of ignored dependencies' mod ids.",
            "They will not be displayed when searching for dependencies/dependants."
    })
    @Config.LangKey("cleanroom.config.ignored_dependencies_list")
    public static String[] ignoredDependenciesList = new String[]{
            "minecraft",
            "forge",
            "FML",
            "mcp",
            "cleanroom"
    };

    @Config.RequiresMcRestart
    @Config.Comment({
            "The list of mod ids that should always use the default item icon.",
            "They will not have random-picked item icons to avoid crashes."
    })
    @Config.LangKey("cleanroom.config.force_default_icon_list")
    public static String[] forceDefaultIconList = new String[]{};
}
