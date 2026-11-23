/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.component;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemCoal;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
class ItemStateTest {

    private static final PropertyInteger TIER = PropertyInteger.create("tier", 0, 3);

    private static Item tieredItem;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
        if (tieredItem == null) {
            tieredItem = new Item() {

                @Override
                protected ItemStateContainer createItemState() {
                    return new ItemStateContainer(this, TIER);
                }

            };
            tieredItem.setRegistryName(new ResourceLocation("test", "tiered_item"));
            ForgeRegistries.ITEMS.register(tieredItem);
        }
    }

    @Test
    void itemStateIdsAreDenseAndConsecutive() {
        int total = Item.ITEM_STATE_IDS.size();
        assertThat(total > 16).isTrue();
        for (int i = 0; i < total; i++) {
            IItemState state = Item.getStateById(i);
            assertThat(state).isNotNull();
            assertThat(state.getStateId()).isEqualTo(i);
            assertThat(Item.getStateId(state)).isEqualTo(i);
        }
    }

    @Test
    void vanillaSubtypeItemsDeclareStatesAndKeepTemplateIdentity() {
        // Dye: 16 states, all with getItem() == Items.DYE
        assertThat(Items.DYE.getItemState().getValidStates().size()).isEqualTo(16);
        IItemState lapisState = Items.DYE.getDefaultItemState().withProperty(ItemDye.COLOR, EnumDyeColor.BLUE);
        assertThat(lapisState.getItem()).isSameAs(Items.DYE);
        assertThat(lapisState.toString()).isEqualTo("minecraft:dye[color=blue]");

        ItemStack lapis = new ItemStack(Items.DYE);
        lapis.setItemState(lapisState);
        assertThat(lapis.getItemState()).isSameAs(lapisState);
        assertThat(lapis.getItem()).isSameAs(Items.DYE);
        assertThat(lapis.getMetadata()).isEqualTo(EnumDyeColor.BLUE.getDyeDamage());

        // Coal / Charcoal
        assertThat(Items.COAL.getItemState().getValidStates().size()).isEqualTo(2);
        IItemState charcoalState = Items.COAL
            .getDefaultItemState()
            .withProperty(ItemCoal.TYPE, ItemCoal.CoalType.CHARCOAL);
        ItemStack charcoal = new ItemStack(Items.COAL);
        charcoal.setItemState(charcoalState);
        assertThat(charcoal.getItemState()).isSameAs(charcoalState);
        assertThat(charcoal.getMetadata()).isEqualTo(1);
        assertThat(Items.COAL.getTranslationKey(charcoal)).isEqualTo("item.charcoal");

        // Fish, Skull, Golden Apple
        assertThat(Items.FISH.getItemState().getValidStates().size()).isEqualTo(4);
        assertThat(new ItemStack(Items.FISH, 1, 3).getItemState().getValue(ItemFishFood.TYPE)
        ).isEqualTo(ItemFishFood.FishType.PUFFERFISH);
        assertThat(Items.SKULL.getItemState().getValidStates().size()).isEqualTo(6);
        assertThat(new ItemStack(Items.SKULL, 1, 5).getItemState().getValue(ItemSkull.TYPE)
        ).isEqualTo(ItemSkull.SkullType.DRAGON);
        assertThat(Items.GOLDEN_APPLE.getItemState().getValidStates().size()).isEqualTo(2);
        assertThat(new ItemStack(Items.GOLDEN_APPLE, 1, 1).getItemState().getValue(ItemAppleGold.ENCHANTED)).isTrue();
        assertThat(new ItemStack(Items.BED, 1, 14).getItemState().getValue(net.minecraft.item.ItemBed.COLOR)
        ).isEqualTo(EnumDyeColor.RED);
        assertThat(net.minecraft.item.ItemBanner.getBaseColor(new ItemStack(Items.BANNER, 1, 4
        ))).isEqualTo(EnumDyeColor.BLUE);
    }

    @Test
    void standaloneItemStateRoundTripsThroughNbtWithoutMetadata() {
        IItemState tier3 = tieredItem.getDefaultItemState().withProperty(TIER, 3);
        assertThat(tier3.getOrdinal()).isEqualTo(3);
        ItemStack stack = new ItemStack(tieredItem);
        stack.setItemState(tier3);
        assertThat(stack.getItemState()).isSameAs(tier3);

        ItemStack reloaded = new ItemStack(stack.writeToNBT(new NBTTagCompound()));
        assertThat(reloaded.getItemState()).isSameAs(tier3);
        assertThat(reloaded.getItemState().getItem()).isSameAs(tieredItem);
    }

}
