/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.input;

import com.cleanroommc.client.input.mouse.MouseTurnEvent;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;

/**
 * Hooks called from patched Minecraft input code.
 */
public final class InputHooks {

    private InputHooks() { }

    /**
     * Called from the {@code EntityRenderer} turn call sites. Fires {@link MouseTurnEvent} and, unless it is
     * canceled, turns the player by the event's yaw and pitch.
     *
     * @param player The local player
     * @param yaw The yaw delta vanilla was about to pass to {@link EntityPlayerSP#turn(float, float)}
     * @param pitch The pitch delta vanilla was about to pass to {@link EntityPlayerSP#turn(float, float)}
     */
    public static void onMouseTurn(EntityPlayerSP player, float yaw, float pitch) {
        MouseTurnEvent event = new MouseTurnEvent(player, yaw, pitch);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            player.turn(event.getYaw(), event.getPitch());
        }
    }

}
