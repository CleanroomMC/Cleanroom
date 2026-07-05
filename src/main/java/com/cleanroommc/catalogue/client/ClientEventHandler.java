package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

/// @author MrCrayfish
public final class ClientEventHandler {
    @SubscribeEvent
    public static void onOpenScreen(GuiOpenEvent event) {
        if (!CatalogueConfig.enable) return;
        if (event.getGui() instanceof GuiModList modList) {
            GuiScreen parent;
            try {
                Field field = GuiModList.class.getDeclaredField("mainMenu");
                field.setAccessible(true);
                parent = (GuiScreen) field.get(modList);
            } catch (Exception e) {
                CatalogueConstants.LOG.error("Failed to get field mainMenu from GuiModList", e);
                parent = Minecraft.getMinecraft().currentScreen;
            }
            event.setGui(new CatalogueModListScreen(parent));
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(CatalogueConstants.MOD_ID)) {
            ConfigManager.sync(CatalogueConstants.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
