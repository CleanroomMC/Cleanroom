package com.cleanroommc.client.modlist;

import com.cleanroommc.client.modlist.screen.ModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

public final class ModListEventHandler {
    @SubscribeEvent
    public static void onOpenScreen(GuiOpenEvent event) {
        if (!ModListConfig.enable) return;
        if (event.getGui() instanceof GuiModList modList) {
            GuiScreen parent;
            try {
                Field field = GuiModList.class.getDeclaredField("mainMenu");
                field.setAccessible(true);
                parent = (GuiScreen) field.get(modList);
            } catch (Exception e) {
                ModListConstants.LOG.error("Failed to get field mainMenu from GuiModList", e);
                parent = Minecraft.getMinecraft().currentScreen;
            }
            event.setGui(new ModListScreen(parent));
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ModListConstants.OWNER_MOD_ID)) {
            ConfigManager.sync(ModListConfig.class);
        }
    }
}
