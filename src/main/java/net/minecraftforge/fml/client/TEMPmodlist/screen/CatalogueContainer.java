package net.minecraftforge.fml.client.TEMPmodlist.screen;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;

// A dummy mod only displayed in list
public class CatalogueContainer extends DummyModContainer {
    public CatalogueContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "catalogue";
        meta.name = "Catalogue";
        meta.description = "Updates Forge's mod list with a new and improved design";
        meta.version = "1.0.0";
        meta.authorList = Arrays.asList("MrCrayfish", "RuiXuqi");
        meta.url = "https://github.com/RuiXuqi/Catalogue-Vintage";
        meta.issueTrackerUrl = "https://github.com/RuiXuqi/Catalogue-Vintage/issues";
        meta.credits = "Hatsondogs for creating icons";
        meta.license = "MIT";
        meta.logoFile = "/catalogue_icon.png";
        meta.logoBlur = false;
        meta.iconFile = "/catalogue_icon.png";
        meta.iconBlur = false;
        meta.iconItem = "minecraft:diamond_sword";
        meta.backgroundFile = "/catalogue_background.png";
    }
}
