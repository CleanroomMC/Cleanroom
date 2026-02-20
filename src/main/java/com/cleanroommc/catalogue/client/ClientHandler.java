package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/// @author MrCrayfish
public class ClientHandler {
    @SubscribeEvent
    public static void onOpenScreen(@NotNull GuiOpenEvent event) {
        if (!CatalogueConfig.enable) return;
        if (event.getGui() instanceof GuiModList screen) {
            event.setGui(new CatalogueModListScreen(screen.getParentScreen()));
        }
    }
}
