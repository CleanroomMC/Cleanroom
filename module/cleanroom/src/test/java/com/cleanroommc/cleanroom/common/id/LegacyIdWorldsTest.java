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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
public class LegacyIdWorldsTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void jeidSectionRoundTripsThroughTheNumericPalette() {
        NBTTagCompound section = new NBTTagCompound();
        section.setByte("Y", (byte) 4);
        int stone = Block.getIdFromBlock(Blocks.STONE) << 4;
        int dirt = Block.getIdFromBlock(Blocks.DIRT) << 4 | 1;
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { stone, dirt });
        byte[] blocks = new byte[4096];
        byte[] data = new byte[2048];
        NibbleArray nibbles = new NibbleArray(data);
        blocks[0] = 0;
        blocks[1] = 0;
        nibbles.set(1, 0, 0, 1);
        section.setByteArray("Blocks", blocks);
        section.setByteArray("Data", data);
        assertThat(SectionBlockFormat.isJeidSection(section)).isTrue();
        assertThat(SectionBlockFormat.isNeidSection(section)).isFalse();
        BlockStateContainer container = new BlockStateContainer();
        SectionBlockFormat.readJeid(section, container);
        assertThat(container.get(0, 0, 0)).isEqualTo(Blocks.STONE.getDefaultState());
        assertThat(container.get(1, 0, 0)).isEqualTo(Blocks.DIRT.getStateFromMeta(1));
    }

    @Test
    public void neidSectionUsesAdd2() {
        NBTTagCompound section = new NBTTagCompound();
        byte[] blocks = new byte[4096];
        byte[] data = new byte[2048];
        byte[] add = new byte[2048];
        byte[] add2 = new byte[2048];
        int id = Block.getIdFromBlock(Blocks.OBSIDIAN);
        blocks[0] = (byte) (id & 0xFF);
        new NibbleArray(add).set(0, 0, 0, (id >> 8) & 15);
        new NibbleArray(add2).set(0, 0, 0, (id >> 12) & 15);
        section.setByteArray("Blocks", blocks);
        section.setByteArray("Data", data);
        section.setByteArray("Add", add);
        section.setByteArray("Add2", add2);
        assertThat(SectionBlockFormat.isNeidSection(section)).isTrue();
        BlockStateContainer container = new BlockStateContainer();
        SectionBlockFormat.readNeid(section, container);
        assertThat(container.get(0, 0, 0)).isEqualTo(Blocks.OBSIDIAN.getDefaultState());
    }

    @Test
    public void cleanroomPaletteIsNotTreatedAsJeid() {
        NBTTagCompound chunk = new NBTTagCompound();
        NBTTagList palette = new NBTTagList();
        palette.appendTag(new NBTTagString("minecraft:stone"));
        chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, palette);
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0 });
        assertThat(SectionBlockFormat.isPaletted(chunk)).isTrue();
        assertThat(SectionBlockFormat.isJeidSection(section)).isFalse();
    }

}
