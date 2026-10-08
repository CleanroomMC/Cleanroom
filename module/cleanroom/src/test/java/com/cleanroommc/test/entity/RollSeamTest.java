/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.entity;

import com.cleanroommc.common.entity.RollHooks;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.MathHelper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the roll seam handling used by {@link RollHooks#setEntityRoll}.
 */
public class RollSeamTest {

    @Test
    public void testPositiveToNegativeCrossing() {
        // 179 -> -179 is a +2 step across the seam, so prev moves down a full turn
        assertThat(RollHooks.adjustPrevRollAcrossSeam(177.0F, 179.0F, -179.0F)).as("Crossing +180 should shift prevRoll by -360").isEqualTo(-183.0F);
    }

    @Test
    public void testNegativeToPositiveCrossing() {
        // -179 -> 179 is a -2 step across the seam, so prev moves up a full turn
        assertThat(RollHooks.adjustPrevRollAcrossSeam(-177.0F, -179.0F, 179.0F)).as("Crossing -180 should shift prevRoll by +360").isEqualTo(183.0F);
    }

    @Test
    public void testNoCrossingInsideRange() {
        assertThat(RollHooks.adjustPrevRollAcrossSeam(10.0F, 10.0F, 20.0F)).as("Small positive step should leave prevRoll unchanged").isEqualTo(10.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(20.0F, 20.0F, 10.0F)).as("Small negative step should leave prevRoll unchanged").isEqualTo(20.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(-90.0F, -90.0F, 89.0F)).as("Step below 180 should leave prevRoll unchanged").isEqualTo(-90.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(5.0F, 5.0F, 5.0F)).as("No change should leave prevRoll unchanged").isEqualTo(5.0F);
    }

    @Test
    public void testExactHalfTurnEdge() {
        // A difference of exactly 180 is ambiguous and is not treated as a seam crossing
        assertThat(RollHooks.adjustPrevRollAcrossSeam(-90.0F, -90.0F, 90.0F)).as("A +180 step should not shift prevRoll").isEqualTo(-90.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(90.0F, 90.0F, -90.0F)).as("A -180 step should not shift prevRoll").isEqualTo(90.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(0.0F, 0.0F, -180.0F)).as("A -180 step to the seam should not shift prevRoll").isEqualTo(0.0F);
        // Just past 180 is a crossing
        assertThat(RollHooks.adjustPrevRollAcrossSeam(0.0F, -180.0F, 0.5F)).as("A step just over +180 should shift prevRoll by +360").isEqualTo(360.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(0.0F, 179.5F, -1.0F)).as("A step just over -180 should shift prevRoll by -360").isEqualTo(-360.0F);
    }

    @Test
    public void testUnwrappedLastRoll() {
        // Per-frame writers advanced roll and prevRoll past the seam without wrapping; setRoll then wraps
        assertThat(RollHooks.adjustPrevRollAcrossSeam(540.0F, 541.0F, -178.0F)).as("A -719 step should shift prevRoll by -720").isEqualTo(-180.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(-540.0F, -541.0F, 178.0F)).as("A +719 step should shift prevRoll by +720").isEqualTo(180.0F);
        assertThat(RollHooks.adjustPrevRollAcrossSeam(182.0F, 181.0F, -179.0F)).as("A -360 step should shift prevRoll by -360").isEqualTo(-178.0F);
        // Exact multiples of a half turn past a full turn keep the no-shift edge
        assertThat(RollHooks.adjustPrevRollAcrossSeam(540.0F, 540.0F, 0.0F)).as("A -540 step should shift prevRoll by -360").isEqualTo(180.0F);
    }

    @Test
    public void testWrapDegrees() {
        assertThat(MathHelper.wrapDegrees(180.0F)).as("180 should wrap to -180").isEqualTo(-180.0F);
        assertThat(MathHelper.wrapDegrees(-180.0F)).as("-180 should stay -180").isEqualTo(-180.0F);
        assertThat(MathHelper.wrapDegrees(540.0F)).as("540 should wrap to -180").isEqualTo(-180.0F);
        assertThat(MathHelper.wrapDegrees(-540.0F)).as("-540 should wrap to -180").isEqualTo(-180.0F);
        assertThat(MathHelper.wrapDegrees(179.0F)).as("179 should stay 179").isEqualTo(179.0F);
        assertThat(MathHelper.wrapDegrees(181.0F)).as("181 should wrap to -179").isEqualTo(-179.0F);
        assertThat(MathHelper.wrapDegrees(-181.0F)).as("-181 should wrap to 179").isEqualTo(179.0F);
        assertThat(MathHelper.wrapDegrees(370.0F)).as("370 should wrap to 10").isEqualTo(10.0F);
    }

}
