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

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * This class is responsible for handling client-side events in Minecraft Forge.
 * It registers models and item color handlers for the universal bucket.
 */
public class ForgeClientHandler
{
    /**
     * Registers models for the universal bucket if it is enabled.
     *
     * @param event The ModelRegistryEvent that triggered this method.
     */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        // register model for the universal bucket, if it exists
        if (FluidRegistry.isUniversalBucketEnabled())
        {
            ModelLoader.setBucketModelDefinition(ForgeModContainer.getInstance().universalBucket);
        }
    }

    /**
     * Registers an item color handler for the universal bucket if it is enabled.
     *
     * @param event The ColorHandlerEvent.Item that triggered this method.
     */
    @SubscribeEvent
    public static void registerItemHandlers(ColorHandlerEvent.Item event)
    {
        if (FluidRegistry.isUniversalBucketEnabled())
        {
            event.getItemColors().registerItemColorHandler(new FluidContainerColorer(), ForgeModContainer.getInstance().universalBucket);
        }
    }
}
