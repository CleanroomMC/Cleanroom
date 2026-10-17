/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.ids;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class WideItemDataTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void idsAreSavedAsIntsWhateverTheirSize() {
        PotionEffect small = new PotionEffect(MobEffects.HASTE, 200, 1);
        assertThat(small.writeCustomPotionEffectToNBT(new NBTTagCompound()).getTagId("Id")).isEqualTo((byte) 3);
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD, 1, 0);
        stack.setItemDamage(300);
        assertThat(stack.writeToNBT(new NBTTagCompound()).getTagId("Damage")).isEqualTo((byte) 3);
        stack.setItemDamage(70000);
        assertThat(stack.writeToNBT(new NBTTagCompound()).getInteger("Damage")).isEqualTo(70000);
    }

    @Test
    public void byteShortAndIntTagsCompareByValue() {
        assertThat(new NBTTagShort((short) 34)).isEqualTo(new NBTTagInt(34));
        assertThat(new NBTTagInt(34)).isEqualTo(new NBTTagShort((short) 34));
        assertThat(new NBTTagInt(34)).isEqualTo(new NBTTagByte((byte) 34));
        assertThat(new NBTTagShort((short) 34).hashCode()).isEqualTo(new NBTTagInt(34).hashCode());
        assertThat(new NBTTagByte((byte) 34).hashCode()).isEqualTo(new NBTTagInt(34).hashCode());
        assertThat(new NBTTagShort((short) 34)).isNotEqualTo(new NBTTagInt(35));
        assertThat(new NBTTagLong(34)).isNotEqualTo(new NBTTagInt(34));
        assertThat(new NBTTagFloat(34)).isNotEqualTo(new NBTTagInt(34));
        NBTTagCompound a = new NBTTagCompound();
        a.setShort("id", (short) 34);
        NBTTagCompound b = new NBTTagCompound();
        b.setInteger("id", 34);
        assertThat(b).isEqualTo(a);
        assertThat(b.hashCode()).isEqualTo(a.hashCode());
        NBTTagList shorts = new NBTTagList();
        shorts.appendTag(new NBTTagShort((short) 7));
        NBTTagList ints = new NBTTagList();
        ints.appendTag(new NBTTagInt(7));
        assertThat(ints).isEqualTo(shorts);
        assertThat(ints.hashCode()).isEqualTo(shorts.hashCode());
    }

    @Test
    public void aStackWithAShortEnchantmentIdEqualsOneWithAnInt() {
        ItemStack ours = new ItemStack(Items.DIAMOND_SWORD);
        ours.addEnchantment(Enchantments.SHARPNESS, 3);
        NBTTagCompound modTag = new NBTTagCompound();
        NBTTagList ench = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setShort("id", (short) Enchantment.getEnchantmentID(Enchantments.SHARPNESS));
        entry.setString("name", Enchantments.SHARPNESS.getRegistryName().toString());
        entry.setShort("lvl", (short) 3);
        ench.appendTag(entry);
        modTag.setTag("ench", ench);
        ItemStack theirs = new ItemStack(Items.DIAMOND_SWORD);
        theirs.setTagCompound(modTag);
        assertThat(ItemStack.areItemStackTagsEqual(ours, theirs)).isTrue();
        assertThat(ItemStack.areItemStacksEqual(ours, theirs)).isTrue();
    }

    @Test
    public void potionIdsFromOlderSavesAreReadAsUnsignedBytes() {
        // Vanilla has 27 potions; register enough that one lands in 128..255, where a signed byte goes negative
        Potion wide = null;
        for (int i = 0; i < 140 && wide == null; i++) {
            Potion potion = new Potion(false, 0) { };
            potion.setRegistryName("wideids_test", "byte_range_effect_" + i);
            net.minecraftforge.fml.common.registry.ForgeRegistries.POTIONS.register(potion);
            int candidate = Potion.getIdFromPotion(potion);
            if (candidate >= 128 && candidate <= 255) {
                wide = potion;
            }
        }
        assertThat(wide).isNotNull();
        int id = Potion.getIdFromPotion(wide);
        assertThat(id >= 128 && id <= 255).isTrue();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Id", (byte) id); // an old save: negative as a byte
        tag.setInteger("Duration", 10);
        assertThat(PotionEffect.readCustomPotionEffectFromNBT(tag).getPotion()).isSameAs(wide);
    }

    @Test
    public void itemStacksKeepWideDamageAndEnchantmentsInNbt() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD, 1, 0);
        stack.setItemDamage(40000);
        stack.addEnchantment(Enchantments.SHARPNESS, 3);
        NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
        ItemStack back = new ItemStack(tag);
        assertThat(back.getItemDamage()).isEqualTo(40000);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(back);
        assertThat((int) enchantments.get(Enchantments.SHARPNESS)).isEqualTo(3);
        assertThat(tag.getCompoundTag("tag").getTagList("ench", 10).getCompoundTagAt(0).getTagId("id")
        ).isEqualTo((byte) 3);
    }

    @Test
    public void potionEffectsRoundTrip() {
        PotionEffect effect = new PotionEffect(MobEffects.HASTE, 200, 1);
        PotionEffect back = PotionEffect.readCustomPotionEffectFromNBT(
            effect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
        assertThat(back.getPotion()).isSameAs(MobEffects.HASTE);
        assertThat(back.getAmplifier()).isEqualTo(1);
    }

    @Test
    public void itemStackPacketsUseVarints() throws IOException {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        ItemStack big = new ItemStack(Items.STONE_PICKAXE, 200, 0);
        big.setItemDamage(50000);
        buf.writeItemStack(ItemStack.EMPTY);
        buf.writeItemStack(big);
        assertThat(buf.readItemStack().isEmpty()).isTrue();
        ItemStack back = buf.readItemStack();
        assertThat(back.getItem()).isSameAs(Items.STONE_PICKAXE);
        assertThat(back.getCount()).isEqualTo(200);
        assertThat(back.getItemDamage()).isEqualTo(50000);
    }

    @Test
    public void recipeBookPackingKeepsVanillaValuesAndPacksTheRest() {
        int vanilla = PackedItems.pack(Items.DYE, 4);
        assertThat(vanilla).isEqualTo(net.minecraft.item.Item.getIdFromItem(Items.DYE) << 16 | 4);
        int wide = PackedItems.pack(Items.DYE, 70000);
        assertThat(wide < 0).isTrue();
        assertThat(PackedItems.pack(Items.DYE, 70000)).isEqualTo(wide);
        assertThat(PackedItems.pack(Items.DYE, 70001)).isNotEqualTo(wide);
        ItemStack back = PackedItems.unpack(wide);
        assertThat(back.getItem()).isSameAs(Items.DYE);
        assertThat(back.getMetadata()).isEqualTo(70000);
        assertThat(PackedItems.unpack(0).isEmpty()).isTrue();
    }

    @Test
    public void recipeBookAndItemEqualDistinguishNonLegacyBlockStateComponents() {
        net.minecraft.block.state.IBlockState granite = net.minecraft.init.Blocks.STONE.getStateFromMeta(1);
        net.minecraft.block.state.IBlockState diorite = net.minecraft.init.Blocks.STONE.getStateFromMeta(3);
        ItemStack stackA = new ItemStack(net.minecraft.init.Blocks.STONE, 1, 0);
        ItemStack stackB = new ItemStack(net.minecraft.init.Blocks.STONE, 1, 0);
        stackA.getComponents().set(com.cleanroommc.component.DataComponents.BLOCK_STATE, granite);
        stackB.getComponents().set(com.cleanroommc.component.DataComponents.BLOCK_STATE, diorite);

        assertThat(stackA.isItemEqual(stackB)).isFalse();
        int packedA = net.minecraft.client.util.RecipeItemHelper.pack(stackA);
        int packedB = net.minecraft.client.util.RecipeItemHelper.pack(stackB);
        assertThat(packedA).isNotEqualTo(packedB);
        assertThat(PackedItems.unpack(packedA).get(com.cleanroommc.component.DataComponents.BLOCK_STATE)
        ).isSameAs(granite);
        assertThat(PackedItems.unpack(packedB).get(com.cleanroommc.component.DataComponents.BLOCK_STATE)
        ).isSameAs(diorite);
    }

    @Test
    public void oreDictionaryHandlesWideDamageWithoutCollision() {
        ItemStack wideA = new ItemStack(Items.DYE, 1, 50000);
        ItemStack wideB = new ItemStack(Items.DYE, 1, 65535);
        ItemStack metaZero = new ItemStack(Items.DYE, 1, 0);
        net.minecraftforge.oredict.OreDictionary.registerOre("testWideDye50000", wideA);
        net.minecraftforge.oredict.OreDictionary.registerOre("testWideDye65535", wideB);

        int id50000 = net.minecraftforge.oredict.OreDictionary.getOreID("testWideDye50000");
        int id65535 = net.minecraftforge.oredict.OreDictionary.getOreID("testWideDye65535");
        assertThat(net.minecraftforge.oredict.OreDictionary.getOreIDs(wideA)).contains(id50000).doesNotContain(id65535);
        assertThat(net.minecraftforge.oredict.OreDictionary.getOreIDs(wideB)).contains(id65535).doesNotContain(id50000);
        assertThat(net.minecraftforge.oredict.OreDictionary.getOreIDs(metaZero)).doesNotContain(id50000, id65535);
    }

    @Test
    public void jukeboxComparatorSignalClampsModdedRecordsToRedstoneRange() {
        net.minecraft.util.ResourceLocation modDiscName = new net.minecraft.util.ResourceLocation("wideids_test",
            "mod_disc"
        );
        if (!net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.containsKey(modDiscName)) {
            net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(
                new net.minecraft.item.Item().setRegistryName(modDiscName));
        }
        net.minecraft.item.Item modDisc = net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValue(modDiscName
        );
        int unclamped = net.minecraft.item.Item.getIdFromItem(modDisc) -
            net.minecraft.item.Item.getIdFromItem(Items.RECORD_13) + 1;
        assertThat(unclamped < 1 || unclamped > 15).isTrue();

        net.minecraft.block.BlockJukebox.TileEntityJukebox te = new net.minecraft.block.BlockJukebox.TileEntityJukebox();
        net.minecraft.world.World dummyWorld = new net.minecraft.world.World(null,
            new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),
            new net.minecraft.world.WorldProviderSurface(), new net.minecraft.profiler.Profiler(), false
        ) {

            @Override
            protected net.minecraft.world.chunk.IChunkProvider createChunkProvider() {
                return null;
            }

            @Override
            protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
                return true;
            }

            @Override
            public net.minecraft.tileentity.TileEntity getTileEntity(net.minecraft.util.math.BlockPos pos) {
                return te;
            }

        };

        te.setRecord(new ItemStack(Items.RECORD_13));
        assertThat(
            net.minecraft.init.Blocks.JUKEBOX.getComparatorInputOverride(net.minecraft.init.Blocks.JUKEBOX.getDefaultState(),
                dummyWorld, net.minecraft.util.math.BlockPos.ORIGIN
            )).isEqualTo(1);

        te.setRecord(new ItemStack(Items.RECORD_WAIT));
        assertThat(
            net.minecraft.init.Blocks.JUKEBOX.getComparatorInputOverride(net.minecraft.init.Blocks.JUKEBOX.getDefaultState(),
                dummyWorld, net.minecraft.util.math.BlockPos.ORIGIN
            )).isEqualTo(12);

        te.setRecord(new ItemStack(modDisc));
        int moddedSignal = net.minecraft.init.Blocks.JUKEBOX.getComparatorInputOverride(net.minecraft.init.Blocks.JUKEBOX.getDefaultState(),
            dummyWorld, net.minecraft.util.math.BlockPos.ORIGIN
        );
        assertThat(moddedSignal).isBetween(1, 15);
    }

    @Test
    void wideStackCountRoundTripsThroughNbtAndPreservesByteTagForSmallCounts() {
        ItemStack normal = new ItemStack(Items.APPLE, 64, 0);
        NBTTagCompound normalNbt = normal.writeToNBT(new NBTTagCompound());
        assertThat(normalNbt.getTagId("Count")).isEqualTo((byte) 1);
        assertThat(new ItemStack(normalNbt).getCount()).isEqualTo(64);

        ItemStack wideCount = new ItemStack(Items.STONE_PICKAXE, 200, 0);
        NBTTagCompound wideNbt = wideCount.writeToNBT(new NBTTagCompound());
        ItemStack restored = new ItemStack(wideNbt);
        assertThat(restored.isEmpty()).isFalse();
        assertThat(restored.getCount()).isEqualTo(200);
    }

    @Test
    void setItemDamageClearsStaleStateComponentWhenMetaChangesButKeepsItWhenUnchanged() {
        ItemStack wool = new ItemStack(net.minecraft.init.Blocks.WOOL, 1, 0);
        net.minecraft.block.state.IBlockState blueState = net.minecraft
            .init
            .Blocks
            .WOOL
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockColored.COLOR, net.minecraft.item.EnumDyeColor.BLUE);
        wool.set(com.cleanroommc.component.DataComponents.BLOCK_STATE, blueState);
        int blueMeta = wool.getMetadata();

        wool.setItemDamage(blueMeta);
        assertThat(wool.get(com.cleanroommc.component.DataComponents.BLOCK_STATE)).isSameAs(blueState);

        wool.setItemDamage(0);
        assertThat(wool.get(com.cleanroommc.component.DataComponents.BLOCK_STATE
        )).isSameAs(net.minecraft.init.Blocks.WOOL.getDefaultState());
    }

    @Test
    void stateGetStateIdRefreshesAfterRegistryInvalidation() {
        net.minecraft.block.state.IBlockState stone = net.minecraft.init.Blocks.STONE.getDefaultState();
        int initialBlockStateId = stone.getStateId();
        ((net.minecraftforge.registries.BlockStateIdMap) net.minecraft.block.Block.BLOCK_STATE_IDS).invalidate();
        assertThat(stone.getStateId()).isEqualTo(initialBlockStateId);

        com.cleanroommc.component.IItemState coalState = Items.COAL.getDefaultItemState();
        int initialItemStateId = coalState.getStateId();
        ((net.minecraftforge.registries.ItemStateIdMap) net.minecraft.item.Item.ITEM_STATE_IDS).invalidate();
        assertThat(coalState.getStateId()).isEqualTo(initialItemStateId);
    }

    @Test
    void partialStatePropertyIngredientMatchesAnyVariantWithMatchingProperty() {
        net.minecraft.item.Item logItem = net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.LOG);
        com.cleanroommc.component.DataComponentPredicate axisY = com.cleanroommc
            .component
            .DataComponentPredicate
            .builder()
            .withStateProperty("axis", "y")
            .build();
        net.minecraft.item.crafting.Ingredient ing = net.minecraft.item.crafting.Ingredient.of(logItem, axisY);

        ItemStack oakY = new ItemStack(logItem, 1, 0);
        ItemStack spruceY = new ItemStack(logItem, 1, 1);
        ItemStack oakX = new ItemStack(logItem, 1, 0);
        oakX.set(com.cleanroommc.component.DataComponents.BLOCK_STATE,
            net.minecraft
                .init
                .Blocks
                .LOG
                .getDefaultState()
                .withProperty(net.minecraft.block.BlockLog.LOG_AXIS, net.minecraft.block.BlockLog.EnumAxis.X)
        );

        assertThat(ing.apply(oakY)).isTrue();
        assertThat(ing.apply(spruceY)).isTrue();
        assertThat(ing.apply(oakX)).isFalse();
    }

    @Test
    void packedItemsClearResetsDynamicKeys() {
        ItemStack stack = new ItemStack(Items.DYE, 1, 70000);
        int packedBefore = PackedItems.pack(stack);
        assertThat(packedBefore).isNegative();
        PackedItems.clear();
        assertThat(PackedItems.unpack(packedBefore).isEmpty()).isTrue();
        int packedAfter = PackedItems.pack(stack);
        assertThat(PackedItems.unpack(packedAfter).getItemDamage()).isEqualTo(70000);
    }

    @Test
    void itemHandlerHelperAndMerchantRecipeRespectDataComponents() {
        ItemStack plain = new ItemStack(Items.APPLE, 1, 0);
        ItemStack customMax = new ItemStack(Items.APPLE, 1, 0);
        customMax.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);
        ItemStack customMax2 = new ItemStack(Items.APPLE, 1, 0);
        customMax2.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);

        assertThat(net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack(plain, customMax)).isFalse();
        assertThat(net.minecraftforge.items.ItemHandlerHelper.canItemStacksStackRelaxed(plain, customMax)).isFalse();
        assertThat(net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack(customMax, customMax2)).isTrue();
        assertThat(net.minecraftforge.items.ItemHandlerHelper.canItemStacksStackRelaxed(customMax, customMax2
        )).isTrue();

        net.minecraft.village.MerchantRecipeList recipes = new net.minecraft.village.MerchantRecipeList();
        recipes.add(new net.minecraft.village.MerchantRecipe(customMax, new ItemStack(Items.EMERALD)));
        assertThat(recipes.canRecipeBeUsed(plain, ItemStack.EMPTY, 0)).isNull();
        assertThat(recipes.canRecipeBeUsed(customMax2, ItemStack.EMPTY, 0)).isNotNull();
    }

    @Test
    void outOfRangeBlockStateIdInPacketDefaultsToAirInsteadOfNull() throws Exception {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBlockPos(net.minecraft.util.math.BlockPos.ORIGIN);
        buf.writeVarInt(999_999_999);
        net.minecraft.network.play.server.SPacketBlockChange pkt = new net.minecraft.network.play.server.SPacketBlockChange();
        pkt.readPacketData(buf);
        assertThat(pkt.getBlockState()).isSameAs(net.minecraft.init.Blocks.AIR.getDefaultState());
    }

    @Test
    void packedItemsPreservesAllPatchedDataComponentsAcrossUnpack() {
        ItemStack plain = new ItemStack(Items.APPLE, 1, 0);
        ItemStack custom = new ItemStack(Items.APPLE, 1, 0);
        custom.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);

        int packedPlain = PackedItems.pack(plain);
        int packedCustom = PackedItems.pack(custom);
        assertThat(packedPlain).isNotEqualTo(packedCustom);

        ItemStack unpackedCustom = PackedItems.unpack(packedCustom);
        assertThat(unpackedCustom.get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)).isEqualTo(16);
        assertThat(ItemStack.areItemStackTagsEqual(unpackedCustom, custom)).isTrue();
    }

    @Test
    void unresolvedStatePropertiesAndRemovedNamespaceComponentsAreHandledOnRead() {
        ItemStack wool = new ItemStack(net.minecraft.init.Blocks.WOOL, 1, 0);
        NBTTagCompound nbt = wool.writeToNBT(new NBTTagCompound());
        NBTTagCompound components = new NBTTagCompound();
        NBTTagCompound blockStateTag = new NBTTagCompound();
        blockStateTag.setString("Name", "minecraft:wool");
        NBTTagCompound props = new NBTTagCompound();
        props.setString("color", "absent_mod_color");
        blockStateTag.setTag("Properties", props);
        components.setTag("cleanroom:block_state", blockStateTag);
        components.setString("removed_mod:stale_tag", "drop_me");
        nbt.setTag("Components", components);

        RetainedContent.removedNamespaces().add("removed_mod");
        try {
            ItemStack loaded = new ItemStack(nbt);
            NBTTagCompound savedOut = loaded.writeToNBT(new NBTTagCompound());
            NBTTagCompound outComponents = savedOut.getCompoundTag("Components");
            assertThat(outComponents.getCompoundTag("cleanroom:block_state")
                    .getCompoundTag("Properties")
                    .getString("color")
            ).isEqualTo("absent_mod_color");
            assertThat(outComponents.hasKey("removed_mod:stale_tag")).isFalse();
        } finally {
            RetainedContent.removedNamespaces().remove("removed_mod");
        }
    }

    @Test
    void damagedStackWithIncreasedMaxStackSizeDoesNotMergeIntoUndamagedStack() {
        ItemStack undamaged = new ItemStack(Items.STONE_PICKAXE, 1, 0);
        undamaged.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);
        ItemStack damaged = new ItemStack(Items.STONE_PICKAXE, 1, 10);
        damaged.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);

        assertThat(net.minecraftforge.items.ItemHandlerHelper.canItemStacksStackRelaxed(undamaged, damaged)).isFalse();
    }

    @Test
    void customDyeColorSupportsStainedClaySheepRgbAndDyeSubItems() {
        net.minecraft.item.EnumDyeColor custom = net.minecraftforge.common.util.EnumHelper.addDyeColor("TEST_INDIGO",
            16, 16, "test_indigo", "testIndigo", 0x4B0082, net.minecraft.util.text.TextFormatting.DARK_PURPLE
        );
        assertThat(custom).isNotNull();

        net.minecraft.block.state.IBlockState clayState = net.minecraft
            .init
            .Blocks
            .STAINED_HARDENED_CLAY
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockColored.COLOR, custom);
        assertThat(clayState.getMapColor(null, net.minecraft.util.math.BlockPos.ORIGIN)).isNotNull();

        float[] rgb = net.minecraft.entity.passive.EntitySheep.getDyeRgb(custom);
        assertThat(rgb).hasSize(3);

        net.minecraft.util.NonNullList<ItemStack> dyes = net.minecraft.util.NonNullList.create();
        Items.DYE.getSubItems(net.minecraft.creativetab.CreativeTabs.MATERIALS, dyes);
        assertThat(dyes).anyMatch(s -> s.getItemState().getValue(net.minecraft.item.ItemDye.COLOR) == custom);
    }

    @Test
    void stainedGlassAndPanePreserveColorStateAndPropertyEnumHashCodeRemainsStable() {
        net.minecraft.block.state.IBlockState preExistingGlass = net.minecraft
            .init
            .Blocks
            .STAINED_GLASS
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockStainedGlass.COLOR, net.minecraft.item.EnumDyeColor.BLUE);
        int hashBefore = net.minecraft.block.BlockStainedGlass.COLOR.hashCode();

        net.minecraft.item.EnumDyeColor custom = net.minecraftforge.common.util.EnumHelper.addDyeColor("TEST_TEAL", 17,
            17, "test_teal", "testTeal", 0x008080, net.minecraft.util.text.TextFormatting.DARK_AQUA
        );
        assertThat(custom).isNotNull();
        assertThat(net.minecraft.block.BlockStainedGlass.COLOR.hashCode()).isEqualTo(hashBefore);
        assertThat(preExistingGlass.getValue(net.minecraft.block.BlockStainedGlass.COLOR
        )).isEqualTo(net.minecraft.item.EnumDyeColor.BLUE);

        net.minecraft.block.state.IBlockState connectedPane = net.minecraft
            .init
            .Blocks
            .STAINED_GLASS_PANE
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockStainedGlassPane.COLOR, custom)
            .withProperty(net.minecraft.block.BlockPane.NORTH, true);
        net.minecraft.block.state.IBlockState dropPane = net.minecraft.init.Blocks.STAINED_GLASS_PANE.getStateForDrop(connectedPane
        );
        assertThat(dropPane.getValue(net.minecraft.block.BlockStainedGlassPane.COLOR)).isEqualTo(custom);
        assertThat(dropPane.getValue(net.minecraft.block.BlockPane.NORTH)).isFalse();

        ItemStack glassStack = net.minecraft.init.Blocks.STAINED_GLASS.getItem(null,
            net.minecraft.util.math.BlockPos.ORIGIN,
            net.minecraft
                .init
                .Blocks
                .STAINED_GLASS
                .getDefaultState()
                .withProperty(net.minecraft.block.BlockStainedGlass.COLOR, custom)
        );
        assertThat(glassStack.getTranslationKey()).endsWith(".testTeal");

        ItemStack paneStack = net.minecraft.init.Blocks.STAINED_GLASS_PANE.getItem(null,
            net.minecraft.util.math.BlockPos.ORIGIN, connectedPane
        );
        assertThat(paneStack.getTranslationKey()).endsWith(".testTeal");

        net.minecraft.block.state.IBlockState standInGlass = StandIns.forMissing("absent_mod:stained_glass[color=blue]"
        );
        assertThat(standInGlass).isEqualTo(net.minecraft
                .init
                .Blocks
                .STAINED_GLASS
                .getDefaultState()
                .withProperty(net.minecraft.block.BlockStainedGlass.COLOR, net.minecraft.item.EnumDyeColor.BLUE)
        );
        net.minecraft.block.state.IBlockState standInPane = StandIns.forMissing("absent_mod:stained_glass_pane[color=red,north=true]"
        );
        assertThat(standInPane).isEqualTo(net.minecraft
                .init
                .Blocks
                .STAINED_GLASS_PANE
                .getDefaultState()
                .withProperty(net.minecraft.block.BlockStainedGlassPane.COLOR, net.minecraft.item.EnumDyeColor.RED)
                .withProperty(net.minecraft.block.BlockPane.NORTH, true)
        );
    }

    @Test
    void mapIdComponentSyncsMapPacketAndSkullUsesItemState() {
        ItemStack mapStack = new ItemStack(Items.FILLED_MAP, 1, 0);
        mapStack.set(com.cleanroommc.component.DataComponents.MAP_ID, 424242);
        assertThat(net.minecraft.item.ItemMap.getMapId(mapStack)).isEqualTo(424242);
        assertThat(mapStack.getMetadata()).isEqualTo(424242);

        net.minecraft.world.storage.MapData mapData = new net.minecraft.world.storage.MapData("map_424242");
        net.minecraft.world.storage.MapData.MapInfo info = mapData.new MapInfo(null);
        net.minecraft.network.play.server.SPacketMaps pkt = (net.minecraft.network.play.server.SPacketMaps) info.getPacket(mapStack
        );
        assertThat(pkt).isNotNull();
        assertThat(pkt.getMapId()).isEqualTo(424242);

        ItemStack dragonSkull = new ItemStack(Items.SKULL, 1, 0);
        dragonSkull.setItemState(Items.SKULL
                .getDefaultItemState()
                .withProperty(net.minecraft.item.ItemSkull.TYPE, net.minecraft.item.ItemSkull.SkullType.DRAGON)
        );
        assertThat(dragonSkull.getTranslationKey()).endsWith(".dragon");
    }

    @Test
    void tileEntityItemStackAndBedRenderersInitializeCleanlyWithCustomDyeColors() {
        net.minecraft.item.EnumDyeColor sparse = net.minecraftforge.common.util.EnumHelper.addDyeColor("TEST_CORAL", 20,
            20, "test_coral", "testCoral", 0xFF7F50, net.minecraft.util.text.TextFormatting.RED
        );
        assertThat(sparse).isNotNull();
        assertThat(net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer.instance).isNotNull();
        assertThat(new net.minecraft.client.renderer.tileentity.TileEntityBedRenderer()).isNotNull();
    }

    @Test
    void flowerPotPreservesDataComponentsAndPistonWritesLegacyAndBlockStateNbt() {
        ItemStack customPlant = new ItemStack(net.minecraft.init.Blocks.SAPLING, 1, 0);
        customPlant.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);

        net.minecraft.tileentity.TileEntityFlowerPot pot = new net.minecraft.tileentity.TileEntityFlowerPot();
        pot.setItemStack(customPlant);
        assertThat(pot.getFlowerItemStack().get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)).isEqualTo(16);

        NBTTagCompound potNbt = pot.writeToNBT(new NBTTagCompound());
        net.minecraft.tileentity.TileEntityFlowerPot loadedPot = new net.minecraft.tileentity.TileEntityFlowerPot();
        loadedPot.readFromNBT(potNbt);
        assertThat(loadedPot.getFlowerItemStack().get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)
        ).isEqualTo(16);

        net.minecraft.block.state.IBlockState blueWool = net.minecraft
            .init
            .Blocks
            .WOOL
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockColored.COLOR, net.minecraft.item.EnumDyeColor.BLUE);
        net.minecraft.tileentity.TileEntityPiston piston = new net.minecraft.tileentity.TileEntityPiston(blueWool,
            net.minecraft.util.EnumFacing.UP, true, false
        );
        NBTTagCompound pistonNbt = piston.writeToNBT(new NBTTagCompound());
        assertThat(pistonNbt.hasKey("blockId", 99)).isTrue();
        assertThat(pistonNbt.hasKey("blockData", 99)).isTrue();
        assertThat(pistonNbt.hasKey("blockState", 10)).isTrue();

        net.minecraft.tileentity.TileEntityPiston loadedPiston = new net.minecraft.tileentity.TileEntityPiston();
        loadedPiston.readFromNBT(pistonNbt);
        assertThat(loadedPiston.getPistonState()).isSameAs(blueWool);
    }

    @Test
    void blockSnapshotRetainsUnregisteredStateTagWithoutReplacingWithAir() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("blockMod", "absent_mod");
        tag.setString("blockName", "custom_block");
        tag.setInteger("posX", 1);
        tag.setInteger("posY", 64);
        tag.setInteger("posZ", 2);
        tag.setInteger("flag", 3);
        tag.setInteger("dimension", 0);
        tag.setInteger("metadata", 0);
        tag.setBoolean("hasTE", false);
        NBTTagCompound stateTag = new NBTTagCompound();
        stateTag.setString("Name", "absent_mod:custom_block");
        NBTTagCompound props = new NBTTagCompound();
        props.setString("tier", "25");
        stateTag.setTag("Properties", props);
        tag.setTag("state", stateTag);

        net.minecraftforge.common.util.BlockSnapshot read = net.minecraftforge.common.util.BlockSnapshot.readFromNBT(tag
        );
        net.minecraftforge.common.util.BlockSnapshot fresh = new net.minecraftforge.common.util.BlockSnapshot(0,
            new net.minecraft.util.math.BlockPos(1, 64, 2),
            new net.minecraft.util.ResourceLocation("absent_mod", "custom_block"), 0, 3, null
        );
        assertThat(read).isEqualTo(fresh);

        NBTTagCompound out = new NBTTagCompound();
        read.writeToNBT(out);
        assertThat(out.getCompoundTag("state").getString("Name")).isEqualTo("absent_mod:custom_block");
        assertThat(out.getCompoundTag("state").getCompoundTag("Properties").getString("tier")).isEqualTo("25");
    }

    @Test
    void dyeUtilsBannerAndShieldRecipesSupportCustomDyeColorsPast16() {
        net.minecraft.item.EnumDyeColor customColor = net.minecraftforge.common.util.EnumHelper.addDyeColor("TEST_AMBER",
            21, 21, "test_amber", "testAmber", 0xFFBF00, net.minecraft.util.text.TextFormatting.GOLD
        );
        ItemStack customDye = new ItemStack(Items.DYE, 1, 21);
        customDye.setItemState(Items.DYE
                .getDefaultItemState()
                .withProperty(net.minecraft.item.ItemDye.COLOR, customColor)
        );
        assertThat(net.minecraftforge.oredict.DyeUtils.isDye(customDye)).isTrue();
        assertThat(net.minecraftforge.oredict.DyeUtils.colorFromStack(customDye)).contains(customColor);
        assertThat(net.minecraftforge.oredict.DyeUtils.rawMetaFromStack(customDye)).isEqualTo(21);
        assertThat(net.minecraftforge.oredict.DyeUtils.rawDyeDamageFromStack(customDye)).isEqualTo(21);

        ItemStack customBanner = net.minecraft.item.ItemBanner.makeBanner(customColor, null);
        assertThat(net.minecraft.item.ItemBanner.getBaseColor(customBanner)).isSameAs(customColor);

        net.minecraft.inventory.InventoryCrafting shieldGrid = new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container() {

                @Override
                public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
                    return true;
                }

            },
            3, 3
        );
        shieldGrid.setInventorySlotContents(0, new ItemStack(Items.SHIELD));
        shieldGrid.setInventorySlotContents(1, customBanner);
        net.minecraft.item.crafting.ShieldRecipes.Decoration shieldRecipe = new net.minecraft.item.crafting.ShieldRecipes.Decoration();
        assertThat(shieldRecipe.matches(shieldGrid, null)).isTrue();
        ItemStack decoratedShield = shieldRecipe.getCraftingResult(shieldGrid);
        assertThat(decoratedShield.getSubCompound("BlockEntityTag").getInteger("Base")).isEqualTo(21);
    }

    @Test
    void recipeFireworksUsesCustomDyeColorValuePast16() {
        net.minecraft.item.EnumDyeColor customColor = net.minecraftforge.common.util.EnumHelper.addDyeColor("TEST_VIOLET",
            22, 22, "test_violet", "testViolet", 0x8F00FF, net.minecraft.util.text.TextFormatting.DARK_PURPLE
        );
        ItemStack customDye = new ItemStack(Items.DYE, 1, 22);
        customDye.setItemState(Items.DYE
                .getDefaultItemState()
                .withProperty(net.minecraft.item.ItemDye.COLOR, customColor)
        );

        net.minecraft.inventory.InventoryCrafting grid = new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container() {

                @Override
                public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
                    return true;
                }

            },
            3, 3
        );
        grid.setInventorySlotContents(0, new ItemStack(Items.GUNPOWDER));
        grid.setInventorySlotContents(1, customDye);

        net.minecraft.item.crafting.RecipeFireworks fireworkRecipe = new net.minecraft.item.crafting.RecipeFireworks();
        assertThat(fireworkRecipe.matches(grid, null)).isTrue();
        ItemStack star = fireworkRecipe.getCraftingResult(grid);
        assertThat(star.getTagCompound().getCompoundTag("Explosion").getIntArray("Colors")).containsExactly(0x8F00FF);
    }

    @Test
    void villagerTradeListAndShulkerBoxColoringPreserveDataComponents() {
        ItemStack templateBuy = new ItemStack(Items.APPLE, 1, 0);
        templateBuy.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);
        net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds tradeGen = new net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds(templateBuy,
            new net.minecraft.entity.passive.EntityVillager.PriceInfo(2, 2)
        );
        net.minecraft.village.MerchantRecipeList tradeList = new net.minecraft.village.MerchantRecipeList();
        tradeGen.addMerchantRecipe(null, tradeList, new java.util.Random(0L));
        assertThat(tradeList).hasSize(1);
        assertThat(tradeList.get(0).getItemToSell().get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)
        ).isEqualTo(16);

        ItemStack whiteShulker = new ItemStack(net.minecraft.init.Blocks.WHITE_SHULKER_BOX);
        whiteShulker.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 4);
        net.minecraft.inventory.InventoryCrafting grid = new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container() {

                @Override
                public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
                    return true;
                }

            },
            3, 3
        );
        grid.setInventorySlotContents(0, whiteShulker);
        grid.setInventorySlotContents(1,
            new ItemStack(Items.DYE, 1, net.minecraft.item.EnumDyeColor.BLUE.getDyeDamage())
        );
        net.minecraft.item.crafting.ShulkerBoxRecipes.ShulkerBoxColoring shulkerRecipe = new net.minecraft.item.crafting.ShulkerBoxRecipes.ShulkerBoxColoring();
        assertThat(shulkerRecipe.matches(grid, null)).isTrue();
        ItemStack coloredShulker = shulkerRecipe.getCraftingResult(grid);
        assertThat(coloredShulker.getItem()).isSameAs(
            net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.BLUE_SHULKER_BOX));
        assertThat(coloredShulker.get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)).isEqualTo(4);
    }

    @Test
    void mapAndBookCloningRecipesPreserveDataComponents() {
        ItemStack filledMap = new ItemStack(Items.FILLED_MAP, 1, 77777);
        net.minecraft.item.ItemMap.setMapId(filledMap, 77777);
        filledMap.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 32);

        net.minecraft.inventory.InventoryCrafting mapGrid = new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container() {

                @Override
                public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
                    return true;
                }

            },
            3, 3
        );
        mapGrid.setInventorySlotContents(0, filledMap);
        mapGrid.setInventorySlotContents(1, new ItemStack(Items.MAP));

        net.minecraft.item.crafting.RecipesMapCloning mapCloning = new net.minecraft.item.crafting.RecipesMapCloning();
        assertThat(mapCloning.matches(mapGrid, null)).isTrue();
        ItemStack clonedMap = mapCloning.getCraftingResult(mapGrid);
        assertThat(net.minecraft.item.ItemMap.getMapId(clonedMap)).isEqualTo(77777);
        assertThat(clonedMap.get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)).isEqualTo(32);

        ItemStack writtenBook = new ItemStack(Items.WRITTEN_BOOK, 1, 0);
        NBTTagCompound bookNbt = new NBTTagCompound();
        bookNbt.setInteger("generation", 0);
        writtenBook.setTagCompound(bookNbt);
        writtenBook.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 8);

        net.minecraft.inventory.InventoryCrafting bookGrid = new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container() {

                @Override
                public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
                    return true;
                }

            },
            3, 3
        );
        bookGrid.setInventorySlotContents(0, writtenBook);
        bookGrid.setInventorySlotContents(1, new ItemStack(Items.WRITABLE_BOOK));

        net.minecraft.item.crafting.RecipeBookCloning bookCloning = new net.minecraft.item.crafting.RecipeBookCloning();
        assertThat(bookCloning.matches(bookGrid, null)).isTrue();
        ItemStack clonedBook = bookCloning.getCraftingResult(bookGrid);
        assertThat(clonedBook.get(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE)).isEqualTo(8);
    }

    @Test
    void containerCanAddItemToSlotRejectsDamagedStacksWithRaisedMaxStackSize() {
        ItemStack undamaged = new ItemStack(Items.DIAMOND_SWORD, 1, 0);
        undamaged.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);

        ItemStack damaged = undamaged.copy();
        damaged.setDamageValue(5);

        net.minecraft.inventory.InventoryBasic inv = new net.minecraft.inventory.InventoryBasic("test", false, 1);
        net.minecraft.inventory.Slot slot = new net.minecraft.inventory.Slot(inv, 0, 0, 0);
        slot.putStack(undamaged.copy());

        assertThat(net.minecraft.inventory.Container.canAddItemToSlot(slot, undamaged, true)).isTrue();
        assertThat(net.minecraft.inventory.Container.canAddItemToSlot(slot, damaged, true)).isFalse();

        slot.putStack(damaged.copy());
        assertThat(net.minecraft.inventory.Container.canAddItemToSlot(slot, undamaged, true)).isFalse();
    }

    @Test
    void maxDamageRemovalAndPlacementOrientationAndChunkPropertyRetentionWork() throws Exception {
        // Pass 1: [!max_damage] makes a diamond sword non-damageable, and [damage=10,max_damage=500] works on a stick
        ItemStack unbreakableSword = new ItemStack(Items.DIAMOND_SWORD);
        com.cleanroommc.component.DataComponentPredicate.parseBracketSpec("[!max_damage]").applyTo(unbreakableSword);
        assertThat(unbreakableSword.getMaxDamage()).isEqualTo(0);
        assertThat(unbreakableSword.isItemStackDamageable()).isFalse();

        ItemStack damageableStick = new ItemStack(Items.STICK);
        com.cleanroommc
            .component
            .DataComponentPredicate
            .parseBracketSpec("[damage=10,max_damage=500]")
            .applyTo(damageableStick);
        assertThat(damageableStick.getMaxDamage()).isEqualTo(500);
        assertThat(damageableStick.getDamageValue()).isEqualTo(10);

        // Pass 2: ItemStackHandler refuses to merge two damaged stacks even when MAX_STACK_SIZE > 1
        ItemStack damagedSword1 = new ItemStack(Items.DIAMOND_SWORD);
        damagedSword1.set(com.cleanroommc.component.DataComponents.MAX_STACK_SIZE, 16);
        damagedSword1.setDamageValue(5);
        ItemStack damagedSword2 = damagedSword1.copy();
        net.minecraftforge.items.ItemStackHandler handler = new net.minecraftforge.items.ItemStackHandler(1);
        assertThat(handler.insertItem(0, damagedSword1, false).isEmpty()).isTrue();
        assertThat(handler.insertItem(0, damagedSword2, false).isEmpty()).isFalse();

        // Pass 3: Block.getStateForPlacement(..., ItemStack, ...) runs slab/log orientation when no explicit BLOCK_STATE component is set
        ItemStack slabStack = new ItemStack(net.minecraft.init.Blocks.STONE_SLAB, 1, 0);
        net.minecraft.block.state.IBlockState topSlab = net.minecraft.init.Blocks.STONE_SLAB.getStateForPlacement(null,
            net.minecraft.util.math.BlockPos.ORIGIN, net.minecraft.util.EnumFacing.DOWN, 0.5F, 0.8F, 0.5F, slabStack,
            null, net.minecraft.util.EnumHand.MAIN_HAND
        );
        assertThat(topSlab.getValue(net.minecraft.block.BlockSlab.HALF
        )).isEqualTo(net.minecraft.block.BlockSlab.EnumBlockHalf.TOP);

        // Pass 4: SectionBlockFormat retains a registered block's state string when a property value is from an absent mod
        NBTTagCompound chunkTag = new NBTTagCompound();
        NBTTagList paletteList = new NBTTagList();
        paletteList.appendTag(new net.minecraft.nbt.NBTTagString("minecraft:wool[color=absent_mod_color]"));
        chunkTag.setTag(SectionBlockFormat.CHUNK_PALETTE, paletteList);
        SectionBlockFormat.ChunkPaletteRead paletteRead = SectionBlockFormat.readChunkPalette(chunkTag);
        assertThat(paletteRead.unresolved(0)).isEqualTo("minecraft:wool[color=absent_mod_color]");
        assertThat(paletteRead.standIn(0)).isEqualTo(net.minecraft.init.Blocks.WOOL.getDefaultState());
    }

    @Test
    void carriedBlockStateCombinesWithPlacementOrientationAndStatListKeepsFurnaceStats() {
        // Round 22 & 23: A slab or log carrying a BLOCK_STATE variant still receives placement orientation (HALF=TOP, LOG_AXIS=X)
        ItemStack quartzSlab = new ItemStack(net.minecraft.init.Blocks.STONE_SLAB, 1, 0);
        net.minecraft.block.state.IBlockState quartzBottom = net.minecraft
            .init
            .Blocks
            .STONE_SLAB
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockStoneSlab.VARIANT, net.minecraft.block.BlockStoneSlab.EnumType.QUARTZ
            )
            .withProperty(net.minecraft.block.BlockSlab.HALF, net.minecraft.block.BlockSlab.EnumBlockHalf.BOTTOM);
        quartzSlab.getComponents().set(com.cleanroommc.component.DataComponents.BLOCK_STATE, quartzBottom);
        assertThat(net.minecraft.init.Blocks.STONE_SLAB.getTypeForItem(quartzSlab
        )).isEqualTo(net.minecraft.block.BlockStoneSlab.EnumType.QUARTZ);

        net.minecraft.block.state.IBlockState placedSlab = net.minecraft.init.Blocks.STONE_SLAB.getStateForPlacement(null,
            net.minecraft.util.math.BlockPos.ORIGIN, net.minecraft.util.EnumFacing.DOWN, 0.5F, 0.8F, 0.5F, quartzSlab,
            null, net.minecraft.util.EnumHand.MAIN_HAND
        );
        assertThat(placedSlab.getValue(net.minecraft.block.BlockStoneSlab.VARIANT
        )).isEqualTo(net.minecraft.block.BlockStoneSlab.EnumType.QUARTZ);
        assertThat(placedSlab.getValue(net.minecraft.block.BlockSlab.HALF
        )).isEqualTo(net.minecraft.block.BlockSlab.EnumBlockHalf.TOP);

        // Round 24: StatList.reinit() does not overwrite Items.AIR (slot 0) or remove furnace/slab stats from ALL_STATS
        net.minecraft.stats.StatList.reinit();
        assertThat(net.minecraft.stats.StatList.getCraftStats(Items.AIR)).isNull();
        net.minecraft.stats.StatBase furnaceUse = net.minecraft.stats.StatList.getObjectUseStats(
            net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.FURNACE));
        assertThat(furnaceUse).isNotNull();
        assertThat(net.minecraft.stats.StatList.ALL_STATS).contains(furnaceUse);
    }

    @Test
    void nbtUtilAreNbtEqualsMatchesByteShortIntAndTallGrassBlockStateSyncsMetadata() {
        // Round 27: NBTUtil.areNBTEquals matches short/byte tags against widened int tags (e.g. enchantment id, Damage, potion Id)
        NBTTagCompound expectedShort = new NBTTagCompound();
        expectedShort.setShort("id", (short) 16);
        NBTTagCompound actualInt = new NBTTagCompound();
        actualInt.setInteger("id", 16);
        assertThat(net.minecraft.nbt.NBTUtil.areNBTEquals(expectedShort, actualInt, true)).isTrue();

        // Round 25: Setting BLOCK_STATE on Blocks.TALLGRASS (where damageDropped is 0 for seeds) syncs itemDamage to getMetaFromState (2 for FERN)
        ItemStack fernStack = new ItemStack(net.minecraft.init.Blocks.TALLGRASS, 1, 0);
        net.minecraft.block.state.IBlockState fernState = net.minecraft
            .init
            .Blocks
            .TALLGRASS
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockTallGrass.TYPE, net.minecraft.block.BlockTallGrass.EnumType.FERN);
        fernStack.set(com.cleanroommc.component.DataComponents.BLOCK_STATE, fernState);
        assertThat(fernStack.getMetadata()).isEqualTo(net.minecraft.block.BlockTallGrass.EnumType.FERN.getMeta());
        assertThat(fernStack.getTranslationKey()).endsWith(".fern");
    }

    @Test
    void flowerPotRetainsMissingModPlantAndMobSpawnerDropsRemovedNamespace() {
        // FlowerPot retains an unregistered mod item and its components when the namespace is not removed
        NBTTagCompound potNbt = new NBTTagCompound();
        potNbt.setString("id", "minecraft:flower_pot");
        potNbt.setInteger("x", 1);
        potNbt.setInteger("y", 2);
        potNbt.setInteger("z", 3);
        potNbt.setString("Item", "absent_flower_mod:blue_rose");
        potNbt.setInteger("Data", 4);
        NBTTagCompound compTag = new NBTTagCompound();
        compTag.setInteger("cleanroom:max_stack_size", 16);
        potNbt.setTag("Components", compTag);

        net.minecraft.tileentity.TileEntityFlowerPot pot = new net.minecraft.tileentity.TileEntityFlowerPot();
        pot.readFromNBT(potNbt);
        assertThat(pot.getFlowerPotItem()).isNull();
        NBTTagCompound savedPot = pot.writeToNBT(new NBTTagCompound());
        assertThat(savedPot.getString("Item")).isEqualTo("absent_flower_mod:blue_rose");
        assertThat(savedPot.getInteger("Data")).isEqualTo(4);
        assertThat(savedPot.getCompoundTag("Components").getInteger("cleanroom:max_stack_size")).isEqualTo(16);

        // When the user removes the namespace, FlowerPot clears the removed plant on read
        RetainedContent.removedNamespaces().add("absent_flower_mod");
        try {
            net.minecraft.tileentity.TileEntityFlowerPot clearedPot = new net.minecraft.tileentity.TileEntityFlowerPot();
            clearedPot.readFromNBT(savedPot);
            NBTTagCompound savedCleared = clearedPot.writeToNBT(new NBTTagCompound());
            assertThat(savedCleared.getString("Item")).isEmpty();
            assertThat(savedCleared.hasKey("Components")).isFalse();
        } finally {
            RetainedContent.removedNamespaces().remove("absent_flower_mod");
        }
    }

    @Test
    void beaconAndSetEnchantmentsRetainMissingModNames() {

        // EnchantmentHelper.setEnchantments preserves unresolved missing-mod enchantments on the target stack
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        NBTTagList enchList = new NBTTagList();
        NBTTagCompound missingEnch = new NBTTagCompound();
        missingEnch.setInteger("id", 999);
        missingEnch.setString("name", "absent_ench_mod:soulbound");
        missingEnch.setShort("lvl", (short) 1);
        enchList.appendTag(missingEnch);
        sword.setTagInfo("ench", enchList);

        java.util.Map<Enchantment, Integer> liveEnchs = EnchantmentHelper.getEnchantments(sword);
        liveEnchs.put(Enchantments.SHARPNESS, 5);
        EnchantmentHelper.setEnchantments(liveEnchs, sword);

        NBTTagList updatedList = sword.getEnchantmentTagList();
        assertThat(updatedList.tagCount()).isEqualTo(2);
        assertThat(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, sword)).isEqualTo(5);
        boolean foundRetained = false;
        for (int i = 0; i < updatedList.tagCount(); i++) {
            if ("absent_ench_mod:soulbound".equals(updatedList.getCompoundTagAt(i).getString("name"))) {
                foundRetained = true;
            }
        }
        assertThat(foundRetained).isTrue();
    }

    @Test
    void spawnEggDisplaysMissingModEntityIdInName() {
        ItemStack egg = new ItemStack(Items.SPAWN_EGG);
        net.minecraft.item.ItemMonsterPlacer.applyEntityIdToItemStack(egg,
            new net.minecraft.util.ResourceLocation("absent_mob_mod", "crystal_golem")
        );
        assertThat(egg.getDisplayName()).contains("absent_mob_mod:crystal_golem");
    }

    @Test
    void merchantRecipeDisablesMissingItemTradeAndPrunesRemovedNamespaceTrade() {
        if (MissingItem.INSTANCE == null) {
            MissingItem.INSTANCE = new MissingItem();
            net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(MissingItem.INSTANCE);
        }
        NBTTagCompound buyTag = new ItemStack(Items.EMERALD, 3).writeToNBT(new NBTTagCompound());
        NBTTagCompound sellTag = new NBTTagCompound();
        sellTag.setString("id", "absent_trade_mod:magic_wand");
        sellTag.setByte("Count", (byte) 1);
        sellTag.setInteger("Damage", 0);

        NBTTagCompound recipeTag = new NBTTagCompound();
        recipeTag.setTag("buy", buyTag);
        recipeTag.setTag("sell", sellTag);
        recipeTag.setInteger("uses", 0);
        recipeTag.setInteger("maxUses", 7);

        // While the mod is absent (retained), the trade is disabled in UI but preserves its MissingItem sell stack and toolUses=0
        net.minecraft.village.MerchantRecipe retainedRecipe = new net.minecraft.village.MerchantRecipe(recipeTag);
        assertThat(retainedRecipe.getItemToSell().getItem()).isSameAs(MissingItem.INSTANCE);
        assertThat(retainedRecipe.isRecipeDisabled()).isTrue();
        assertThat(retainedRecipe.getToolUses()).isEqualTo(0);
        assertThat(retainedRecipe.writeToTags().getCompoundTag("sell").getString("id")
        ).isEqualTo("absent_trade_mod:magic_wand");

        // When the user removes the namespace, MerchantRecipeList prunes the emptied trade
        NBTTagCompound offersTag = new NBTTagCompound();
        NBTTagList recipesList = new NBTTagList();
        recipesList.appendTag(recipeTag.copy());
        offersTag.setTag("Recipes", recipesList);

        RetainedContent.removedNamespaces().add("absent_trade_mod");
        try {
            net.minecraft.village.MerchantRecipeList list = new net.minecraft.village.MerchantRecipeList(offersTag);
            assertThat(list).isEmpty();
        } finally {
            RetainedContent.removedNamespaces().remove("absent_trade_mod");
        }
    }

    @Test
    void missingItemMovedBetweenInventorySlotsKeepsNewSlotAndOreDictionaryMatchesBlockState() {
        if (MissingItem.INSTANCE == null) {
            MissingItem.INSTANCE = new MissingItem();
            net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(MissingItem.INSTANCE);
        }

        // Round 43: Moving a MissingItem loaded from Slot 0 to Slot 5 in an inventory does not overwrite Slot 5 with Slot 0 on save
        NBTTagCompound slot0Tag = new NBTTagCompound();
        slot0Tag.setByte("Slot", (byte) 0);
        slot0Tag.setString("id", "absent_chest_mod:ruby");
        slot0Tag.setByte("Count", (byte) 4);
        slot0Tag.setInteger("Damage", 0);

        ItemStack missingRuby = new ItemStack(slot0Tag);
        assertThat(missingRuby.getDisplayName()).contains("absent_chest_mod:ruby");

        net.minecraft.util.NonNullList<ItemStack> chestSlots = net.minecraft.util.NonNullList.withSize(9,
            ItemStack.EMPTY
        );
        chestSlots.set(5, missingRuby); // Moved from slot 0 to slot 5
        NBTTagCompound savedChest = net.minecraft.inventory.ItemStackHelper.saveAllItems(new NBTTagCompound(),
            chestSlots
        );

        net.minecraft.util.NonNullList<ItemStack> reloadedSlots = net.minecraft.util.NonNullList.withSize(9,
            ItemStack.EMPTY
        );
        net.minecraft.inventory.ItemStackHelper.loadAllItems(savedChest, reloadedSlots);
        assertThat(reloadedSlots.get(0).isEmpty()).isTrue();
        assertThat(reloadedSlots.get(5).getItem()).isSameAs(MissingItem.INSTANCE);
        assertThat(reloadedSlots.get(5).getCount()).isEqualTo(4);

        // Round 44: OreDictionary.itemMatches distinguishes non-legacy BLOCK_STATE components
        ItemStack oakLogY = new ItemStack(net.minecraft.init.Blocks.LOG, 1, 0);
        ItemStack oakLogX = new ItemStack(net.minecraft.init.Blocks.LOG, 1, 0);
        oakLogX.set(com.cleanroommc.component.DataComponents.BLOCK_STATE,
            net.minecraft
                .init
                .Blocks
                .LOG
                .getDefaultState()
                .withProperty(net.minecraft.block.BlockLog.LOG_AXIS, net.minecraft.block.BlockLog.EnumAxis.X)
        );
        assertThat(net.minecraftforge.oredict.OreDictionary.itemMatches(oakLogY, oakLogX, false)).isFalse();
        assertThat(net.minecraftforge.oredict.OreDictionary.itemMatches(oakLogX, oakLogX.copy(), false)).isTrue();
    }

    @Test
    void furnaceAndIngredientDistinguishDefaultMetaZeroFromPatchedBlockState() {
        ItemStack oakLogY = new ItemStack(net.minecraft.init.Blocks.LOG, 1, 0);
        ItemStack oakLogX = new ItemStack(net.minecraft.init.Blocks.LOG, 1, 0);
        oakLogX.set(com.cleanroommc.component.DataComponents.BLOCK_STATE,
            net.minecraft
                .init
                .Blocks
                .LOG
                .getDefaultState()
                .withProperty(net.minecraft.block.BlockLog.LOG_AXIS, net.minecraft.block.BlockLog.EnumAxis.X)
        );

        net.minecraft.item.crafting.Ingredient onlyOakY = net.minecraft.item.crafting.Ingredient.fromStacks(oakLogY);
        assertThat(onlyOakY.apply(oakLogY)).isTrue();
        assertThat(onlyOakY.apply(oakLogX)).isFalse();

        net.minecraft.item.crafting.Ingredient wildcardLog = net.minecraft.item.crafting.Ingredient.fromItem(
            net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.LOG));
        assertThat(wildcardLog.apply(oakLogY)).isTrue();
        assertThat(wildcardLog.apply(oakLogX)).isTrue();
    }

    @Test
    void legacyChunkBiomesRetainsMissingModBiomeFromLegacyIdsTable() {
        NBTTagCompound registries = new NBTTagCompound();
        NBTTagCompound biomesReg = new NBTTagCompound();
        NBTTagList idsList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("K", "absent_biome_mod:mystic_grove");
        entry.setInteger("V", 88);
        idsList.appendTag(entry);
        biomesReg.setTag("ids", idsList);
        registries.setTag("minecraft:biomes", biomesReg);

        LegacyIds.install(registries);
        try {
            net.minecraft.world.chunk.Chunk chunk = new net.minecraft.world.chunk.Chunk(null, 0, 0);
            byte[] legacyBytes = new byte[256];
            java.util.Arrays.fill(legacyBytes, (byte) 88);
            chunk.getBiomeIds().setLegacy(legacyBytes);

            NBTTagCompound savedChunk = new NBTTagCompound();
            chunk.getBiomeIds().writeNBT(savedChunk);
            NBTTagList palette = savedChunk.getTagList(ChunkBiomes.PALETTE, 8);
            assertThat(palette.tagCount()).isEqualTo(1);
            assertThat(palette.getStringTagAt(0)).isEqualTo("absent_biome_mod:mystic_grove");
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    void legacyAndJeidSectionMigrationRetainsMissingModBlockAndMetadataFromLegacyIds() {
        NBTTagCompound registries = new NBTTagCompound();
        NBTTagCompound blocksReg = new NBTTagCompound();
        NBTTagList idsList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("K", "absent_ore_mod:multi_ore");
        entry.setInteger("V", 250);
        idsList.appendTag(entry);
        blocksReg.setTag("ids", idsList);
        registries.setTag("minecraft:blocks", blocksReg);

        LegacyIds.install(registries);
        try {
            byte[] blockIds = new byte[4096];
            blockIds[0] = (byte) 250;
            net.minecraft.world.chunk.NibbleArray dataNibbles = new net.minecraft.world.chunk.NibbleArray();
            dataNibbles.set(0, 0, 0, 3); // meta = 3
            net.minecraft.world.chunk.BlockStateContainer container = new net.minecraft.world.chunk.BlockStateContainer();
            RetainedContent.Section retained = new RetainedContent.Section();

            SectionBlockFormat.readLegacy(blockIds, dataNibbles, null, container, retained, null);
            assertThat(retained.isEmpty()).isFalse();
            assertThat(retained.name(0)).isEqualTo("absent_ore_mod:multi_ore[3]");

            // And when a block is registered, SectionBlockFormat.parse("[<meta>]") resolves getStateFromMeta(meta)
            assertThat(SectionBlockFormat.parse("minecraft:wool[14]")).isEqualTo(
                net.minecraft.init.Blocks.WOOL.getStateFromMeta(14));
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    void legacyBeaconAndEnchantmentHelperRetainMissingModNamesFromLegacyIdsTable() {
        NBTTagCompound registries = new NBTTagCompound();

        NBTTagCompound potionsReg = new NBTTagCompound();
        NBTTagList potIds = new NBTTagList();
        NBTTagCompound potEntry = new NBTTagCompound();
        potEntry.setString("K", "absent_potion_mod:haste_plus");
        potEntry.setInteger("V", 91);
        potIds.appendTag(potEntry);
        potionsReg.setTag("ids", potIds);
        registries.setTag("minecraft:potions", potionsReg);

        NBTTagCompound enchReg = new NBTTagCompound();
        NBTTagList enchIds = new NBTTagList();
        NBTTagCompound enchEntry = new NBTTagCompound();
        enchEntry.setString("K", "absent_ench_mod:xp_boost");
        enchEntry.setInteger("V", 145);
        enchIds.appendTag(enchEntry);
        enchReg.setTag("ids", enchIds);
        registries.setTag("minecraft:enchantments", enchReg);

        LegacyIds.install(registries);
        try {
            // LegacyIds.potionName resolves the saved registry name even when the potion mod is absent
            assertThat(LegacyIds.potionName(91)).isEqualTo(new net.minecraft.util.ResourceLocation("absent_potion_mod",
                "haste_plus"
            ));
            assertThat(LegacyIds.enchantmentName(145)).isEqualTo(
                new net.minecraft.util.ResourceLocation("absent_ench_mod", "xp_boost"));

            // Legacy ItemStack enchantment tag has only short "id", no "name"
            ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
            NBTTagList enchList = new NBTTagList();
            NBTTagCompound legacyEnch = new NBTTagCompound();
            legacyEnch.setShort("id", (short) 145);
            legacyEnch.setShort("lvl", (short) 2);
            enchList.appendTag(legacyEnch);
            pick.setTagInfo("ench", enchList);

            Map<Enchantment, Integer> liveEnchs = EnchantmentHelper.getEnchantments(pick);
            liveEnchs.put(Enchantments.EFFICIENCY, 4);
            EnchantmentHelper.setEnchantments(liveEnchs, pick);

            NBTTagList afterList = pick.getEnchantmentTagList();
            assertThat(afterList.tagCount()).isEqualTo(2);
            boolean foundRetained = false;
            for (int i = 0; i < afterList.tagCount(); i++) {
                if ("absent_ench_mod:xp_boost".equals(afterList.getCompoundTagAt(i).getString("name"))) {
                    foundRetained = true;
                }
            }
            assertThat(foundRetained).isTrue();
        } finally {
            LegacyIds.clear();
        }
    }

}
