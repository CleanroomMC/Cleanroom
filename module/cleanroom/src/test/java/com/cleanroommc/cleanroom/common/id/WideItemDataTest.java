/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

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
        tag.setByte("Id", (byte) id);
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
    void stateGetStateIdRefreshesAfterRegistryInvalidation() {
        net.minecraft.block.state.IBlockState stone = net.minecraft.init.Blocks.STONE.getDefaultState();
        int initialBlockStateId = stone.getStateId();
        ((net.minecraftforge.registries.BlockStateIdMap) net.minecraft.block.Block.BLOCK_STATE_IDS).invalidate();
        assertThat(stone.getStateId()).isEqualTo(initialBlockStateId);
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
    void outOfRangeBlockStateIdInPacketDefaultsToAirInsteadOfNull() throws Exception {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBlockPos(net.minecraft.util.math.BlockPos.ORIGIN);
        buf.writeVarInt(999_999_999);
        net.minecraft.network.play.server.SPacketBlockChange pkt = new net.minecraft.network.play.server.SPacketBlockChange();
        pkt.readPacketData(buf);
        assertThat(pkt.getBlockState()).isSameAs(net.minecraft.init.Blocks.AIR.getDefaultState());
    }

}
