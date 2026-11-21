package net.minecraftforge.registries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Derived registry numbering ({@link ForgeRegistry#renumber}): at freeze time every registry's ids become
 * dense 0..N-1, with vanilla entries on the numbers the vanilla snapshot gave them and the rest filled in by
 * name order.
 */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class DerivedNumberingTest
{
    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
        GameData.freezeData();
    }

    @Test
    public void vanillaKeepsItsNumbers()
    {
        assertEquals(0, Block.getIdFromBlock(Blocks.AIR));
        assertEquals(1, Block.getIdFromBlock(Blocks.STONE));
        assertEquals(2, Block.getIdFromBlock(Blocks.GRASS));
        assertEquals(3, Block.getIdFromBlock(Blocks.DIRT));
        assertEquals(1, Potion.getIdFromPotion(MobEffects.SPEED));

        ForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
        assertEquals(256, items.getVanillaBaseline().get(Items.IRON_SHOVEL.getRegistryName()).intValue(), "vanilla item id in the baseline");
        assertEquals(256, Item.getIdFromItem(Items.IRON_SHOVEL));
    }

    @Test
    public void moddedEntriesAreSortedByNameAfterVanilla()
    {
        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        blocks.unfreeze();
        Block zzz = new Block(Material.ROCK).setRegistryName("wideids_test", "zzz_block");
        Block aaa = new Block(Material.ROCK).setRegistryName("wideids_test", "aaa_block");
        blocks.register(zzz);
        blocks.register(aaa);
        blocks.freeze();

        int zzzId = Block.getIdFromBlock(zzz);
        int aaaId = Block.getIdFromBlock(aaa);
        assertTrue(aaaId < zzzId, "name order decides, not registration order");

        // vanilla ids are untouched, and the modded pair takes the first ids vanilla left free
        Set<Integer> vanillaIds = new HashSet<>(blocks.getVanillaBaseline().values());
        assertTrue(!vanillaIds.contains(aaaId) && !vanillaIds.contains(zzzId), "modded blocks must not displace vanilla ids");
        int free = 0;
        while (vanillaIds.contains(free))
        {
            free++;
        }
        assertEquals(free, aaaId);
        assertEquals(aaaId + 1, zzzId);
        assertSame(Blocks.AIR, blocks.getValue(0));
        assertSame(aaa, blocks.getValue(aaaId));
        assertSame(zzz, blocks.getValue(zzzId));
    }

    @Test
    public void numberingIsIndependentOfRegistrationOrder()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "standalone_blocks");
        RegistryManager first = new RegistryManager("TEST_A");
        RegistryManager second = new RegistryManager("TEST_B");
        ForgeRegistry<Block> regA = first.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);
        ForgeRegistry<Block> regB = second.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);

        ResourceLocation a = new ResourceLocation("wideids_test", "order_a");
        ResourceLocation b = new ResourceLocation("wideids_test", "order_b");
        ResourceLocation c = new ResourceLocation("wideids_test", "order_c");
        regA.register(blockNamed(a));
        regA.register(blockNamed(b));
        regA.register(blockNamed(c));
        regB.register(blockNamed(c));
        regB.register(blockNamed(a));
        regB.register(blockNamed(b));
        regA.freeze();
        regB.freeze();

        for (ResourceLocation key : new ResourceLocation[] {a, b, c})
        {
            assertEquals(regA.getID(key), regB.getID(key), key.toString());
        }
        assertEquals(0, regA.getID(a));
        assertEquals(1, regA.getID(b));
        assertEquals(2, regA.getID(c));
    }

    @Test
    public void explicitSeedIdsAreHonouredBeforeFreeze()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "seeded_blocks");
        RegistryManager manager = new RegistryManager("TEST_SEED");
        ForgeRegistry<Block> reg = manager.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);

        Block blockA = blockNamed(new ResourceLocation("wideids_test", "seed_a"));
        Block blockB = blockNamed(new ResourceLocation("wideids_test", "seed_b"));
        Block blockC = blockNamed(new ResourceLocation("wideids_test", "seed_c"));

        assertEquals(7, reg.add(7, blockA));
        assertEquals(7, reg.getID(blockA));
        reg.register(blockB);
        assertEquals(0, reg.getID(blockB));
        int blockCSeed = reg.add(7, blockC);
        assertTrue(blockCSeed >= 0 && blockCSeed != 7, "taken seed must not be reused");
        assertEquals(blockCSeed, reg.getID(blockC));

        reg.freeze();
        assertEquals(0, reg.getID(blockA));
        assertEquals(1, reg.getID(blockB));
        assertEquals(2, reg.getID(blockC));
        assertSame(blockA, reg.getValue(0));
        assertSame(blockC, reg.getValue(2));
    }

    @Test
    public void thereIsNoIdRange()
    {
        int count = 70000;
        ResourceLocation name = new ResourceLocation("wideids_test", "ranged_blocks");
        RegistryManager manager = new RegistryManager("TEST_RANGE");
        ForgeRegistry<Block> reg = manager.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);

        Block last = null;
        for (int i = 0; i < count; i++)
        {
            last = blockNamed(new ResourceLocation("wideids_test", String.format("range_%05d", i)));
            reg.register(last);
        }
        reg.freeze();

        assertEquals(count - 1, reg.getID(last));
        assertSame(last, reg.getValue(count - 1));
    }

    private static Block blockNamed(ResourceLocation name)
    {
        return new Block(Material.ROCK).setRegistryName(name);
    }

    @Test
    public void vanillaItemIdsSurviveManyModdedBlocks()
    {
        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        ForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
        blocks.unfreeze();
        items.unfreeze();
        // Vanilla blocks leave holes at 253/254: with four modded blocks at least two land past 255, the ids
        // vanilla items start at. Modded entries must not claim them.
        List<Block> modded = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            ResourceLocation name = new ResourceLocation("wideids_test", "many_" + i);
            Block block = new Block(Material.ROCK).setRegistryName(name);
            Item item = new ItemBlock(block).setRegistryName(name);
            blocks.register(block);
            items.register(item);
            modded.add(block);
        }
        blocks.freeze();
        items.freeze();

        assertTrue(modded.stream().anyMatch(b -> Block.getIdFromBlock(b) >= 256), "test needs a modded block past the vanilla item ids");
        assertEquals(256, Item.getIdFromItem(Items.IRON_SHOVEL));
        assertEquals(1, Item.getIdFromItem(Item.getItemFromBlock(Blocks.STONE)));
        assertDense(items);
        for (Block block : modded)
        {
            Item item = Item.getItemFromBlock(block);
            assertNotNull(item);
            assertTrue(Item.getIdFromItem(item) >= 0);
        }
    }

    @Test
    public void idsAreDenseAfterFreeze()
    {
        ForgeRegistry<Block> blocks = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
        ForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
        ForgeRegistry<Biome> biomes = RegistryManager.ACTIVE.getRegistry(GameData.BIOMES);
        assertDense(blocks);
        assertDense(items);
        assertDense(biomes);
    }

    private static <T extends IForgeRegistryEntry<T>> void assertDense(ForgeRegistry<T> reg)
    {
        List<T> entries = new ArrayList<>();
        for (T value : reg)
        {
            entries.add(value);
        }
        assertEquals(entries.size(), reg.getKeys().size());
        for (int i = 0; i < entries.size(); i++)
        {
            assertNotNull(reg.getRaw(i), "empty id " + i);
            assertSame(entries.get(i), reg.getRaw(i), "iterator order is id order");
            assertEquals(i, reg.getID(entries.get(i)));
        }
        assertEquals(null, reg.getRaw(entries.size()));
    }
    @Test
    public void renumberReportsTheMovedIds()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "remap_blocks");
        RegistryManager manager = new RegistryManager("TEST_REMAP");
        ForgeRegistry<Block> reg = manager.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);

        // seeds come in registration order, reverse of name order: c=0, b=1, a=2
        reg.register(blockNamed(new ResourceLocation("wideids_test", "remap_c")));
        reg.register(blockNamed(new ResourceLocation("wideids_test", "remap_b")));
        reg.register(blockNamed(new ResourceLocation("wideids_test", "remap_a")));
        reg.freeze();

        // frozen order is name order: a=0, b=1, c=2 - a and c moved, b did not
        Map<ResourceLocation, Integer[]> remaps = reg.lastRenumber();
        Integer[] cMove = remaps.get(new ResourceLocation("wideids_test", "remap_c"));
        Integer[] aMove = remaps.get(new ResourceLocation("wideids_test", "remap_a"));
        assertNotNull(cMove, "c moved at freeze");
        assertNotNull(aMove, "a moved at freeze");
        assertEquals(0, (int) cMove[0], "old id is the seed");
        assertEquals(2, (int) cMove[1], "new id is the derived one");
        assertEquals(2, (int) aMove[0], "old id is the seed");
        assertEquals(0, (int) aMove[1], "new id is the derived one");
        assertEquals(2, remaps.size(), "only the two entries whose id actually changed are reported");
    }
    @Test
    public void denseIdsMatchTheRecordedDerivation()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "dense_blocks");
        RegistryManager manager = new RegistryManager("TEST_DENSE");
        ForgeRegistry<Block> reg = manager.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);

        // Registered out of name order: the dense numbering this fixture recorded before the two-state
        // registry change is name-sorted regardless.
        Block delta = blockNamed(new ResourceLocation("wideids_test", "delta"));
        Block alpha = blockNamed(new ResourceLocation("wideids_test", "alpha"));
        Block charlie = blockNamed(new ResourceLocation("wideids_test", "charlie"));
        Block bravo = blockNamed(new ResourceLocation("wideids_test", "bravo"));
        reg.register(delta);
        reg.register(alpha);
        reg.register(charlie);
        reg.register(bravo);
        reg.freeze();

        assertEquals(0, reg.getID(alpha));
        assertEquals(1, reg.getID(bravo));
        assertEquals(2, reg.getID(charlie));
        assertEquals(3, reg.getID(delta));
        assertSame(alpha, reg.getValue(0));
        assertSame(bravo, reg.getValue(1));
        assertSame(charlie, reg.getValue(2));
        assertSame(delta, reg.getValue(3));
    }

    @Test
    public void refreezeInvalidatesCachedNumbering()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "refreeze_blocks");
        RegistryManager manager = new RegistryManager("TEST_REFREEZE");
        ForgeRegistry<Block> reg = manager.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);
        Block a = blockNamed(new ResourceLocation("wideids_test", "refreeze_a"));
        Block b = blockNamed(new ResourceLocation("wideids_test", "refreeze_b"));
        reg.register(a);
        reg.register(b);
        reg.freeze();

        Object frozen = reg.numberingToken();
        assertNotNull(frozen);
        reg.unfreeze();
        assertNotSame(frozen, reg.numberingToken(), "unfreeze drops the installed numbering");
        reg.freeze();
        Object refrozen = reg.numberingToken();
        assertNotSame(frozen, refrozen, "refreeze installs a new numbering caches must not keep");
        assertEquals(0, reg.getID(a));
        assertEquals(1, reg.getID(b));
    }

    /** BiMap.values() was a Set upstream; UniversalTweaks' UTEntityRadiusCheck casts it back. */
    @Test
    public void valuesCollectionStaysASet()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "setview_blocks");
        RegistryManager manager = new RegistryManager("TEST_SETVIEW");
        ForgeRegistry<Block> reg = manager.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);
        Block a = blockNamed(new ResourceLocation("wideids_test", "setview_a"));
        Block b = blockNamed(new ResourceLocation("wideids_test", "setview_b"));
        Block c = blockNamed(new ResourceLocation("wideids_test", "setview_c"));
        reg.register(c);
        reg.register(a);
        reg.register(b);

        assertTrue(reg.getValuesCollection() instanceof Set, "open registry values must be a Set");
        assertEquals(3, reg.getValuesCollection().size());
        assertTrue(reg.getValuesCollection().contains(a));

        reg.freeze();
        assertTrue(reg.getValuesCollection() instanceof Set, "frozen registry values must be a Set");
        assertTrue(reg.getValuesCollection().contains(b));
        List<Block> order = new ArrayList<>(reg.getValuesCollection());
        for (int i = 0; i < order.size(); i++)
        {
            assertSame(order.get(i), reg.getValue(i), "frozen values iterate in id order");
        }
    }
}
