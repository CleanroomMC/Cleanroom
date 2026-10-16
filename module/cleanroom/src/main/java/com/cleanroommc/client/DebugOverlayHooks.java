/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class DebugOverlayHooks {

    private DebugOverlayHooks() { }

    /* "Biomes O' Plenty: ..." for a biomesoplenty biome, "Minecraft: ..." for vanilla, the namespace itself for an unknown mod. */
    public static String formatBiome(Biome biome) {
        ResourceLocation name = biome.getRegistryName();
        if (name == null) {
            return biome.getBiomeName();
        }
        ModContainer mod = Loader.instance().getIndexedModList().get(name.getNamespace());
        return biome.getBiomeName() + " (" + biomeOwnerName + ")"; // Cache biomeOwnerName in Biome?
    }

}
