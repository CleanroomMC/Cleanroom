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

package net.minecraftforge.fml.common;

import java.util.Map;

import net.minecraft.nbt.*;
import net.minecraft.world.storage.*;

/**
 * World save. This service is only available to coremods via the
 * {@link WorldAccessContainer} interface on the mod wrapper. Usually for essential datas. e.g. ForgeRegistries data
 * for mods, use {@link net.minecraft.world.storage.WorldSavedData}
 */
public interface WorldAccessContainer
{
    /**
     * provide nbt to be saved
     *
     * @param handler saveHandler instance
     * @param info    worldinfo
     * @return An NBTTagCompound containing the datas
     */
    NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info);

    /**
     * consume and restores world state from NBT data.
     *
     * @param handler     The SaveHandler instance managing the load operation
     * @param info        The current world's metadata
     * @param propertyMap Additional properties map for world data, see {@ WorldInfo#setAdditionalProperties}
     * @param tag         The NBTTagCompound containing saved world data
     */
    void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag);
}
