package com.cleanroommc.client.modlist;

import com.cleanroommc.client.modlist.screen.ModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ModListEventHandler {
    @SubscribeEvent
    public static void onOpenScreen(GuiOpenEvent event) {
        if (!ModListConfig.enable) return;
        if (event.getGui() instanceof GuiModList) {
            event.setGui(new ModListScreen(Minecraft.getMinecraft().currentScreen));
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ModListConstants.OWNER_MOD_ID)) {
            ConfigManager.sync(ModListConfig.class);
        }
    }
}
