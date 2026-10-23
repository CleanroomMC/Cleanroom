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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
public class SavedNamesTest {

    private static final ResourceLocation POTION_NAME = new ResourceLocation("wideids_test", "wide_effect");
    private static final ResourceLocation ENCH_NAME = new ResourceLocation("wideids_test", "wide_ench");

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private static Potion widePotion() {
        if (!ForgeRegistries.POTIONS.containsKey(POTION_NAME)) {
            Potion potion = new Potion(false, 0) { };
            potion.setRegistryName(POTION_NAME);
            ForgeRegistries.POTIONS.register(potion);
        }
        return ForgeRegistries.POTIONS.getValue(POTION_NAME);
    }

    private static Enchantment wideEnchantment() {
        if (!ForgeRegistries.ENCHANTMENTS.containsKey(ENCH_NAME)) {
            Enchantment enchantment = new Enchantment(Enchantment.Rarity.RARE, EnumEnchantmentType.ALL,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND }
            ) { };
            enchantment.setRegistryName(ENCH_NAME);
            ForgeRegistries.ENCHANTMENTS.register(enchantment);
        }
        return ForgeRegistries.ENCHANTMENTS.getValue(ENCH_NAME);
    }

    @Test
    public void potionEffectRoundTripsByName() {
        Potion potion = widePotion();
        NBTTagCompound tag = new PotionEffect(potion, 100, 1).writeCustomPotionEffectToNBT(new NBTTagCompound());
        assertThat(tag.hasKey("Name", 8)).isTrue();
        assertThat(tag.getString("Name")).isEqualTo("wideids_test:wide_effect");
        assertThat(tag.hasKey("Id")).isTrue();
        assertThat(PotionEffect.readCustomPotionEffectFromNBT(tag).getPotion()).isSameAs(potion);

        // An id that meant the modded potion last session can mean a vanilla one now: the name wins
        tag.setInteger("Id", Potion.getIdFromPotion(MobEffects.SPEED));
        assertThat(PotionEffect.readCustomPotionEffectFromNBT(tag).getPotion()).isSameAs(potion);
    }

    @Test
    public void potionEffectWithoutNameStillReadsById() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Id", (byte) Potion.getIdFromPotion(MobEffects.SPEED));
        PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(tag);
        assertThat(effect).isNotNull();
        assertThat(effect.getPotion()).isSameAs(MobEffects.SPEED);
    }

    @Test
    public void enchantmentRoundTripsByName() {
        Enchantment enchantment = wideEnchantment();
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.addEnchantment(enchantment, 1);
        NBTTagCompound enchTag = stack.getEnchantmentTagList().getCompoundTagAt(0);
        assertThat(enchTag.getString("name")).isEqualTo("wideids_test:wide_ench");
        assertThat(EnchantmentHelper.getEnchantments(stack).containsKey(enchantment)).isTrue();

        enchTag.setInteger("id", Enchantment.getEnchantmentID(Enchantments.SHARPNESS));
        assertThat(EnchantmentHelper.getEnchantments(stack).containsKey(enchantment)).isTrue();
        assertThat(EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.SHARPNESS)).isFalse();
    }

    @Test
    public void enchantmentWithoutNameStillReadsById() {
        NBTTagList list = new NBTTagList();
        NBTTagCompound enchTag = new NBTTagCompound();
        enchTag.setShort("id", (short) Enchantment.getEnchantmentID(Enchantments.SHARPNESS));
        enchTag.setShort("lvl", (short) 3);
        list.appendTag(enchTag);
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.setTagInfo("ench", list);
        assertThat((int) EnchantmentHelper.getEnchantments(stack).get(Enchantments.SHARPNESS)).isEqualTo(3);
    }

    @Test
    public void beaconEffectsRoundTripByName() {
        NBTTagCompound in = new NBTTagCompound();
        in.setInteger("Primary", Potion.getIdFromPotion(MobEffects.HASTE)); // wrong on purpose: the name wins
        in.setString("PrimaryName", "minecraft:speed");
        in.setInteger("Secondary", 0);
        TileEntityBeacon beacon = new TileEntityBeacon();
        beacon.readFromNBT(in);

        NBTTagCompound out = beacon.writeToNBT(new NBTTagCompound());
        assertThat(out.getString("PrimaryName")).isEqualTo("minecraft:speed");
        assertThat(out.getInteger("Primary")).isEqualTo(Potion.getIdFromPotion(MobEffects.SPEED));
        assertThat(out.hasKey("SecondaryName")).isFalse();
        assertThat(out.getInteger("Secondary")).isEqualTo(-1);

        TileEntityBeacon read = new TileEntityBeacon();
        read.readFromNBT(out);
        NBTTagCompound again = read.writeToNBT(new NBTTagCompound());
        assertThat(again.getInteger("Primary")).isEqualTo(Potion.getIdFromPotion(MobEffects.SPEED));
        assertThat(again.getInteger("Secondary")).isEqualTo(-1);
    }

    @Test
    public void unknownNameIsMissingNotTheId() {
        // An unresolvable name means the owning mod is gone: the numeric id must NOT be used,
        // it could now belong to a different registry entry.
        NBTTagCompound enchTag = new NBTTagCompound();
        enchTag.setString("name", "wideids_test:gone");
        enchTag.setInteger("id", Enchantment.getEnchantmentID(Enchantments.SHARPNESS));
        assertThat(EnchantmentHelper.enchantmentFrom(enchTag)).isNull();
        NBTTagList enchList = new NBTTagList();
        enchList.appendTag(enchTag);
        ItemStack goneEnchStack = new ItemStack(Items.DIAMOND_SWORD);
        goneEnchStack.setTagInfo("ench", enchList);
        assertThat(EnchantmentHelper.getEnchantments(goneEnchStack)).isEmpty();

        NBTTagCompound potionTag = new NBTTagCompound();
        potionTag.setString("Name", "wideids_test:gone");
        potionTag.setInteger("Id", Potion.getIdFromPotion(MobEffects.SPEED));
        assertThat(PotionEffect.readCustomPotionEffectFromNBT(potionTag)).isNull();

        NBTTagCompound beaconTag = new NBTTagCompound();
        beaconTag.setString("PrimaryName", "wideids_test:gone");
        beaconTag.setInteger("Primary", Potion.getIdFromPotion(MobEffects.SPEED));
        TileEntityBeacon beacon = new TileEntityBeacon();
        beacon.readFromNBT(beaconTag);
        NBTTagCompound out = beacon.writeToNBT(new NBTTagCompound());
        assertThat(out.getInteger("Primary")).isEqualTo(-1);
        assertThat(out.hasKey("PrimaryName")).isFalse();
    }

}
