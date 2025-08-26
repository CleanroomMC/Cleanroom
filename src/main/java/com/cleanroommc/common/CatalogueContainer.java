package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;

public class CatalogueContainer extends DummyModContainer {
    public CatalogueContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "catalogue";
        meta.name = "Catalogue";
        meta.description = "Updates Forge's mod list with a new and improved design";
        meta.version = "1.0.0";
        meta.authorList = Arrays.asList("MrCrayfish", "RuiXuqi");
        meta.logoFile = "/catalogue_icon.png";
        meta.logoBlur = false;
        meta.iconFile = "/catalogue_icon.png";
        meta.iconBlur = false;
        meta.backgroundFile = "/catalogue_background.png";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
