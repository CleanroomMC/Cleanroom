package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class ClientHandler {
    @SubscribeEvent
    public static void onOpenScreen(@NotNull GuiOpenEvent event) {
        //noinspection deprecation
        if (event.getGui() instanceof GuiModList) {
            event.setGui(new CatalogueModListScreen(Minecraft.getMinecraft().currentScreen));
        }
    }
}
