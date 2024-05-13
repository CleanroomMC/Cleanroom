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

/**
 * Reference implementation of {@link IEnergyStorage}. Use/extend this or implement your own.
 *
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 */
public class EnergyStorage implements IEnergyStorage
{
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    /**
     * Constructor for EnergyStorage with default maxReceive and maxExtract equal to capacity.
     *
     * @param capacity Initial energy capacity.
     */
    public EnergyStorage(int capacity)
    {
        this(capacity, capacity, capacity, 0);
    }

    /**
     * Constructor for EnergyStorage with maxReceive and maxExtract equal to maxTransfer.
     *
     * @param capacity Initial energy capacity.
     * @param maxTransfer Maximum energy that can be received or extracted per tick.
     */
    public EnergyStorage(int capacity, int maxTransfer)
    {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    /**
     * Constructor for EnergyStorage.
     *
     * @param capacity Initial energy capacity.
     * @param maxReceive Maximum energy that can be received per tick.
     * @param maxExtract Maximum energy that can be extracted per tick.
     */
    public EnergyStorage(int capacity, int maxReceive, int maxExtract)
    {
        this(capacity, maxReceive, maxExtract, 0);
    }

    /**
     * Constructor for EnergyStorage.
     *
     * @param capacity Initial energy capacity.
     * @param maxReceive Maximum energy that can be received per tick.
     * @param maxExtract Maximum energy that can be extracted per tick.
     * @param energy Initial energy stored.
     */
    public EnergyStorage(int capacity, int maxReceive, int maxExtract, int energy)
    {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    /**
     * Receive energy into this storage.
     *
     * @param maxReceive Maximum energy to receive.
     * @param simulate If true, the energy is only simulated and not actually received.
     * @return The actual energy received.
     */
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate)
            energy += energyReceived;
        return energyReceived;
    }

    /**
     * Extract energy from this storage.
     *
     * @param maxExtract Maximum energy to extract.
     * @param simulate If true, the energy is only simulated and not actually extracted.
     * @return The actual energy extracted.
     */
    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate)
            energy -= energyExtracted;
        return energyExtracted;
    }

    /**
     * Get the current energy stored in this storage.
     *
     * @return The current energy stored.
     */
    @Override
    public int getEnergyStored()
    {
        return energy;
    }

    /**
     * Get the maximum energy capacity of this storage.
     *
     * @return The maximum energy capacity.
     */
    @Override
    public int getMaxEnergyStored()
    {
        return capacity;
    }

    /**
     * Check if this storage can extract energy.
     *
     * @return True if energy can be extracted, false otherwise.
     */
    @Override
    public boolean canExtract()
    {
        return this.maxExtract > 0;
    }

    /**
     * Check if this storage can receive energy.
     *
     * @return True if energy can be received, false otherwise.
     */
    @Override
    public boolean canReceive()
    {
        return this.maxReceive > 0;
    }
}
