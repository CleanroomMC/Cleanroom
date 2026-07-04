package com.cleanroommc.cleanmix;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import org.spongepowered.asm.mixin.Mixin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CleanMixModContainer extends DummyModContainer {

    public static File location() {
        try {
            URL url = Mixin.class.getProtectionDomain().getCodeSource().getLocation();
            return new File(url.toURI()).getAbsoluteFile();
        } catch (Exception e) {
            return FMLSanityChecker.fmlLocation;
        }
    }

    public CleanMixModContainer() {
        super(loadMetadata());
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    private static ModMetadata loadMetadata() {
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("name", "CleanMix");
        fallbackData.put("version", "0.2.11");

        InputStream inputStream = CleanMixModContainer.class.getClassLoader().getResourceAsStream("cleanmix.info");
        try {
            return MetadataCollection.from(inputStream, "cleanmix.info").getMetadataForId("cleanmix", fallbackData);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) { }
            }
        }
    }

}
