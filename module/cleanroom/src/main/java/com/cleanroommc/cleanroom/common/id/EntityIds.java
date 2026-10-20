/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.function.IntPredicate;

/**
 * Entity network ids ({@code Entity.entityId}). Vanilla counts up forever; after 2^31 entities (a busy server
 * spawning ~1000 entities a second gets there in about 25 days) the counter wraps and hands out ids that live
 * entities still use, so two entities share an id on clients. Ids stay {@code int}: after a wrap the counter skips
 * ids still in use.
 */
public final class EntityIds {

    private static volatile boolean wrapped;

    private EntityIds() { }

    public static synchronized int allocate(int counter) {
        return next(counter, EntityIds::inUseOnServer);
    }

    /*
     * The id to use given the counter's current value; the caller stores {@code result + 1} as the new counter (it may
     * overflow, which the next call treats as a wrap).
     */
    public static int next(int counter, IntPredicate inUse) {
        int id = counter;
        if (id < 0) {
            wrapped = true;
            id = 0;
        }
        if (wrapped) {
            while (inUse.test(id)) {
                id = id == Integer.MAX_VALUE ? 0 : id + 1;
            }
        }
        return id;
    }

    /* Whether a loaded server world has an entity with this id. */
    public static boolean inUseOnServer(int id) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return false;
        }
        for (WorldServer world : server.worlds) {
            if (world != null && world.getEntityByID(id) != null) {
                return true;
            }
        }
        return false;
    }

}
