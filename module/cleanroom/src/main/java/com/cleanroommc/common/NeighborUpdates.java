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

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

public final class NeighborUpdates {

    private static final int MAX_DEPTH = 64;
    private static final int MAX_CHAINED_UPDATES = 65536;
    private static final Map<World, State> STATES = new WeakHashMap<>();

    private NeighborUpdates() { }

    public static boolean begin(World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        State state = stateFor(world);
        if (state.chainedUpdates >= MAX_CHAINED_UPDATES) {
            if (state.chainedUpdates == MAX_CHAINED_UPDATES) {
                state.chainedUpdates++;
                FMLLog.log.warn("Neighbor update chain exceeded {} updates at {} (source {}); skipping further updates in this chain",
                    MAX_CHAINED_UPDATES, pos, blockIn.getRegistryName()
                );
            }
            return false;
        }
        if (state.depth >= MAX_DEPTH) {
            if (state.chainedUpdates + state.deferred.size() < MAX_CHAINED_UPDATES) {
                state.deferred.addLast(new DeferredUpdate(pos.toImmutable(), blockIn, fromPos.toImmutable()));
            }
            return false;
        }
        state.chainedUpdates++;
        state.depth++;
        return true;
    }

    public static void end(World world) {
        State state = stateFor(world);
        try {
            if (state.depth == 1) {
                DeferredUpdate deferred;
                while ((deferred = state.deferred.pollFirst()) != null) {
                    world.neighborChanged(deferred.pos(), deferred.blockIn(), deferred.fromPos());
                }
            }
        } finally {
            state.depth--;
            if (state.depth == 0) {
                state.chainedUpdates = 0;
                state.deferred.clear();
            }
        }
    }

    private static State stateFor(World world) {
        synchronized (STATES) {
            return STATES.computeIfAbsent(world, k -> new State());
        }
    }

    private static final class State {

        final ArrayDeque<DeferredUpdate> deferred = new ArrayDeque<>();

        int depth;
        int chainedUpdates;

    }

    private record DeferredUpdate(BlockPos pos, Block blockIn, BlockPos fromPos) { }

}
