/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.test;

import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the roll seam handling used by {@link ForgeHooks#setEntityRoll}. Isolated because loading
 * {@link ForgeHooks} runs its static initializers, which touch items and registries.
 */
@ForgeTestRunner.Isolated
public class RollSeamTest
{
    @BeforeAll
    public static void setUp()
    {
        Loader.instance();
        Bootstrap.register();
    }

    @Test
    public void testPositiveToNegativeCrossing()
    {
        // 179 -> -179 is a +2 step across the seam, so prev moves down a full turn
        assertEquals(-183.0F, ForgeHooks.adjustPrevRollAcrossSeam(177.0F, 179.0F, -179.0F), "Crossing +180 should shift prevRoll by -360");
    }

    @Test
    public void testNegativeToPositiveCrossing()
    {
        // -179 -> 179 is a -2 step across the seam, so prev moves up a full turn
        assertEquals(183.0F, ForgeHooks.adjustPrevRollAcrossSeam(-177.0F, -179.0F, 179.0F), "Crossing -180 should shift prevRoll by +360");
    }

    @Test
    public void testNoCrossingInsideRange()
    {
        assertEquals(10.0F, ForgeHooks.adjustPrevRollAcrossSeam(10.0F, 10.0F, 20.0F), "Small positive step should leave prevRoll unchanged");
        assertEquals(20.0F, ForgeHooks.adjustPrevRollAcrossSeam(20.0F, 20.0F, 10.0F), "Small negative step should leave prevRoll unchanged");
        assertEquals(-90.0F, ForgeHooks.adjustPrevRollAcrossSeam(-90.0F, -90.0F, 89.0F), "Step below 180 should leave prevRoll unchanged");
        assertEquals(5.0F, ForgeHooks.adjustPrevRollAcrossSeam(5.0F, 5.0F, 5.0F), "No change should leave prevRoll unchanged");
    }

    @Test
    public void testExactHalfTurnEdge()
    {
        // A difference of exactly 180 is ambiguous and is not treated as a seam crossing
        assertEquals(-90.0F, ForgeHooks.adjustPrevRollAcrossSeam(-90.0F, -90.0F, 90.0F), "A +180 step should not shift prevRoll");
        assertEquals(90.0F, ForgeHooks.adjustPrevRollAcrossSeam(90.0F, 90.0F, -90.0F), "A -180 step should not shift prevRoll");
        assertEquals(0.0F, ForgeHooks.adjustPrevRollAcrossSeam(0.0F, 0.0F, -180.0F), "A -180 step to the seam should not shift prevRoll");
        // Just past 180 is a crossing
        assertEquals(360.0F, ForgeHooks.adjustPrevRollAcrossSeam(0.0F, -180.0F, 0.5F), "A step just over +180 should shift prevRoll by +360");
        assertEquals(-360.0F, ForgeHooks.adjustPrevRollAcrossSeam(0.0F, 179.5F, -1.0F), "A step just over -180 should shift prevRoll by -360");
    }

    @Test
    public void testUnwrappedLastRoll()
    {
        // Per-frame writers advanced roll and prevRoll past the seam without wrapping; setRoll then wraps
        assertEquals(-180.0F, ForgeHooks.adjustPrevRollAcrossSeam(540.0F, 541.0F, -178.0F), "A -719 step should shift prevRoll by -720");
        assertEquals(180.0F, ForgeHooks.adjustPrevRollAcrossSeam(-540.0F, -541.0F, 178.0F), "A +719 step should shift prevRoll by +720");
        assertEquals(-178.0F, ForgeHooks.adjustPrevRollAcrossSeam(182.0F, 181.0F, -179.0F), "A -360 step should shift prevRoll by -360");
        // Exact multiples of a half turn past a full turn keep the no-shift edge
        assertEquals(180.0F, ForgeHooks.adjustPrevRollAcrossSeam(540.0F, 540.0F, 0.0F), "A -540 step should shift prevRoll by -360");
    }

    @Test
    public void testWrapDegrees()
    {
        assertEquals(-180.0F, MathHelper.wrapDegrees(180.0F), "180 should wrap to -180");
        assertEquals(-180.0F, MathHelper.wrapDegrees(-180.0F), "-180 should stay -180");
        assertEquals(-180.0F, MathHelper.wrapDegrees(540.0F), "540 should wrap to -180");
        assertEquals(-180.0F, MathHelper.wrapDegrees(-540.0F), "-540 should wrap to -180");
        assertEquals(179.0F, MathHelper.wrapDegrees(179.0F), "179 should stay 179");
        assertEquals(-179.0F, MathHelper.wrapDegrees(181.0F), "181 should wrap to -179");
        assertEquals(179.0F, MathHelper.wrapDegrees(-181.0F), "-181 should wrap to 179");
        assertEquals(10.0F, MathHelper.wrapDegrees(370.0F), "370 should wrap to 10");
    }
}
