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

package net.minecraftforge.fml.common.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

/**
 * Vanilla registry tests
 */
@ForgeTestRunner.Isolated
public class VanillaRegistryTests
{
    @BeforeAll
    public static void setupHarness()
    {
        Loader.instance();
        Bootstrap.register();
    }

    @Test
    public void testSetup()
    {
        // All the blocks loaded
        assertEquals(254, Block.REGISTRY.getKeys().size(), "We have all the blocks via GameData");

        // All the items loaded
        assertEquals(411, Item.REGISTRY.getKeys().size(), "We have all the items via GameData");

        // Our lookups find the same stuff vanilla sees
        final IForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(Block.class);
        assertEquals(blocks, getDelegate(Block.REGISTRY), "We have a different block registry then vanilla");

        // We can look up stuff through our APIs
        Block bl = blocks.getValue(new ResourceLocation("minecraft:air"));
        assertEquals(Blocks.AIR, bl, "We got air when we asked for it");

        // Default values work
        Block blch = blocks.getValue(new ResourceLocation("minecraft:cheese"));
        assertEquals(Blocks.AIR, blch, "We got air when we asked for cheese");

        // Our lookups find the same stuff vanilla sees
        final IForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(Item.class);
        assertEquals(items, getDelegate(Item.REGISTRY), "We have a different item registry then vanilla");

        // We can look up stuff through our APIs
        Item it = items.getValue(new ResourceLocation("minecraft:bed"));
        assertEquals(Items.BED, it, "We got a bed item when we asked for it");

        // We find nothing for a non-defaulted registry
        Item none = items.getValue(new ResourceLocation("minecraft:cheese"));
        assertNull(none, "We got nothing (items) when we asked for cheese");
    }

    private Object getDelegate(Object obj)
    {
        try
        {
            Field f = obj.getClass().getDeclaredField("delegate");
            f.setAccessible(true);
            return f.get(obj);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRegistration()
    {
        final IForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(Block.class);
        Block myBlock = (new Block(Material.CAKE){}).setRegistryName(new ResourceLocation("minecraft:testy"));
        blocks.register(myBlock);
        assertNotNull(myBlock, "Registered my block");

        // Our lookups find the same stuff vanilla sees
        assertEquals(blocks, getDelegate(Block.REGISTRY), "We have a different block registry then vanilla");

        Block found = blocks.getValue(new ResourceLocation("minecraft:testy"));
        assertEquals(myBlock, found, "Registry lookup works");
    }

    @Test
    public void testRegistryStates()
    {
        final ForgeRegistry<Block> blockVanilla = (ForgeRegistry<Block>)RegistryManager.VANILLA.getRegistry(Block.class);
        final ForgeRegistry<Block> blockActive = (ForgeRegistry<Block>)RegistryManager.ACTIVE.getRegistry(Block.class);

        assertNotEquals(blockActive, blockVanilla, "Registry states are distinct");

        final Block stoneActive = blockActive.getValue(new ResourceLocation("minecraft:stone"));
        final Block stoneVanilla = blockVanilla.getValue(new ResourceLocation("minecraft:stone"));

        assertEquals(stoneActive, stoneVanilla, "Stone from active and vanilla are the same");

        int activeId = blockActive.getID(stoneActive);
        int vanillaId = blockVanilla.getID(stoneVanilla);

        assertEquals(1, activeId, "Stone has correct id");
        assertEquals(1, vanillaId, "Stone has correct id");
    }


}
