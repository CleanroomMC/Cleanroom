package com.cleanroommc.client.chat.suggestion;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public final class TabCompleteHooks {

    // Tab completion runs ICommand#checkPermission on every candidate, and some commands message the sender when refusing
    private static EntityPlayerMP completing;

    public static List<String> getTabCompletions(MinecraftServer server, EntityPlayerMP player, CPacketTabComplete packet) {
        completing = player;
        try {
            return server.getTabCompletions(player, packet.getMessage(), packet.getTargetBlock(), packet.hasTargetBlock());
        } finally {
            completing = null;
        }
    }

    public static boolean isCompleting(EntityPlayerMP player) {
        return completing == player;
    }

    private TabCompleteHooks() { }

}
