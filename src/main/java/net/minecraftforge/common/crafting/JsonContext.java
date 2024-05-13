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

package net.minecraftforge.common.crafting;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;

/**
 * This class represents a context for JSON parsing in Minecraft Forge modding.
 * It holds information about the mod ID and constants used in crafting recipes.
 */
public class JsonContext
{
    /**
     * The mod ID associated with this context.
     */
    private String modId;

    /**
     * A map of constants used in crafting recipes, keyed by their names.
     */
    private Map<String, Ingredient> constants = Maps.newHashMap();

    /**
     * Constructs a new JsonContext with the given mod ID.
     *
     * @param modId The mod ID associated with this context.
     */
    public JsonContext(String modId)
    {
        this.modId = modId;
    }

    /**
     * Returns the mod ID associated with this context.
     *
     * @return The mod ID.
     */
    public String getModId()
    {
        return this.modId;
    }

    /**
     * Appends the mod ID to the given data if it doesn't already contain a mod ID.
     *
     * @param data The data to append the mod ID to.
     * @return The data with the mod ID appended, or the original data if it already contains a mod ID.
     */
    public String appendModId(String data)
    {
        if (data.indexOf(':') == -1)
            return modId + ":" + data;
        return data;
    }

    /**
     * Returns the constant with the given name, or null if no such constant exists.
     *
     * @param name The name of the constant.
     * @return The constant with the given name, or null if no such constant exists.
     */
    @Nullable
    public Ingredient getConstant(String name)
    {
        return constants.get(name);
    }

    /**
     * Loads constants from the given JSON objects into this context.
     *
     * @param jsons The JSON objects to load constants from.
     * @throws JsonSyntaxException If a constant entry does not contain an 'ingredient' value.
     */
    void loadConstants(JsonObject... jsons)
    {
        for (JsonObject json : jsons)
        {
            if (!CraftingHelper.processConditions(json, "conditions", this))
                continue;
            if (!json.has("ingredient"))
                throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
            constants.put(JsonUtils.getString(json, "name"), CraftingHelper.getIngredient(json.get("ingredient"), this));
        }

    }
}
