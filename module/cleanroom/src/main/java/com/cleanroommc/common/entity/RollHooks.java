/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common.entity;

import com.cleanroommc.util.CleanroomLog;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Hooks behind the entity roll axis, {@link Entity#roll} and {@link Entity#prevRoll}.
 */
public final class RollHooks {

    /**
     * Implementation of {@link Entity#setRoll(float)}.
     *
     * <p>Non-finite values are logged and discarded; finite values are wrapped to [-180, 180) with
     * {@link MathHelper#wrapDegrees(float)}.
     *
     * <p>{@link Entity#prevRoll} is moved by whole turns when the step from the old roll exceeds 180 degrees
     * (see {@link #adjustPrevRollAcrossSeam(float, float, float)}) so that interpolation takes the short
     * way round.
     *
     * <p>On the logical server the value is also written to {@link Entity#ROLL} for tracking clients.
     *
     * @param entity The entity to roll
     * @param roll The new roll in degrees
     */
    public static void setEntityRoll(Entity entity, float roll) {
        if (!Float.isFinite(roll)) {
            CleanroomLog.get().warn("Invalid roll {} for {}, discarding", roll, entity);
            return;
        }
        roll = MathHelper.wrapDegrees(roll);
        float last = entity.roll;
        entity.roll = roll;
        entity.prevRoll = adjustPrevRollAcrossSeam(entity.prevRoll, last, roll);
        if (entity.world != null && !entity.world.isRemote) {
            entity.getDataManager().set(Entity.ROLL, roll);
        }
    }

    /**
     * Applies a roll value received from the server through {@link Entity#ROLL}.
     *
     * <p>Called at tick time from {@link Entity#tickRoll()}, right after the previous-tick snapshot.
     * Values that arrive before the first tick are applied directly by {@link Entity#onRollSynced(float)}.
     *
     * <p>While {@code ticksExisted} (read before the tick increments it) is at most 1, {@link Entity#prevRoll}
     * is snapped to the new value. This covers object spawns whose metadata packet lands just after
     * the first tick, so a freshly tracked entity does not animate from 0.
     *
     * @param entity The client-side entity
     * @param roll The synced roll in degrees
     */
    public static void applySyncedEntityRoll(Entity entity, float roll) {
        setEntityRoll(entity, roll);
        if (entity.ticksExisted <= 1) {
            entity.prevRoll = entity.roll;
        }
    }

    /**
     * Keeps roll interpolation continuous across the &plusmn;180 seam.
     *
     * <p>If the step from {@code lastRoll} to {@code newRoll} is larger than 180 degrees in either
     * direction, the previous value is moved by as many whole turns as bring the step back within
     * &plusmn;180; otherwise it is returned unchanged. A step of exactly &plusmn;180 is not shifted.
     *
     * <p>{@code lastRoll} may be unwrapped, e.g. after per-frame writers advanced it past the seam.
     *
     * @param prevRoll The previous-tick roll used for interpolation
     * @param lastRoll The roll before the update
     * @param newRoll The roll after the update
     * @return The adjusted previous-tick roll
     */
    public static float adjustPrevRollAcrossSeam(float prevRoll, float lastRoll, float newRoll) {
        double delta = (double) newRoll - lastRoll;
        if (delta > 180.0D) {
            return (float) (prevRoll + Math.ceil((delta - 180.0D) / 360.0D) * 360.0D);
        }
        if (delta < -180.0D) {
            return (float) (prevRoll + Math.floor((delta + 180.0D) / 360.0D) * 360.0D);
        }
        return prevRoll;
    }

    private RollHooks() { }

}
