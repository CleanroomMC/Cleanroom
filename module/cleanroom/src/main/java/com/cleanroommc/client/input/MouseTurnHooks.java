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

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;

/**
 * Hooks behind {@link MouseTurnEvent}: the frame clock and the {@code EntityRenderer} turn call sites.
 */
public final class MouseTurnHooks {

    // Frame clock, advanced once per rendered frame from Minecraft.runGameLoop before RenderTickEvent START
    private static final float DEFAULT_FRAME_DELTA_SECONDS = 1.0F / 60.0F;
    private static final float MAX_FRAME_DELTA_SECONDS = 0.1F;
    private static long lastFrameNanos = -1;
    private static float frameDeltaSeconds = DEFAULT_FRAME_DELTA_SECONDS;

    /**
     * Advances the frame clock. Called once per rendered frame, right before
     * {@link net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent} {@code START} is fired.
     *
     * <p>The first frame reads {@link #DEFAULT_FRAME_DELTA_SECONDS}; later frames are clamped to
     * [0, {@link #MAX_FRAME_DELTA_SECONDS}] to absorb hitches such as world loads and dimension changes.
     */
    public static void beginFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos >= 0) {
            frameDeltaSeconds = MathHelper.clamp((now - lastFrameNanos) / 1.0E9F, 0.0F, MAX_FRAME_DELTA_SECONDS);
        } else {
            frameDeltaSeconds = DEFAULT_FRAME_DELTA_SECONDS;
        }
        lastFrameNanos = now;
    }

    /**
     * Returns the frame clock's current reading.
     *
     * @return the duration of the current rendered frame in seconds, as measured by {@link #beginFrame()}
     */
    public static float getFrameDeltaSeconds() {
        return frameDeltaSeconds;
    }

    /**
     * Fires {@link MouseTurnEvent} and, unless it is canceled, turns the player by the event's yaw and pitch.
     *
     * @param player The local player
     * @param yaw The yaw delta vanilla was about to pass to {@link EntityPlayerSP#turn(float, float)}
     * @param pitch The pitch delta vanilla was about to pass to {@link EntityPlayerSP#turn(float, float)}
     */
    public static void onMouseTurn(EntityPlayerSP player, float yaw, float pitch) {
        MouseTurnEvent event = new MouseTurnEvent(player, yaw, pitch, frameDeltaSeconds);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            player.turn(event.getYaw(), event.getPitch());
        }
    }

    private MouseTurnHooks() { }

}
