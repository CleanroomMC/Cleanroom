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

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;

/**
 * Hooks behind {@link MouseTurnEvent} at the {@code EntityRenderer} turn call sites.
 */
public final class MouseTurnHooks {

    /** Length of one game tick in seconds. Vanilla's {@code Timer} runs at 20 ticks per second. */
    private static final float SECONDS_PER_TICK = 0.05F;

    /**
     * Fires {@link MouseTurnEvent} and, unless it is canceled, turns the player by the event's yaw and pitch.
     *
     * <p>The event's frame delta is vanilla's own reading, {@link Minecraft#getTickLength()}, converted from
     * ticks to seconds. It is updated once per frame in {@code Minecraft.runGameLoop}, before rendering.
     *
     * @param player The local player
     * @param yaw The yaw delta vanilla was about to pass to {@link EntityPlayerSP#turn(float, float)}
     * @param pitch The pitch delta vanilla was about to pass to {@link EntityPlayerSP#turn(float, float)}
     */
    public static void onMouseTurn(EntityPlayerSP player, float yaw, float pitch) {
        float frameDelta = Minecraft.getMinecraft().getTickLength() * SECONDS_PER_TICK;
        MouseTurnEvent event = new MouseTurnEvent(player, yaw, pitch, frameDelta);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            player.turn(event.getYaw(), event.getPitch());
        }
    }

    private MouseTurnHooks() { }

}
