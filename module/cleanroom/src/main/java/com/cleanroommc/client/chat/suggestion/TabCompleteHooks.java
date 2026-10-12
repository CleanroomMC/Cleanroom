/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.chat.suggestion;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public final class TabCompleteHooks {

    // Tab completion runs ICommand#checkPermission on every candidate, and some commands message the sender when refusing
    private static EntityPlayerMP completing;

    private TabCompleteHooks() { }

    public static List<String> getTabCompletions(MinecraftServer server, EntityPlayerMP player,
        CPacketTabComplete packet
    ) {
        completing = player;
        try {
            return server.getTabCompletions(player, packet.getMessage(), packet.getTargetBlock(),
                packet.hasTargetBlock()
            );
        } finally {
            completing = null;
        }
    }

    public static boolean isCompleting(EntityPlayerMP player) {
        return completing == player;
    }

}
