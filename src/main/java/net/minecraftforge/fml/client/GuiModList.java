package net.minecraftforge.fml.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.modlist.screen.CatalogueModListScreen;

@Deprecated
public class GuiModList extends CatalogueModListScreen {
    public GuiModList(GuiScreen mainMenu) {
        super(mainMenu);
    }
}
