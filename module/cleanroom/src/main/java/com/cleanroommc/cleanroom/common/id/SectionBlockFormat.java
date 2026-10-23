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

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.BitArray;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Block storage in saved chunks, by name, so saves don't depend on runtime ids (no limit on blocks or states):
 * <ul>
 * <li>Chunk {@code BlockPalette}: list of strings, every state the chunk's sections use, written as
 * {@code namespace:name} or {@code namespace:name[property=value,...]}. One list per chunk: sections share most of
 * their states, so names are stored once rather than once per section.</li>
 * <li>Section {@code Palette}: int array of {@code BlockPalette} indices, the section's own palette.</li>
 * <li>Section {@code BlockStates}: long array of 4096 section-palette indices, absent when the section palette has
 * one entry. Each index uses {@code max(4, ceil(log2(palette size)))} bits; indices don't span longs (as in Minecraft
 * 1.16+), so a long holds {@code 64 / bits} of them, lowest bits first, in the section's {@code y << 8 | z << 4 | x}
 * order. At 4 bits this is also the in-memory layout, so most sections load and save as an array copy.</li>
 * </ul>
 * Unknown block names read as air, unknown properties or values as the block's default for them; Forge aliases and
 * missing-mapping remaps apply through the block registry.
 */
@SuppressWarnings("deprecation") // Cleanroom: metadata uses in this file are the legacy view
public final class SectionBlockFormat {

    public static final String CHUNK_PALETTE = "BlockPalette";
    public static final String PALETTE = "Palette";
    public static final String STATES = "BlockStates";
    private static final int SIZE = 4096;

    private SectionBlockFormat() { }

    /*
     * JEID/REID section: {@code Blocks}/{@code Data} are 12-bit indices into an int {@code Palette} of packed state ids
     * ({@code blockId << 4 | meta}). Must be detected before the vanilla reader, which would treat those bytes as block ids.
     */
    public static boolean isJeidSection(NBTTagCompound section) {
        return section.hasKey(PALETTE, 11) && section.hasKey("Blocks", 7) && section.hasKey("Data", 7);
    }

    /*
     * NEID section: 1.12.2 NotEnoughIDs ({@code Blocks} + {@code Data} + optional {@code Add} + nibble {@code Add2}
     * for block ID bits 12..15).
     */
    public static boolean isNeidSection(NBTTagCompound section) {
        return section.hasKey("Add2", 7);
    }

    public static void readJeid(NBTTagCompound section, BlockStateContainer data) {
        int[] palette = section.getIntArray(PALETTE);
        byte[] blocks = section.getByteArray("Blocks");
        byte[] nibbles = section.getByteArray("Data");
        if (palette.length == 0 || blocks.length < SIZE || nibbles.length < SIZE / 2) {
            throw corrupt(section, "is a JEID section with a short palette, Blocks or Data array");
        }
        NibbleArray dataNibbles = new NibbleArray(nibbles);
        NibbleArray addNibbles = section.hasKey("Add", 7) ? new NibbleArray(section.getByteArray("Add")) : null;
        for (int i = 0; i < SIZE; i++) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int index = (blocks[i] & 255) << 4 | dataNibbles.get(x, y, z);
            if (addNibbles != null) {
                index |= addNibbles.get(x, y, z) << 12;
            }
            if (index >= palette.length) {
                throw corrupt(section, "refers to JEID palette entry " + index + " of " + palette.length);
            }
            setFromPacked(i, palette[index], data);
        }
    }

    public static void readNeid(NBTTagCompound section, BlockStateContainer data) {
        byte[] blocks = section.getByteArray("Blocks");
        byte[] nibbles = section.getByteArray("Data");
        byte[] add2Bytes = section.getByteArray("Add2");
        if (blocks.length < SIZE || nibbles.length < SIZE / 2 || add2Bytes.length < SIZE / 2) {
            throw corrupt(section, "is a 1.12 NEID section with a short Blocks, Data or Add2 array");
        }
        NibbleArray dataNibbles = new NibbleArray(nibbles);
        NibbleArray addNibbles = section.hasKey("Add", 7) ? new NibbleArray(section.getByteArray("Add")) : null;
        NibbleArray add2Nibbles = new NibbleArray(add2Bytes);
        for (int i = 0; i < SIZE; i++) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int add = addNibbles == null ? 0 : addNibbles.get(x, y, z);
            int add2 = add2Nibbles.get(x, y, z);
            int blockId = (add2 << 12) | (add << 8) | (blocks[i] & 255);
            int meta = dataNibbles.get(x, y, z);
            setFromPacked(i, blockId << 4 | meta, data);
        }
    }

    public static void readLegacy(byte[] blockIds, NibbleArray dataNibbles, @Nullable NibbleArray blockIdExtension,
        BlockStateContainer data
    ) {
        for (int i = 0; i < SIZE; i++) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int ext = blockIdExtension == null ? 0 : blockIdExtension.get(x, y, z);
            int packed = ext << 12 | (blockIds[i] & 255) << 4 | dataNibbles.get(x, y, z);
            setFromPacked(i, packed, data);
        }
    }

    private static void setFromPacked(int cellIndex, int packed, BlockStateContainer data) {
        int x = cellIndex & 15;
        int y = cellIndex >> 8 & 15;
        int z = cellIndex >> 4 & 15;
        int blockId = packed >>> 4;
        int meta = packed & 15;
        Block block = LegacyIds.block(blockId);
        if (block != null) {
            data.set(x, y, z, block.getStateFromMeta(meta));
            return;
        }
        data.set(x, y, z, Blocks.AIR.getDefaultState());
    }

    public static boolean isPaletted(NBTTagCompound chunk) {
        if (!chunk.hasKey(CHUNK_PALETTE)) {
            return false;
        }
        chunkPaletteTag(chunk);
        return true;
    }

    private static NBTTagList chunkPaletteTag(NBTTagCompound chunk) {
        NBTBase tag = chunk.getTag(CHUNK_PALETTE);
        if (!(tag instanceof NBTTagList list) || list.tagCount() > 0 && list.getTagType() != 8) {
            throw new IllegalStateException("Chunk " + chunk.getInteger("xPos") + ", " + chunk.getInteger("zPos") +
                    " has an invalid " + CHUNK_PALETTE + "; expected a list of block-state strings."
            );
        }
        return list;
    }

    public static IBlockState[] readChunkPalette(NBTTagCompound chunk) {
        NBTTagList list = chunkPaletteTag(chunk);
        IBlockState[] states = new IBlockState[list.tagCount()];
        for (int i = 0; i < states.length; i++) {
            states[i] = parse(list.getStringTagAt(i));
        }
        return states;
    }

    public static void write(BlockStateContainer data, NBTTagCompound section, ChunkPalette chunkPalette) {
        IBlockState[] local = data.getLocalPalette();
        int[] values;
        IBlockState[] entries;
        if (local != null) {
            entries = local;
            values = null;
        } else {
            BitArray storage = data.getRawStorage();
            Int2IntOpenHashMap localOf = new Int2IntOpenHashMap();
            localOf.defaultReturnValue(-1);
            List<IBlockState> used = new ArrayList<>();
            values = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                int raw = storage.getAt(i);
                int index = localOf.get(raw);
                if (index < 0) {
                    index = used.size();
                    localOf.put(raw, index);
                    used.add(data.get(i & 15, i >> 8, i >> 4 & 15));
                }
                values[i] = index;
            }
            entries = used.toArray(new IBlockState[0]);
        }
        int[] palette = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
            palette[i] = chunkPalette.indexOf(entries[i]);
        }
        section.setTag(PALETTE, new NBTTagIntArray(palette));
        if (entries.length == 1) {
            section.removeTag(STATES);
            return;
        }
        int bits = bitsFor(entries.length);
        long[] longs;
        if (values == null && bits == data.getRawBits()) {
            longs = data.getRawStorage().getBackingLongArray().clone();
        } else {
            if (values == null) {
                BitArray storage = data.getRawStorage();
                values = new int[SIZE];
                for (int i = 0; i < SIZE; i++) {
                    values[i] = storage.getAt(i);
                }
            }
            longs = pack(values, bits);
        }
        section.setTag(STATES, new NBTTagLongArray(longs));
    }

    public static void read(NBTTagCompound section, BlockStateContainer data, IBlockState[] chunkPalette) {
        int[] palette = section.getIntArray(PALETTE);
        if (palette.length == 0) {
            throw corrupt(section, "has an empty block palette");
        }
        IBlockState[] entries = new IBlockState[palette.length];
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] < 0 || palette[i] >= chunkPalette.length) {
                throw corrupt(section,
                    "refers to chunk palette entry " + palette[i] + " of " + chunkPalette.length
                );
            }
            entries[i] = chunkPalette[palette[i]];
        }
        if (entries.length == 1) {
            if (!data.loadLocalPalette(entries, 0)) {
                for (int i = 0; i < SIZE; i++) {
                    data.set(i & 15, i >> 8, i >> 4 & 15, entries[0]);
                }
            }
            return;
        }
        int bits = bitsFor(entries.length);
        NBTBase tag = section.getTag(STATES);
        if (!(tag instanceof NBTTagLongArray longArray) || longArray.getLongArray().length != longCount(bits)) {
            throw corrupt(section,
                "has " + entries.length + " palette entries but no " + longCount(bits) + "-long " + STATES + " array"
            );
        }
        long[] longs = longArray.getLongArray();
        int[] values = null;
        if (bits == 4) {
            checkNibbles(longs, entries.length, section);
        } else {
            values = unpack(longs, bits);
            for (int value : values) {
                if (value >= entries.length) {
                    throw corrupt(section, "refers to palette entry " + value + " of " + entries.length);
                }
            }
        }
        boolean local = data.loadLocalPalette(entries, bits);
        if (local) {
            System.arraycopy(longs, 0, data.getRawStorage().getBackingLongArray(), 0, longs.length);
        } else {
            if (values == null) {
                values = unpack(longs, bits);
            }
            for (int i = 0; i < SIZE; i++) {
                data.set(i & 15, i >> 8, i >> 4 & 15, entries[values[i]]);
            }
        }
    }

    private static void checkNibbles(long[] longs, int paletteSize, NBTTagCompound section) {
        if (paletteSize >= 16) {
            return;
        }
        for (long word : longs) {
            for (int shift = 0; shift < 64; shift += 4) {
                int value = (int) (word >>> shift & 15);
                if (value >= paletteSize) {
                    throw corrupt(section, "refers to palette entry " + value + " of " + paletteSize);
                }
            }
        }
    }

    private static IllegalStateException corrupt(NBTTagCompound section, String problem) {
        return new IllegalStateException("Chunk section Y=" + section.getByte("Y") + " " + problem + ".");
    }

    static String toString(IBlockState state) {
        ResourceLocation name = Block.REGISTRY.getNameForObject(state.getBlock());
        if (state.getProperties().isEmpty()) {
            return String.valueOf(name);
        }
        StringBuilder out = new StringBuilder(String.valueOf(name)).append('[');
        boolean first = true;
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            out.append(entry.getKey().getName()).append('=').append(valueName(entry.getKey(), entry.getValue()));
        }
        return out.append(']').toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String valueName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    static IBlockState parse(String s) {
        int open = s.indexOf('[');
        Block block = Block.REGISTRY.getObject(new ResourceLocation(open < 0 ? s : s.substring(0, open)));
        IBlockState state = block.getDefaultState();
        int close = s.endsWith("]") ? s.length() - 1 : s.length();
        if (open < 0 || open + 1 >= close) {
            return state;
        }
        String inner = s.substring(open + 1, close);
        if (inner.indexOf('=') < 0) {
            try {
                return block.getStateFromMeta(Integer.parseInt(inner));
            } catch (RuntimeException ignored) {
                return state;
            }
        }
        net.minecraft.block.state.BlockStateContainer definition = block.getBlockState();
        for (String pair : inner.split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                IProperty<?> property = definition.getProperty(pair.substring(0, eq));
                if (property != null) {
                    state = withValue(state, property, pair.substring(eq + 1));
                }
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> IBlockState withValue(IBlockState state, IProperty<T> property,
        String value
    ) {
        return property.parseValue(value).toJavaUtil().map(v -> state.withProperty(property, v)).orElse(state);
    }

    static int bitsFor(int paletteSize) {
        return Math.max(4, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
    }

    static int longCount(int bits) {
        int perLong = 64 / bits;
        return (SIZE + perLong - 1) / perLong;
    }

    static long[] pack(int[] indices, int bits) {
        int perLong = 64 / bits;
        long[] longs = new long[longCount(bits)];
        for (int i = 0; i < indices.length; i++) {
            longs[i / perLong] |= (long) indices[i] << (i % perLong * bits);
        }
        return longs;
    }

    static int[] unpack(long[] longs, int bits) {
        int perLong = 64 / bits;
        long mask = (1L << bits) - 1;
        int[] indices = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            indices[i] = (int) (longs[i / perLong] >>> (i % perLong * bits) & mask);
        }
        return indices;
    }

    public sealed interface SectionFormat {

        static SectionFormat detect(NBTTagCompound chunk, NBTTagCompound section) {
            if (isPaletted(chunk)) {
                return new Native();
            }
            if (isJeidSection(section)) {
                return new Jeid();
            }
            if (isNeidSection(section)) {
                return new Neid();
            }
            return new ForgeLegacy();
        }

        record Native() implements SectionFormat { }

        record ForgeLegacy() implements SectionFormat { }

        record Jeid() implements SectionFormat { }

        record Neid() implements SectionFormat { }

    }

    public static final class ChunkPalette {

        private final Reference2IntOpenHashMap<IBlockState> indexOf = new Reference2IntOpenHashMap<>();
        private final List<IBlockState> entries = new ArrayList<>();

        public ChunkPalette() {
            indexOf.defaultReturnValue(-1);
        }

        int indexOf(IBlockState state) {
            int index = indexOf.getInt(state);
            if (index < 0) {
                index = entries.size();
                indexOf.put(state, index);
                entries.add(state);
            }
            return index;
        }

        public NBTTagList toNBT() {
            NBTTagList list = new NBTTagList();
            for (IBlockState entry : entries) {
                list.appendTag(new NBTTagString(SectionBlockFormat.toString(entry)));
            }
            return list;
        }

    }

}
