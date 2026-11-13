/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;

import java.util.ArrayDeque;
import java.util.EnumSet;

public final class CascadingWorldGen {

    private static final ThreadLocal<
        EnumSet<
            Decorate.EventType>> ACTIVE_DECORATE = ThreadLocal.withInitial(() -> EnumSet.noneOf(Decorate.EventType.class
    ));
    private static final ArrayDeque<Chunk> DEFERRED_POPULATE = new ArrayDeque<>();

    private static int populateDepth;

    private CascadingWorldGen() { }

    public static boolean beginPopulate(Chunk chunk, boolean alreadyPopulating) {
        if (alreadyPopulating) {
            if (!DEFERRED_POPULATE.contains(chunk)) {
                DEFERRED_POPULATE.addLast(chunk);
            }
            return false;
        }
        populateDepth++;
        return true;
    }

    public static void endPopulate(IChunkGenerator generator) {
        try {
            if (populateDepth == 1) {
                Chunk deferred;
                while ((deferred = DEFERRED_POPULATE.pollFirst()) != null) {
                    if (deferred.isLoaded()) {
                        deferred.populate(deferred.getWorld().getChunkProvider(), generator);
                    }
                }
            }
        } finally {
            populateDepth--;
            if (populateDepth == 0) {
                DEFERRED_POPULATE.clear();
            }
        }
    }

    public static boolean beginDecorate(Decorate.EventType type) {
        return ACTIVE_DECORATE.get().add(type);
    }

    public static void endDecorate(Decorate.EventType type) {
        ACTIVE_DECORATE.get().remove(type);
    }

}
