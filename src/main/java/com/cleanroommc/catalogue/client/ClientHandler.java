package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class ClientHandler {
    @SubscribeEvent
    public static void onOpenScreen(@NotNull GuiOpenEvent event) {
        //noinspection deprecation
        if (CatalogueConfig.enableMod && event.getGui() instanceof GuiModList) {
            event.setGui(new CatalogueModListScreen(Minecraft.getMinecraft().currentScreen));
        }
    }
}
