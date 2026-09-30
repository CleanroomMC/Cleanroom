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
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * MouseTurnEvent is fired when the mouse is about to rotate the local player's view, just before
 * {@link Entity#turn(float, float)} is called from {@code EntityRenderer.updateCameraAndRender}.
 *
 * <p>{@link #yaw} and {@link #pitch} are exactly the values vanilla passes to {@link Entity#turn(float, float)}:
 * already scaled by mouse sensitivity ({@code (sensitivity * 0.6 + 0.2)^3 * 8}), with the pitch already
 * multiplied by the invert-mouse sign, and, when smooth camera is active, already filtered.
 * {@link Entity#turn(float, float)} itself multiplies both by 0.15 to obtain degrees.
 *
 * <p>This event is fired once per rendered frame while {@code inGameHasFocus && Display.isActive()},
 * including frames with a zero mouse delta. {@link #frameDelta} is the duration of the current frame
 * in seconds, see {@link MouseTurnHooks#getFrameDeltaSeconds()}.
 *
 * <p>Handlers may call {@link Entity#turn(float, float)} on the player themselves; the hook sits only at the
 * {@code EntityRenderer} call sites, so doing so does not re-fire this event.
 *
 * <p>This event is {@link Cancelable}. If this event is canceled, the player is not turned this frame.
 *
 * <p>This event does not have a result. {@link HasResult}
 *
 * <p>This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
@Cancelable
public class MouseTurnEvent extends Event {

    private final EntityPlayerSP player;
    private float yaw;
    private float pitch;
    private final float frameDelta;

    public MouseTurnEvent(EntityPlayerSP player, float yaw, float pitch, float frameDelta) {
        this.player = player;
        this.yaw = yaw;
        this.pitch = pitch;
        this.frameDelta = frameDelta;
    }

    public EntityPlayerSP getPlayer() {
        return this.player;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getFrameDeltaSeconds() {
        return this.frameDelta;
    }

}
