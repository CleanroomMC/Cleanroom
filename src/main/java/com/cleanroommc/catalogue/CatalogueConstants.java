package com.cleanroommc.catalogue;

import net.minecraft.util.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CatalogueConstants {
    public static final String MOD_ID = "catalogue";
    public static final String MOD_NAME = "Catalogue";
    public static final Logger LOG = LoggerFactory.getLogger(CatalogueConstants.MOD_NAME);

    public static ResourceLocation resource(String name) {
        return new ResourceLocation(CatalogueConstants.MOD_ID, name);
    }
}
