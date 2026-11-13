package net.minecraftforge.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 12: numbering is derived, so there is no VANILLA/FROZEN/STAGING manager, no dummies, and
 * snapshot loading only fires MissingMappings and installs aliases.
 */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class RegistryCleanupTest
{
    @BeforeAll
    public static void setup() throws Exception
    {
        MinecraftTestHarness.bootstrap();
        GameData.freezeData();

        // StartupQuery.abort() -> FMLCommonHandler.getMinecraftServerInstance() needs a sided delegate;
        // a proxy that returns null for getServer() is enough for the abort path.
        Object handler = java.lang.reflect.Proxy.newProxyInstance(
                RegistryCleanupTest.class.getClassLoader(),
                new Class<?>[] { net.minecraftforge.fml.common.IFMLSidedHandler.class },
                (proxy, method, args) -> method.getReturnType() == boolean.class ? false : null);
        java.lang.reflect.Field delegate = net.minecraftforge.fml.common.FMLCommonHandler.class.getDeclaredField("sidedDelegate");
        delegate.setAccessible(true);
        delegate.set(net.minecraftforge.fml.common.FMLCommonHandler.instance(), handler);
    }

    @Test
    public void restoreLocalNumberingReturnsToTheDerivedOrder()
    {
        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        Map<ResourceLocation, Integer> expected = new HashMap<>();
        for (ResourceLocation name : blocks.getKeys())
            expected.put(name, blocks.getID(name));

        List<ResourceLocation> remote = new ArrayList<>(blocks.getKeys());
        Collections.reverse(remote);
        blocks.adoptOrder(remote);
        boolean moved = false;
        for (Map.Entry<ResourceLocation, Integer> e : expected.entrySet())
            moved |= blocks.getID(e.getKey()) != e.getValue();
        assertTrue(moved, "adoptOrder should have changed at least one id");

        GameData.restoreLocalNumbering();

        for (Map.Entry<ResourceLocation, Integer> e : expected.entrySet())
            assertEquals(e.getValue().intValue(), blocks.getID(e.getKey()), e.getKey().toString());
        // lastRenumber feeds the remap event Loader.fireRemapEvent broadcasts; moved ids are reported
        assertFalse(blocks.lastRenumber().isEmpty(), "moved ids should be reported for the remap event");
    }

    @Test
    public void worldSnapshotMissingNamesFireMissingMappingsAndRemap()
    {
        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        ResourceLocation remapped = new ResourceLocation("gone_mod", "remapped_block");
        ResourceLocation ignored = new ResourceLocation("gone_mod", "ignored_block");
        ResourceLocation warned = new ResourceLocation("gone_mod", "warned_block");
        ResourceLocation oldDummy = new ResourceLocation("gone_mod", "old_dummy");

        ForgeRegistry.Snapshot snap = new ForgeRegistry.Snapshot();
        snap.ids.put(remapped, 900);
        snap.ids.put(ignored, 901);
        snap.ids.put(warned, 902);
        snap.dummied.add(oldDummy);
        Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot = new HashMap<>();
        snapshot.put(GameData.BLOCKS, snap);

        int sizeBefore = blocks.getKeys().size();
        Set<ResourceLocation> seen = new HashSet<>();
        Object listener = missingListener(mapping ->
        {
            seen.add(mapping.key);
            if (mapping.key.equals(remapped))
                mapping.remap(Blocks.STONE);
            else if (mapping.key.equals(ignored))
                mapping.ignore();
        });
        MinecraftForge.EVENT_BUS.register(listener);
        try
        {
            GameData.loadWorldSnapshot(snapshot);
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        assertTrue(seen.contains(remapped) && seen.contains(ignored) && seen.contains(warned) && seen.contains(oldDummy),
                "all missing names fired, got " + seen);
        // REMAP installs an alias: the old name resolves to the target everywhere getValue runs
        assertSame(Blocks.STONE, blocks.getValue(remapped));
        assertSame(Blocks.STONE, Block.REGISTRY.getObject(remapped));
        // IGNORE / WARN / old dummied names stay unresolved: nothing was added
        assertFalse(blocks.containsKey(ignored));
        assertFalse(blocks.containsKey(warned));
        assertFalse(blocks.containsKey(oldDummy));
        assertEquals(sizeBefore, blocks.getKeys().size(), "no dummy entries were created");
    }

    @Test
    public void noDummiesAnywhere()
    {
        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        Set<ResourceLocation> before = new HashSet<>(blocks.getKeys());

        ForgeRegistry.Snapshot snap = new ForgeRegistry.Snapshot();
        snap.ids.put(new ResourceLocation("gone_mod", "one"), 950);
        snap.ids.put(new ResourceLocation("gone_mod", "two"), 951);
        snap.dummied.add(new ResourceLocation("gone_mod", "three"));
        Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot = new HashMap<>();
        snapshot.put(GameData.BLOCKS, snap);

        GameData.loadWorldSnapshot(snapshot);
        assertEquals(before, new HashSet<>(blocks.getKeys()), "registry keys unchanged: no dummies");
    }

    @Test
    public void worldSnapshotFailAborts()
    {
        ResourceLocation failed = new ResourceLocation("gone_mod", "failed_block");
        ForgeRegistry.Snapshot snap = new ForgeRegistry.Snapshot();
        snap.ids.put(failed, 970);
        Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot = new HashMap<>();
        snapshot.put(GameData.BLOCKS, snap);

        Object listener = missingListener(mapping -> mapping.fail());
        MinecraftForge.EVENT_BUS.register(listener);
        try
        {
            assertThrows(StartupQuery.AbortedException.class, () -> GameData.loadWorldSnapshot(snapshot));
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    @Test
    public void vanillaManagerHoldsOnlyVanillaEntries()
    {
        assertSame(RegistryManager.ACTIVE, RegistryManager.FROZEN);

        ForgeRegistry<Block> vanillaBlocks = RegistryManager.VANILLA.getRegistry(GameData.BLOCKS);
        assertNotNull(vanillaBlocks, "VANILLA has a block registry");
        assertSame(Blocks.STONE, vanillaBlocks.getValue(new ResourceLocation("minecraft:stone")));
        assertEquals(1, vanillaBlocks.getID(Blocks.STONE), "stone keeps its vanilla number");

        ForgeRegistry<Block> activeBlocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        activeBlocks.unfreeze();
        Block modded = new Block(net.minecraft.block.material.Material.ROCK).setRegistryName("wideids_test", "not_vanilla");
        activeBlocks.register(modded);
        activeBlocks.freeze();

        assertFalse(vanillaBlocks.containsKey(modded.getRegistryName()), "VANILLA holds only baseline entries");
        assertTrue(activeBlocks.containsKey(modded.getRegistryName()));
    }

    @Test
    public void recipeReloadKeepsCodeRecipesOnly()
    {
        ForgeRegistry<IRecipe> reg = (ForgeRegistry<IRecipe>)ForgeRegistries.RECIPES;
        reg.unfreeze();
        try
        {
            reg.register(new TestRecipe().setRegistryName(new ResourceLocation("cleanup_test", "code_recipe")));
            CraftingHelper.rememberCodeRecipes();
            reg.register(new TestRecipe().setRegistryName(new ResourceLocation("cleanup_test", "json_recipe")));

            CraftingHelper.revertToCodeRecipes();

            assertNull(reg.getValue(new ResourceLocation("cleanup_test", "json_recipe")), "json recipe removed");
            assertNotNull(reg.getValue(new ResourceLocation("cleanup_test", "code_recipe")), "code recipe kept");
        }
        finally
        {
            if (!reg.isLocked())
                reg.freeze();
        }
    }

    /**
     * Registering a MissingMappings handler fails in unit tests because Forge's
     * EventSubscriptionTransformer injects the no-arg constructor EventBus probes for at class load
     * and it does not run here; subscribing the base {@link Event} reaches the same ListenerList
     * hierarchy. Returns a listener applying {@code action} to every BLOCKS mapping.
     */
    @SuppressWarnings("unchecked")
    private static Object missingListener(Consumer<RegistryEvent.MissingMappings.Mapping<Block>> action)
    {
        return new Object()
        {
            @SubscribeEvent
            public void onMissing(Event e)
            {
                if (!(e instanceof RegistryEvent.MissingMappings))
                    return;
                RegistryEvent.MissingMappings<?> mm = (RegistryEvent.MissingMappings<?>)e;
                if (!GameData.BLOCKS.equals(mm.getName()))
                    return;
                for (RegistryEvent.MissingMappings.Mapping<?> m : mm.getAllMappings())
                    action.accept((RegistryEvent.MissingMappings.Mapping<Block>)m);
            }
        };
    }

    private static class TestRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
    {
        @Override public boolean matches(InventoryCrafting inv, World worldIn) { return false; }
        @Override public ItemStack getCraftingResult(InventoryCrafting inv) { return ItemStack.EMPTY; }
        @Override public boolean canFit(int width, int height) { return false; }
        @Override public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
        @Override public boolean isDynamic() { return true; }
    }
}
