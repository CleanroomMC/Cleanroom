package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

public class CleanroomContainer extends DummyModContainer {
    public CleanroomContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "cleanroom";
        meta.name = "Cleanroom";
        meta.description = """
                Cleanroom is a 1.12.2 Forge fork, providing newer toolchain, new APIs and 99% compatibility.
                Our plan is to make 1.12.2 up-to-date with latest toolchain while adding more feature to Forge & vanilla, so we don't need to care about higher versions' X point release * Y Mod API headache.
                """;
        meta.version = CleanroomVersion.VERSION;
        meta.authorList = Arrays.asList("LexManos", "cpw", "fry", "Rongmario", "kappa_maintainer", "Li");
        meta.updateJSON = "https://download.cleanroommc.com/api/forge";
        meta.logoFile = "/cleanroom_banner.png";
        meta.iconFile = "/cleanroom_icon.png";
        meta.backgroundFile = "/cleanroom_background.png";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
    
    @Override
    public URL getUpdateUrl() {
        try{
            return new URI("https://download.cleanroommc.com/api/forge").toURL();
        } catch (MalformedURLException | URISyntaxException ignored){}
        return null;
    }
}
