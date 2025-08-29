package com.cleanroommc.catalogue;

import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
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
    public static String[] libraryList = new String[] {
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

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(CatalogueConstants.MOD_ID)) {
            ConfigManager.sync(CatalogueConstants.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
