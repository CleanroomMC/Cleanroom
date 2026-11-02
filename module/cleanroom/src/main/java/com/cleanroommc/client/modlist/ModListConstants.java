/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.modlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.ResourceLocation;

public final class ModListConstants {

    public static final String OWNER_MOD_ID = "cleanroom";

    // ERROR only for critical, unrecoverable failures
    // WARN for non-fatal issues or recoverable states
    public static final Logger LOG = LoggerFactory.getLogger("ModList");

    private ModListConstants() { }

    public static ResourceLocation resource(String name) {
        return new ResourceLocation(OWNER_MOD_ID, name);
    }

}
