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

package net.minecraftforge.fml.client;

import java.util.Map;

/**
 * The extended server list data contains information about the server type, compatibility, 
 * mod data, and whether the server is blocked.
 */
public class ExtendedServerListData {
    public final String type;
    public final boolean isCompatible;
    public final Map<String,String> modData;
    public final boolean isBlocked;

    /**
     * Constructs a new instance of ExtendedServerListData.
     *
     * @param type The type of the server.
     * @param isCompatible Indicates whether the server is compatible with the current client.
     * @param modData Additional mod data associated with the server.
     * @param isBlocked Indicates whether the server is blocked.
     */
    public ExtendedServerListData(String type, boolean isCompatible, Map<String,String> modData, boolean isBlocked)
    {
        this.type = type;
        this.isCompatible = isCompatible;
        this.modData = modData;
        this.isBlocked = isBlocked;
    }
}
