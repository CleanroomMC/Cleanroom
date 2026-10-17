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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import static org.assertj.core.api.Assertions.assertThat;

/** Missing-item retention: an unreadable stack keeps its whole compound and writes it back verbatim. */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class MissingItemTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
        if (MissingItem.INSTANCE == null) {
            // The Register<Item> event does not fire in the test harness; register directly
            MissingItem.INSTANCE = new MissingItem();
            ForgeRegistries.ITEMS.register(MissingItem.INSTANCE);
        }
    }

    private static NBTTagCompound stack(String id) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("id", id);
        compound.setByte("Count", (byte) 3);
        compound.setInteger("Damage", 7);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Charge", 12);
        compound.setTag("tag", tag);
        return compound;
    }

    @Test
    public void unknownItemBecomesAMissingStackThatKeepsItsCompound() {
        NBTTagCompound compound = stack("gone_mod:wand");
        ItemStack stack = new ItemStack(compound);
        assertThat(stack.isEmpty()).isFalse();
        assertThat(stack.getItem()).isSameAs(MissingItem.INSTANCE);
        assertThat(stack.getCount()).isEqualTo(3);
        assertThat(stack.getTagCompound().getCompoundTag(MissingItem.RETAINED)).isEqualTo(compound);
    }

    @Test
    public void writeToNbtRestoresTheOriginalCompound() {
        NBTTagCompound compound = stack("gone_mod:dagger");
        ItemStack stack = new ItemStack(compound);
        assertThat(stack.writeToNBT(new NBTTagCompound())).isEqualTo(compound);
        ItemStack split = stack.copy();
        split.setCount(1);
        NBTTagCompound splitOut = split.writeToNBT(new NBTTagCompound());
        assertThat(splitOut.getByte("Count")).isEqualTo((byte) 1);
        splitOut.setByte("Count", (byte) 3);
        assertThat(splitOut).isEqualTo(compound);
    }

    @Test
    public void retainedItemReturnsWhenTheModDoes() {
        NBTTagCompound compound = stack("gone_mod:wand2");
        ItemStack stack = new ItemStack(compound);
        assertThat(stack.getItem()).isSameAs(MissingItem.INSTANCE);
        NBTTagCompound out = stack.writeToNBT(new NBTTagCompound());

        Item wand2 = new Item();
        wand2.setRegistryName(new ResourceLocation("gone_mod", "wand2"));
        ForgeRegistries.ITEMS.register(wand2);

        ItemStack back = new ItemStack(out);
        assertThat(back.getItem()).isSameAs(wand2);
        assertThat(back.getCount()).isEqualTo(3);
        assertThat(back.getItemDamage()).isEqualTo(7);
        assertThat(back.getTagCompound().getInteger("Charge")).isEqualTo(12);
    }

    @Test
    public void differentMissingStacksDoNotMerge() {
        ItemStack wand = new ItemStack(stack("gone_mod:wand3"));
        ItemStack dagger = new ItemStack(stack("gone_mod:dagger2"));
        ItemStack wandAgain = new ItemStack(stack("gone_mod:wand3"));
        assertThat(ItemStack.areItemStackTagsEqual(wand, dagger)).isFalse();
        assertThat(ItemStack.areItemStacksEqual(wand, dagger)).isFalse();
        assertThat(ItemStack.areItemStackTagsEqual(wand, wandAgain)).isTrue();
        assertThat(ItemStack.areItemStacksEqual(wand, wandAgain)).isTrue();
    }

    @Test
    public void airAndEmptyAreUntouched() {
        NBTTagCompound air = stack("minecraft:air");
        assertThat(new ItemStack(air).isEmpty()).isTrue();
        NBTTagCompound empty = new NBTTagCompound();
        assertThat(new ItemStack(empty).isEmpty()).isTrue();
        NBTTagCompound emptyId = new NBTTagCompound();
        emptyId.setString("id", "");
        assertThat(new ItemStack(emptyId).isEmpty()).isTrue();
    }

    @Test
    public void missingItemIsHiddenFromCreativeTabs() {
        assertThat(MissingItem.INSTANCE.getCreativeTab()).isNull();
    }

}
