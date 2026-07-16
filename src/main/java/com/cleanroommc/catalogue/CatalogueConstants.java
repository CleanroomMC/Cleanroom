package com.cleanroommc.catalogue;

import net.minecraft.util.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CatalogueConstants {
    public static final String MOD_ID = "catalogue";
    public static final String MOD_NAME = "Catalogue";

    // ERROR only for critical, unrecoverable failures
    // WARN for non-fatal issues or recoverable states
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static ResourceLocation resource(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
