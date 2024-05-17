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

package net.minecraftforge.client;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;

/**
 * Provide custom coloring for items which contains fluids.
 * that contain fluids. It is used to color the fluid inside the item container.
 */
public class FluidContainerColorer implements IItemColor
{
    /**
     * This method is called by the Minecraft rendering system to get the color of the item.
     *
     * @param stack The ItemStack of the item being rendered.
     * @param tintIndex The index of the tint to apply. In this case, 1 represents the color of the fluid inside the item.
     * @return The color to apply to the item, or 0xFFFFFFFF if no color should be applied.
     */
    @Override
    public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex)
    {
        // Only apply color to the fluid tint (index 1)
        if (tintIndex!= 1) return 0xFFFFFFFF;

        // Get the fluid contained in the item stack
        FluidStack fluidStack = FluidUtil.getFluidContained(stack);

        // If no fluid is contained, return white color
        if (fluidStack == null) return 0xFFFFFFFF;

        // Return the color of the fluid
        return fluidStack.getFluid().getColor(fluidStack);
    }
}