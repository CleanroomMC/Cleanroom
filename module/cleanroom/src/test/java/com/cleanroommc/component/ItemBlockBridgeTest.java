/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.component;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import static org.assertj.core.api.Assertions.assertThat;

/** The ItemBlock bridge: one block is one id; stackFor carries only the legacy number. */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class ItemBlockBridgeTest {

    private static final PropertyInteger LAYER = PropertyInteger.create("layer", 0, 3);

    private static Block layered;
    private static Block plain;
    private static ItemBlock layeredItem;
    private static ItemBlock plainItem;

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
        if (layered == null) {
            layered = new LayeredBlock();
            layered.setRegistryName(new ResourceLocation("test", "layered"));
            ForgeRegistries.BLOCKS.register(layered);
            layeredItem = new ItemBlock(layered);
            layeredItem.setRegistryName(new ResourceLocation("test", "layered"));
            ForgeRegistries.ITEMS.register(layeredItem);

            plain = new Block(Material.ROCK);
            plain.setRegistryName(new ResourceLocation("test", "plain"));
            ForgeRegistries.BLOCKS.register(plain);
            plainItem = new ItemBlock(plain);
            plainItem.setRegistryName(new ResourceLocation("test", "plain"));
            ForgeRegistries.ITEMS.register(plainItem);
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void stackForCarriesOnlyDamageDropped() {
        IBlockState state = layered.getDefaultState().withProperty(LAYER, 2);
        ItemStack stack = layeredItem.stackFor(state);
        assertThat(stack.getItemDamage()).isEqualTo(2); // legacy number follows damageDropped
        assertThat(stack.getComponents().asPatch().isEmpty()).isTrue(); // and nothing else
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getItemAndDropsUseStackFor() {
        IBlockState state = layered.getDefaultState().withProperty(LAYER, 3);
        ItemStack picked = layered.getItem(null, BlockPos.ORIGIN, state);
        assertThat(picked.getItemDamage()).isEqualTo(3);
        assertThat(picked.getComponents().asPatch().isEmpty()).isTrue();

        NonNullList<ItemStack> drops = NonNullList.create();
        layered.getDrops(drops, null, BlockPos.ORIGIN, state, 0);
        assertThat(drops.size()).isEqualTo(1);
        assertThat(drops.get(0).getItemDamage()).isEqualTo(3);
        assertThat(drops.get(0).getComponents().asPatch().isEmpty()).isTrue();
    }

    @Test
    public void placementFollowsTheMetaPath() {
        IBlockState state = layered.getDefaultState().withProperty(LAYER, 2);
        ItemStack stack = layeredItem.stackFor(state);
        IBlockState placed = layered.getStateForPlacement(null, BlockPos.ORIGIN, EnumFacing.UP, 0.5F, 0.5F, 0.5F, stack,
            null, EnumHand.MAIN_HAND
        );
        // the stack's L resolves through ItemBlock.getMetadata -> getStateFromMeta, nothing else
        assertThat(placed).isEqualTo(layered.getDefaultState());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void theLegacyShapeIsUnchanged() {
        IBlockState state = plain.getDefaultState();
        ItemStack viaStackFor = plainItem.stackFor(state);
        ItemStack legacy = new ItemStack(plainItem, 1, 0);
        assertThat(viaStackFor.getComponents().asPatch().isEmpty()).isTrue();
        assertThat(viaStackFor.writeToNBT(new NBTTagCompound())).isEqualTo(legacy.writeToNBT(new NBTTagCompound()));

        IBlockState placed = plain.getStateForPlacement(null, BlockPos.ORIGIN, EnumFacing.UP, 0.5F, 0.5F, 0.5F,
            viaStackFor, null, EnumHand.MAIN_HAND
        );
        assertThat(placed).isEqualTo(state);
    }

    @Test
    public void standaloneStateRoundTripsThroughStackAndPlacement() {
        IBlockState state = layered.getDefaultState().withProperty(LAYER, 3);
        assertThat(state.getStateId()).isEqualTo(Block.getStateId(state));
        ItemStack stack = new ItemStack(layeredItem);
        stack.set(DataComponents.BLOCK_STATE, state);
        ItemStack reloaded = new ItemStack(stack.writeToNBT(new NBTTagCompound()));
        assertThat(reloaded.get(DataComponents.BLOCK_STATE)).isEqualTo(state);
        IBlockState placed = layered.getStateForPlacement(null, BlockPos.ORIGIN, EnumFacing.UP, 0.5F, 0.5F, 0.5F,
            reloaded, null, EnumHand.MAIN_HAND
        );
        assertThat(placed).isEqualTo(state);

        // And on a standard template block like wool, every ItemBlock stack answers BLOCK_STATE with its IBlockState
        IBlockState redWool = net.minecraft
            .init
            .Blocks
            .WOOL
            .getDefaultState()
            .withProperty(net.minecraft.block.BlockColored.COLOR, net.minecraft.item.EnumDyeColor.RED);
        ItemStack woolStack = new ItemStack(net.minecraft.init.Blocks.WOOL);
        woolStack.set(DataComponents.BLOCK_STATE, redWool);
        assertThat(woolStack.get(DataComponents.BLOCK_STATE)).isEqualTo(redWool);
        assertThat(woolStack.get(DataComponents.BLOCK_STATE).getBlock()).isEqualTo(net.minecraft.init.Blocks.WOOL);
        assertThat(woolStack.getMetadata()).isEqualTo(14);
    }

    @SuppressWarnings("deprecation")
    private static class LayeredBlock extends Block {

        LayeredBlock() {
            super(Material.ROCK);
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, LAYER);
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            return 0; // meta deliberately does NOT encode the property: nothing carries it to the item
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return getDefaultState();
        }

        @Override
        public int damageDropped(IBlockState state) {
            return state.getValue(LAYER);
        }

    }

}
