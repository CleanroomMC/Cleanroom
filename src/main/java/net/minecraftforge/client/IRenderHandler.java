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

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

/**
 * Take over the rendering of world effects. For the vanilla, they are Cloud, Sky and Weather.
 * <br><br/>
 * Call <br>    {@link net.minecraft.world.WorldProvider#setCloudRenderer(IRenderHandler)}<br>   {@link net.minecraft.world.WorldProvider#setSkyRenderer(IRenderHandler)}<br>    {@link net.minecraft.world.WorldProvider#setWeatherRenderer(IRenderHandler)}
 * to enable them.
 */
public abstract class IRenderHandler
{
    /**
     * @param partialTicks Fractional part of a tick
     * @param world the world being rendered
     * @param mc Current mc instance
     */
    @SideOnly(Side.CLIENT)
    public abstract void render(float partialTicks, WorldClient world, Minecraft mc);
}
