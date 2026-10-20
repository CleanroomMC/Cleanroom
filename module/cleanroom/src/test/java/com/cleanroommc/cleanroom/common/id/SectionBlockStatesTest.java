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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The name format and section storage round-trip every vanilla block state. */
@ForgeTestRunner.Isolated
public class SectionBlockStatesTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private static List<IBlockState> allStates() {
        List<IBlockState> states = new ArrayList<>();
        for (Block block : Block.REGISTRY) {
            states.addAll(block.getBlockState().getValidStates());
        }
        return states;
    }

    @Test
    public void uniformAirClearsAReusedContainer() {
        BlockStateContainer data = new BlockStateContainer();
        data.set(3, 2, 1, Blocks.STONE.getDefaultState());
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0 });
        SectionBlockFormat.read(section, data, new IBlockState[] { Blocks.AIR.getDefaultState() });
        for (int i = 0; i < 4096; i++) {
            assertThat(data.get(i & 15, i >> 8, i >> 4 & 15)).isSameAs(Blocks.AIR.getDefaultState());
        }
    }

    @Test
    public void badIndicesDoNotClearTheExistingContainer() {
        BlockStateContainer data = new BlockStateContainer();
        data.set(0, 0, 0, Blocks.STONE.getDefaultState());
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0, 1 });
        long[] packed = new long[SectionBlockFormat.longCount(4)];
        packed[0] = 2;
        section.setTag(SectionBlockFormat.STATES, new NBTTagLongArray(packed));
        assertThatThrownBy(() -> SectionBlockFormat.read(section, data,
            new IBlockState[] { Blocks.AIR.getDefaultState(), Blocks.STONE.getDefaultState() }
        )).isInstanceOf(IllegalStateException.class);
        assertThat(data.get(0, 0, 0)).isSameAs(Blocks.STONE.getDefaultState());
    }

    @Test
    public void malformedPaletteTagsAreNotTreatedAsLegacy() {
        NBTTagCompound chunk = new NBTTagCompound();
        assertThat(SectionBlockFormat.isPaletted(chunk)).isFalse();
        chunk.setInteger(SectionBlockFormat.CHUNK_PALETTE, 1);
        assertThatThrownBy(() -> SectionBlockFormat.isPaletted(chunk)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> SectionBlockFormat.readChunkPalette(chunk)).isInstanceOf(IllegalStateException.class);
        NBTTagList wrongEntries = new NBTTagList();
        wrongEntries.appendTag(new NBTTagInt(0));
        chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, wrongEntries);
        assertThatThrownBy(() -> SectionBlockFormat.readChunkPalette(chunk)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void uniformWriteRemovesOldPackedStates() {
        NBTTagCompound section = new NBTTagCompound();
        section.setTag(SectionBlockFormat.STATES, new NBTTagLongArray(new long[SectionBlockFormat.longCount(4)]));
        SectionBlockFormat.write(new BlockStateContainer(), section, new SectionBlockFormat.ChunkPalette());
        assertThat(section.hasKey(SectionBlockFormat.STATES)).isFalse();
    }

    @Test
    public void namesRoundTripEveryState() {
        for (IBlockState state : allStates()) {
            assertThat(SectionBlockFormat.parse(SectionBlockFormat.toString(state))).isSameAs(state);
        }
        assertThat(SectionBlockFormat.toString(Blocks.STONE.getStateFromMeta(1
        ))).isEqualTo("minecraft:stone[variant=granite]");
        assertThat(SectionBlockFormat.parse("nomod:gone[x=1]")).isSameAs(Blocks.AIR.getDefaultState());
        assertThat(SectionBlockFormat.parse("minecraft:stone[variant=nope,unknown=1]"
        )).isSameAs(Blocks.STONE.getDefaultState());
        assertThat(SectionBlockFormat.parse("minecraft:stone[")).isSameAs(Blocks.STONE.getDefaultState());
        assertThat(SectionBlockFormat.parse("minecraft:stone[]")).isSameAs(Blocks.STONE.getDefaultState());
        assertThat(SectionBlockFormat.parse("minecraft:stone[variant=granite")).isSameAs(Blocks.STONE.getStateFromMeta(1
        ));
    }

    /* Sections with 1, 2, 16, 17, 256 and 300 distinct states (4-bit copy, 5..8-bit local palettes, global ids). */
    @Test
    public void sectionsRoundTripAtEveryPaletteWidth() {
        List<IBlockState> states = allStates();
        for (int distinct : new int[] { 1, 2, 16, 17, 33, 100, 256, 300, 1024 }) {
            BlockStateContainer data = new BlockStateContainer();
            for (int i = 0; i < 4096; i++) {
                data.set(i & 15, i >> 8, i >> 4 & 15, states.get(1 + i % distinct));
            }
            SectionBlockFormat.ChunkPalette palette = new SectionBlockFormat.ChunkPalette();
            NBTTagCompound section = new NBTTagCompound();
            SectionBlockFormat.write(data, section, palette);
            NBTTagCompound chunk = new NBTTagCompound();
            chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, palette.toNBT());
            BlockStateContainer back = new BlockStateContainer();
            SectionBlockFormat.read(section, back, SectionBlockFormat.readChunkPalette(chunk));
            for (int i = 0; i < 4096; i++) {
                assertThat(back.get(i & 15, i >> 8, i >> 4 & 15)).isSameAs(data.get(i & 15, i >> 8, i >> 4 & 15));
            }
        }
    }

    /* A uniform section loads at zero bits and keeps no packed storage; NBT round-trips the single state. */
    @Test
    public void uniformSectionLoadsAtZeroBits() {
        BlockStateContainer data = new BlockStateContainer();
        assertThat(data.getRawBits()).isEqualTo(0);
        assertThat(data.getRawStorage()).isNull();
        assertThat(data.get(5, 5, 5)).isSameAs(Blocks.AIR.getDefaultState());
        IBlockState stone = Blocks.STONE.getStateFromMeta(2);
        NBTTagCompound chunk = new NBTTagCompound();
        NBTTagList names = new NBTTagList();
        names.appendTag(new NBTTagString(SectionBlockFormat.toString(stone)));
        chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, names);
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0 });
        SectionBlockFormat.read(section, data, SectionBlockFormat.readChunkPalette(chunk));
        assertThat(data.getRawBits()).isEqualTo(0);
        assertThat(data.getRawStorage()).isNull();
        assertThat(data.get(0, 0, 0)).isSameAs(stone);
        assertThat(data.get(15, 15, 15)).isSameAs(stone);
        // A second distinct state grows to the 4-bit linear palette and never shrinks back
        data.set(0, 0, 0, Blocks.AIR.getDefaultState());
        assertThat(data.getRawBits()).isEqualTo(4);
        assertThat(data.get(0, 0, 0)).isSameAs(Blocks.AIR.getDefaultState());
        assertThat(data.get(15, 15, 15)).isSameAs(stone);
        SectionBlockFormat.ChunkPalette palette = new SectionBlockFormat.ChunkPalette();
        NBTTagCompound written = new NBTTagCompound();
        SectionBlockFormat.write(data, written, palette);
        BlockStateContainer back = new BlockStateContainer();
        NBTTagCompound chunkBack = new NBTTagCompound();
        chunkBack.setTag(SectionBlockFormat.CHUNK_PALETTE, palette.toNBT());
        SectionBlockFormat.read(written, back, SectionBlockFormat.readChunkPalette(chunkBack));
        assertThat(back.get(0, 0, 0)).isSameAs(Blocks.AIR.getDefaultState());
        assertThat(back.get(15, 15, 15)).isSameAs(stone);
    }

    /* A uniform section's chunk packet is bits=0 + one varint state id + an empty long array. */
    @Test
    public void uniformSectionRoundTripsThroughThePacket() {
        IBlockState stone = Blocks.STONE.getStateFromMeta(2);
        BlockStateContainer singleSet = new BlockStateContainer();
        singleSet.set(0, 0, 0, stone);
        assertThat(singleSet.getRawBits()).isEqualTo(4);
        assertThat(singleSet.get(0, 0, 0)).isSameAs(stone);
        assertThat(singleSet.get(9, 9, 9)).isSameAs(Blocks.AIR.getDefaultState());

        BlockStateContainer data = new BlockStateContainer();
        assertThat(data.loadLocalPalette(new IBlockState[] { stone }, 0)).isEqualTo(true);
        PacketBuffer buf = new PacketBuffer(io.netty.buffer.Unpooled.buffer());
        data.write(buf);
        byte[] expected = new byte[buf.readableBytes()];
        buf.getBytes(0, expected);
        // bits byte, the state id varint, the empty long array varint
        assertThat(expected[0] & 0xFF).isEqualTo(0);
        assertThat(expected[expected.length - 1] & 0xFF).isEqualTo(0);
        assertThat(expected.length).isEqualTo(1 +
                PacketBuffer.getVarIntSize(net.minecraft.block.Block.BLOCK_STATE_IDS.get(stone)) + 1
        );
        BlockStateContainer back = new BlockStateContainer();
        back.read(buf);
        assertThat(back.getRawBits()).isEqualTo(0);
        assertThat(back.get(9, 9, 9)).isSameAs(stone);
        assertThat(back.get(0, 0, 0)).isSameAs(stone);
    }

    @Test
    public void duplicateAirPaletteResetsToZeroBits() {
        BlockStateContainer data = new BlockStateContainer();
        data.set(1, 1, 1, Blocks.STONE.getDefaultState());
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0, 1 });
        section.setTag(SectionBlockFormat.STATES, new NBTTagLongArray(new long[SectionBlockFormat.longCount(4)]));
        SectionBlockFormat.read(section, data,
            new IBlockState[] { Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState() }
        );
        assertThat(data.getRawBits()).isEqualTo(0);
        assertThat(data.getRawStorage()).isNull();
        assertThat(data.get(1, 1, 1)).isSameAs(Blocks.AIR.getDefaultState());
    }

}
