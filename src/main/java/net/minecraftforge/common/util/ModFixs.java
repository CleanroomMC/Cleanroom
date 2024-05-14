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

package net.minecraftforge.common.util;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.datafix.IFixType;
import net.minecraft.util.datafix.IFixableData;

/**
 * This class is responsible for managing and registering data fixes for a specific mod.
 * It keeps track of the fixes for each {@link IFixType} and ensures that the fixes are applied in the correct order.
 */
public class ModFixs
{
    /**
     * Logger instance for logging warnings and errors.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    final String mod;
    final int version;

    /**
     * A map that associates each {@link IFixType} with a list of {@link IFixableData} fixes.
     */
    private final Map<IFixType, List<IFixableData>> fixes = Maps.newHashMap();

    /**
     * Constructs a new instance of ModFixs.
     *
     * @param mod The name of the mod for which the fixes are being registered.
     * @param version The data version of the game when the fixes were registered.
     */
    ModFixs(String mod, int version)
    {
        this.mod = mod;
        this.version = version;
    }

    /**
     * Retrieves the list of fixes for a specific {@link IFixType}.
     * If no fixes have been registered for the given type, a new empty list is created.
     *
     * @param type The type of fix to retrieve.
     * @return The list of fixes for the specified type.
     */
    public List<IFixableData> getFixes(IFixType type)
    {
        return this.fixes.computeIfAbsent(type, k -> Lists.newArrayList());
    }

    /**
     * Registers a new data fix for a specific {@link IFixType}.
     * The fix is added to the list of fixes for the given type, and the list is sorted in ascending order of fix versions.
     * If the fix version is greater than the ModFixs's version, a warning is logged and the fix is not registered.
     *
     * @param type The type of fix to register.
     * @param fixer The data fix to register.
     */
    public void registerFix(IFixType type, IFixableData fixer)
    {
        List<IFixableData> list = getFixes(type);
        int ver = fixer.getFixVersion();

        if (ver > this.version)
        {
            LOGGER.warn("[{}] Ignored fix registered for version: {} as the DataVersion of the game is: {}", mod, ver, this.version);
            return;
        }

        if (!list.isEmpty() && list.get(list.size()-1).getFixVersion() > ver)
        {
            for (int x = 0; x < list.size(); ++x)
            {
                if (list.get(x).getFixVersion() > ver)
                {
                    list.add(x, fixer);
                    break;
                }
            }
        }
        else
            list.add(fixer);
    }
}
