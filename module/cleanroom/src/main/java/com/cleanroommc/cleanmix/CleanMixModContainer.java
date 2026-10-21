/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanmix;

import com.google.common.eventbus.EventBus;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CleanMixModContainer extends DummyModContainer {

    public CleanMixModContainer() {
        super(loadMetadata());
    }

    public static File location() {
        try {
            String resource = Mixin.class.getName().replace('.', '/') + ".class";
            URL url = Mixin.class.getClassLoader().getResource(resource);
            String path = url.getPath();
            int index = path.indexOf('!');
            if (index != -1) {
                path = path.substring(0, index);
            }
            return new File(new URI(path)).getAbsoluteFile();
        } catch (Exception ignore) { }
        return FMLSanityChecker.fmlLocation;
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

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

}
