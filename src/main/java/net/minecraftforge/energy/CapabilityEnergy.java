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

package net.minecraftforge.energy;

import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.capabilities.Capability.IStorage;

/**
 * This class provides a static method to register the Energy capability.
 * It also injects the Energy capability into the CapabilityManager.
 */
public class CapabilityEnergy
{
    /**
     * Energy capability instance.
     * This capability is injected by the CapabilityInject annotation.
     */
    @CapabilityInject(IEnergyStorage.class)
    public static Capability<IEnergyStorage> ENERGY = null;

    /**
     * Registers the Energy capability with the CapabilityManager.
     * It also provides a default implementation of IStorage for the Energy capability.
     * The default implementation is EnergyStorage, which has a maximum capacity of 1000 energy units.
     */
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IEnergyStorage.class, new IStorage<IEnergyStorage>()
        {
            /**
             * Writes the energy stored in the EnergyStorage instance to an NBTTagInt.
             *
             * @param capability The capability being stored.
             * @param instance The EnergyStorage instance.
             * @param side The side of the block being stored.
             * @return An NBTTagInt containing the energy stored in the EnergyStorage instance.
             */
            @Override
            public NBTBase writeNBT(Capability<IEnergyStorage> capability, IEnergyStorage instance, EnumFacing side)
            {
                return new NBTTagInt(instance.getEnergyStored());
            }

            /**
             * Reads the energy from an NBTTagInt and stores it in the EnergyStorage instance.
             *
             * @param capability The capability being stored.
             * @param instance The EnergyStorage instance.
             * @param side The side of the block being stored.
             * @param nbt The NBTBase containing the energy to be read.
             */
            @Override
            public void readNBT(Capability<IEnergyStorage> capability, IEnergyStorage instance, EnumFacing side, NBTBase nbt)
            {
                if (!(instance instanceof EnergyStorage))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                ((EnergyStorage)instance).energy = ((NBTTagInt)nbt).getInt();
            }
        },
        () -> new EnergyStorage(1000));
    }
}
