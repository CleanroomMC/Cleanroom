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

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.Map;

public final class DebugOverlayHooks {

    private static final Map<Biome, String> BIOME_DISPLAY_NAMES = new Reference2ObjectOpenHashMap<>();

    private DebugOverlayHooks() { }

    /* "... (Biomes O' Plenty)" for a biomesoplenty biome, "... (Minecraft)" for vanilla, the namespace itself for an unknown mod. */
    public static String formatBiome(Biome biome) {
        return BIOME_DISPLAY_NAMES.computeIfAbsent(biome, DebugOverlayHooks::computeBiomeDisplayName);
    }

    private static String computeBiomeDisplayName(Biome biome) {
        ResourceLocation name = biome.getRegistryName();
        if (name == null) {
            return biome.getBiomeName();
        }
        ModContainer mod = Loader.instance().getIndexedModList().get(name.getNamespace());
        String owner = mod != null ? mod.getName() : name.getNamespace();
        return biome.getBiomeName() + " (" + owner + ")";
    }

}
