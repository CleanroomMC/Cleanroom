package net.minecraftforge.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 13: {@link RegistryEvent.RegisterAll} opens every registry in one event before the
 * per-type {@link RegistryEvent.Register} sequence.
 */
@ForgeTestRunner.Isolated
public class RegistrationPhaseTest
{
    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void registerAllRegistersBlocksAndItemsAtOnce()
    {
        ResourceLocation blockName = new ResourceLocation("wideids_test", "all_block");
        ResourceLocation itemName = new ResourceLocation("wideids_test", "all_block_item");
        Object listener = new Object()
        {
            @SubscribeEvent
            public void onAll(Event e)
            {
                // Forge's EventSubscriptionTransformer injects the ctor EventBus probes for at
                // class load and does not run in tests; subscribe the base event and filter.
                if (!(e instanceof RegistryEvent.RegisterAll))
                    return;
                RegistryEvent.RegisterAll all = (RegistryEvent.RegisterAll)e;
                Block block = new Block(Material.ROCK).setRegistryName(blockName);
                IForgeRegistry<Block> blocks = all.get(GameData.BLOCKS);
                blocks.register(block);
                all.get(Item.class).register(new ItemBlock(block).setRegistryName(itemName));
            }
        };
        MinecraftForge.EVENT_BUS.register(listener);
        try
        {
            GameData.fireRegistryEvents();
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        ForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
        assertNotNull(blocks.getValue(blockName));
        Item item = items.getValue(itemName);
        assertNotNull(item);
        assertSame(blocks.getValue(blockName), ((ItemBlock)item).getBlock());
    }

    @Test
    public void registerAllFiresBeforeEveryTypedRegister()
    {
        List<String> order = new ArrayList<>();
        Object listener = new Object()
        {
            @SubscribeEvent
            public void onAny(Event e)
            {
                if (e instanceof RegistryEvent.RegisterAll)
                    order.add("RegisterAll");
                else if (e instanceof RegistryEvent.Register)
                    order.add("Register:" + ((RegistryEvent.Register<?>)e).getName());
            }
        };
        MinecraftForge.EVENT_BUS.register(listener);
        try
        {
            GameData.fireRegistryEvents();
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        assertFalse(order.isEmpty(), "no events seen");
        assertEquals("RegisterAll", order.get(0), order.toString());
        assertEquals("Register:" + GameData.BLOCKS, order.get(1), order.toString());
        assertEquals("Register:" + GameData.ITEMS, order.get(2), order.toString());
        for (int i = 3; i < order.size(); i++)
            assertTrue(order.get(i).startsWith("Register:"), order.toString());
        assertTrue(order.size() > 3, "expected the remaining registries too: " + order);
    }

    @Test
    public void recipesOnlyPassDoesNotFireRegisterAll()
    {
        List<String> order = new ArrayList<>();
        Object listener = new Object()
        {
            @SubscribeEvent
            public void onAny(Event e)
            {
                if (e instanceof RegistryEvent.RegisterAll)
                    order.add("RegisterAll");
                else if (e instanceof RegistryEvent.Register)
                    order.add("Register:" + ((RegistryEvent.Register<?>)e).getName());
            }
        };
        MinecraftForge.EVENT_BUS.register(listener);
        try
        {
            GameData.fireRegistryEvents(rl -> rl.equals(GameData.RECIPES));
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        assertFalse(order.contains("RegisterAll"), order.toString());
        assertEquals(java.util.Collections.singletonList("Register:" + GameData.RECIPES), order);
    }

    @Test
    public void registerAllGetHonoursTheFilter()
    {
        boolean[] seen = { false };
        Object listener = new Object()
        {
            @SubscribeEvent
            public void onAll(Event e)
            {
                if (!(e instanceof RegistryEvent.RegisterAll))
                    return;
                seen[0] = true;
                assertNull(((RegistryEvent.RegisterAll)e).get(GameData.RECIPES), "recipes filtered out of this pass");
                assertNotNull(((RegistryEvent.RegisterAll)e).get(GameData.BLOCKS));
            }
        };
        MinecraftForge.EVENT_BUS.register(listener);
        try
        {
            GameData.fireRegistryEvents(rl -> !rl.equals(GameData.RECIPES));
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        assertTrue(seen[0], "RegisterAll did not fire on the main pass");
    }
}
