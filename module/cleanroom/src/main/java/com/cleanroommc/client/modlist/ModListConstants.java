package com.cleanroommc.client.modlist;

import net.minecraft.util.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModListConstants {
    public static final String OWNER_MOD_ID = "cleanroom";

    // ERROR only for critical, unrecoverable failures
    // WARN for non-fatal issues or recoverable states
    public static final Logger LOG = LoggerFactory.getLogger("ModList");

    public static ResourceLocation resource(String name) {
        return new ResourceLocation(OWNER_MOD_ID, name);
    }
}
