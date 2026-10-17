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

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;

import java.util.ArrayList;
import java.util.Arrays;
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
     * NEID section: either 1.12 NotEnoughIDs ({@code Blocks} + {@code Data} + optional {@code Add} + nibble {@code Add2}
     * for block ID bits 12..15) or GTNH {@code Blocks16} (two bytes per block, big-endian block id) + nibble {@code Data}.
     */
    public static boolean isNeidSection(NBTTagCompound section) {
        return section.hasKey(LegacyIdWorlds.BLOCKS16, 7) || section.hasKey("Add2", 7);
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
        IBlockState[] resolved = new IBlockState[palette.length];
        for (int p = 0; p < palette.length; p++) {
            resolved[p] = stateFromPacked(palette[p]);
        }
        for (int i = 0; i < SIZE; i++) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int index = (blocks[i] & 255) << 4 | dataNibbles.get(x, y, z);
            if (addNibbles != null) {
                index |= addNibbles.get(x, y, z) << 12;
            }
            if (index >= resolved.length) {
                throw corrupt(section, "refers to JEID palette entry " + index + " of " + resolved.length);
            }
            data.set(x, y, z, resolved[index]);
        }
    }

    public static void readNeid(NBTTagCompound section, BlockStateContainer data) {
        if (section.hasKey("Add2", 7)) {
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
                data.set(x, y, z, stateFromPacked(blockId << 4 | meta));
            }
            return;
        }
        byte[] blocks = section.getByteArray(LegacyIdWorlds.BLOCKS16);
        byte[] nibbles = section.getByteArray("Data");
        if (blocks.length < SIZE * 2 || nibbles.length < SIZE / 2) {
            throw corrupt(section, "is a NEID section with a short Blocks16 or Data array");
        }
        NibbleArray dataNibbles = new NibbleArray(nibbles);
        for (int i = 0; i < SIZE; i++) {
            int blockId = (blocks[i * 2] & 255) << 8 | blocks[i * 2 + 1] & 255;
            int meta = dataNibbles.get(i & 15, i >> 8 & 15, i >> 4 & 15);
            data.set(i & 15, i >> 8, i >> 4 & 15, stateFromPacked(blockId << 4 | meta));
        }
    }

    private static IBlockState stateFromPacked(int packed) {
        Block block = LegacyIds.block(LegacyIdWorlds.jeidBlockId(packed));
        if (block == null) {
            return Blocks.AIR.getDefaultState();
        }
        return block.getStateFromMeta(LegacyIdWorlds.jeidMeta(packed));
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

    public static ChunkPaletteRead readChunkPalette(NBTTagCompound chunk) {
        NBTTagList list = chunkPaletteTag(chunk);
        IBlockState[] states = new IBlockState[list.tagCount()];
        String[] unresolved = null;
        IBlockState[] standIns = null;
        for (int i = 0; i < states.length; i++) {
            String s = list.getStringTagAt(i);
            states[i] = parse(s);
            int open = s.indexOf('[');
            ResourceLocation blockKey = new ResourceLocation(open < 0 ? s : s.substring(0, open));
            if (!Block.REGISTRY.containsKey(blockKey)) {
                if (RetainedContent.isRemoved(s)) {
                    RetainedContent.noteRemoved(s); // a namespace the user chose to drop: air, no stand-in, no retained name
                    continue;
                }
                if (unresolved == null) {
                    unresolved = new String[states.length];
                    standIns = new IBlockState[states.length];
                }
                unresolved[i] = s; // a mod's block, missing now: reads as its stand-in, writes back verbatim
                standIns[i] = StandIns.forMissing(s);
                if (standIns[i] != null) {
                    states[i] = standIns[i];
                }
            } else if (open >= 0 && hasUnresolvedProperty(states[i].getBlock(), s, open)) {
                if (unresolved == null) {
                    unresolved = new String[states.length];
                    standIns = new IBlockState[states.length];
                }
                unresolved[i] = s; // registered block with a missing mod's property value (e.g. custom EnumDyeColor)
                standIns[i] = states[i];
            }
        }
        return new ChunkPaletteRead(states, unresolved, standIns);
    }

    public static void write(BlockStateContainer data, NBTTagCompound section, ChunkPalette chunkPalette) {
        write(data, section, chunkPalette, null);
    }

    public static void write(BlockStateContainer data, NBTTagCompound section, ChunkPalette chunkPalette,
        RetainedContent.@Nullable Section retained
    ) {
        IBlockState[] local = data.getLocalPalette();
        int[] values;
        Object[] entries;
        if (local != null) {
            // The section's own palette, in storage order: storage values are already palette indices
            entries = local;
            values = null;
        } else {
            // Global state ids (more than 256 states in the section): number the states the section uses
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
            entries = used.toArray();
        }
        if (retained != null && !retained.isEmpty()) {
            // A cell that still reads as its stand-in (air when none) keeps its saved name; any other state
            // wins (ExtendedBlockStorage.set drops the name, so this guards only writes that bypassed it)
            List<Object> grown = new ArrayList<>(Arrays.asList(entries));
            Map<String, Integer> retainedEntries = new java.util.HashMap<>();
            if (values == null) {
                BitArray storage = data.getRawStorage(); // null at zero bits: every cell is palette index 0
                values = new int[SIZE];
                for (int i = 0; i < SIZE; i++) {
                    values[i] = storage == null ? 0 : storage.getAt(i);
                }
            }
            for (Int2ObjectMap.Entry<RetainedContent.Retained> cell : retained.cells().int2ObjectEntrySet()) {
                int i = cell.getIntKey();
                if (data.get(i & 15, i >> 8, i >> 4 & 15) == retained.reads(i)) {
                    Integer index = retainedEntries.get(cell.getValue().name());
                    if (index == null) {
                        index = grown.size();
                        grown.add(cell.getValue().name());
                        retainedEntries.put(cell.getValue().name(), index);
                    }
                    values[i] = index;
                }
            }
            entries = grown.toArray();
        }
        int[] palette = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
            palette[i] = entries[i] instanceof String name
                ? chunkPalette.indexOf(name)
                : chunkPalette.indexOf((IBlockState) entries[i]);
        }
        section.setTag(PALETTE, new NBTTagIntArray(palette));
        if (entries.length == 1) {
            section.removeTag(STATES);
            return;
        }
        int bits = bitsFor(entries.length);
        long[] longs;
        if (values == null && bits == data.getRawBits()) {
            // Storage already holds these palette indices in the on-disk layout: copy it verbatim
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
        read(section, data, new ChunkPaletteRead(chunkPalette, null, null), null, null);
    }

    public static void read(NBTTagCompound section, BlockStateContainer data, ChunkPaletteRead chunkPalette) {
        read(section, data, chunkPalette, null, null);
    }

    public static void read(NBTTagCompound section, BlockStateContainer data, ChunkPaletteRead chunkPalette,
        RetainedContent.@Nullable Section retained, @Nullable ChunkPos pos
    ) {
        int[] palette = section.getIntArray(PALETTE);
        if (palette.length == 0) {
            throw corrupt(section, "has an empty block palette");
        }
        IBlockState[] entries = new IBlockState[palette.length];
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] < 0 || palette[i] >= chunkPalette.states().length) {
                throw corrupt(section,
                    "refers to chunk palette entry " + palette[i] + " of " + chunkPalette.states().length
                );
            }
            entries[i] = chunkPalette.states()[palette[i]];
        }
        if (entries.length == 1) {
            if (!data.loadLocalPalette(entries, 0)) {
                for (int i = 0; i < SIZE; i++) {
                    data.set(i & 15, i >> 8, i >> 4 & 15, entries[0]);
                }
            }
            if (retained != null) {
                String name = chunkPalette.unresolved(palette[0]);
                if (name != null) {
                    RetainedContent.noteMissingBlock(name, chunkPalette.standIn(palette[0]), pos);
                    for (int i = 0; i < SIZE; i++) {
                        retained.retain(i, name, chunkPalette.standIn(palette[0]));
                    }
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
        // false: the palette can't take the entries in saved order (no air first, or more than 256 of them)
        boolean local = data.loadLocalPalette(entries, bits);
        if (local) {
            // The saved layout is the storage layout at every bit width: one copy, no repack
            System.arraycopy(longs, 0, data.getRawStorage().getBackingLongArray(), 0, longs.length);
        } else {
            if (values == null) {
                values = unpack(longs, bits);
            }
            BitArray storage = data.getRawStorage();
            for (int i = 0; i < SIZE; i++) {
                if (local) {
                    storage.setAt(i, values[i]);
                } else {
                    data.set(i & 15, i >> 8, i >> 4 & 15, entries[values[i]]);
                }
            }
        }
        if (retained != null && chunkPalette.unresolved() != null) {
            BitArray storage = data.getRawStorage();
            for (int i = 0; i < SIZE; i++) {
                int index = local ? storage.getAt(i) : values[i];
                String name = chunkPalette.unresolved(palette[index]);
                if (name != null) {
                    retained.retain(i, name, chunkPalette.standIn(palette[index]));
                    RetainedContent.noteMissingBlock(name, chunkPalette.standIn(palette[index]), pos);
                }
            }
        }
    }

    /* Validates 4-bit indices in place, without unpacking them. */
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
        net.minecraft.block.state.BlockStateContainer definition = block.getBlockState();
        for (String pair : s.substring(open + 1, close).split(",")) {
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

    private static boolean hasUnresolvedProperty(Block block, String s, int open) {
        int close = s.endsWith("]") ? s.length() - 1 : s.length();
        if (open < 0 || open + 1 >= close) {
            return false;
        }
        net.minecraft.block.state.BlockStateContainer definition = block.getBlockState();
        for (String pair : s.substring(open + 1, close).split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                IProperty<?> property = definition.getProperty(pair.substring(0, eq));
                if (property == null || !property.parseValue(pair.substring(eq + 1)).isPresent()) {
                    return true;
                }
            }
        }
        return false;
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

    /*
     * Which section layout one section is in. Detected once per section and switched exhaustively by the
     * reader, so a format added later cannot be handled in one place and forgotten in another.
     */
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

        /* Our format: chunk-level name palette plus per-section packed indices. */
        record Native() implements SectionFormat { }

        /* Pre-palette Anvil: byte {@code Blocks}, nibble {@code Data}, optional nibble {@code Add}. */
        record ForgeLegacy() implements SectionFormat { }

        /* JEID/REID: int {@code Palette} of packed state ids plus 12-bit {@code Blocks}/{@code Data}. */
        record Jeid() implements SectionFormat { }

        /* NEID: big-endian {@code Blocks16} plus nibble {@code Data}. */
        record Neid() implements SectionFormat { }

    }

    /* The states one chunk's sections use, while writing it. Entries are states or raw retained strings. */
    public static final class ChunkPalette {

        private final Reference2IntOpenHashMap<IBlockState> indexOf = new Reference2IntOpenHashMap<>();
        private final Object2IntOpenHashMap<String> nameIndex = new Object2IntOpenHashMap<>();
        private final List<Object> entries = new ArrayList<>();

        public ChunkPalette() {
            indexOf.defaultReturnValue(-1);
            nameIndex.defaultReturnValue(-1);
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

        /* A retained name goes back in as it came out; the state it means does not exist to dedupe by. */
        int indexOf(String rawName) {
            int index = nameIndex.getInt(rawName);
            if (index < 0) {
                index = entries.size();
                nameIndex.put(rawName, index);
                entries.add(rawName);
            }
            return index;
        }

        public NBTTagList toNBT() {
            NBTTagList list = new NBTTagList();
            for (Object entry : entries) {
                list.appendTag(new NBTTagString(entry instanceof String name
                        ? name
                        : SectionBlockFormat.toString((IBlockState) entry)
                ));
            }
            return list;
        }

    }

    /*
     * The chunk palette as read: the resolved states (a vanilla stand-in for an unknown block name, air when
     * none applies) plus the original string and stand-in of every unresolved entry ({@code null} when all
     * resolved).
     */
    public record ChunkPaletteRead(IBlockState[] states,
        @Nullable String[] unresolved,
        @Nullable IBlockState[] standIns
    ) {

        @Nullable
        public String unresolved(int index) {
            return unresolved == null ? null : unresolved[index];
        }

        @Nullable
        public IBlockState standIn(int index) {
            return standIns == null ? null : standIns[index];
        }

    }

}
