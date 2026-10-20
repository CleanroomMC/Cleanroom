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

import com.cleanroommc.util.CleanroomLog;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.registries.GameData;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A chunk's biomes as full runtime ids, one per x/z column ({@code z << 4 | x}) or 16 vertical section
 * layers ({@code (y >> 4 & 15) << 8 | (z & 15) << 4 | (x & 15)}), {@link #UNSET} where not generated.
 * <p>
 * There is no {@code byte[256]} on the chunk anymore. {@code Chunk.getBiomeArray()} returns a throwaway
 * {@linkplain #snapshotBytes snapshot}, and code reaching it untransformed reads correct low bytes while its writes
 * are lost; the BiomeArrayTransformer rewrites array writes on it into {@link #storeByte}, which finds the owning
 * chunk in a weak map and applies {@link #storeFromMod}.
 * <p>
 * Saved as {@code BiomeLayers} (1 or 16), {@code BiomePalette} (biome names, {@code ""} for unset) and
 * {@code BiomeIndices} ({@code layers * 256} palette indices, absent for a single entry).
 */
public final class ChunkBiomes {

    public static final int UNSET = -1;
    public static final String PALETTE = "BiomePalette";
    public static final String INDICES = "BiomeIndices";
    public static final String LAYERS = "BiomeLayers";
    private static final int SIZE = 256;
    private static final int MAX_LAYERS = 16;

    /* Identity map (arrays never override equals) from a snapshot handed out by {@link #snapshotBytes} to its chunk. */
    private static final Map<byte[], WeakReference<Chunk>> SNAPSHOTS = Collections.synchronizedMap(new WeakHashMap<>());

    private static final Set<String> WARNED_WRITERS = ConcurrentHashMap.newKeySet();

    private final Chunk chunk;

    private int layers = 1;
    private int[] ids = new int[SIZE];

    public ChunkBiomes(Chunk chunk) {
        this.chunk = chunk;
        Arrays.fill(ids, UNSET);
    }

    /*
     * A new byte[256] holding each column's id low byte (0xFF for {@link #UNSET}), registered to this
     * chunk so {@link #storeByte} can find the owner of writes to it. Called by {@code Chunk.getBiomeArray()}.
     */
    public static byte[] snapshotBytes(Chunk chunk) {
        byte[] snapshot = new byte[SIZE];
        ChunkBiomes biomes = chunk.getBiomeIds();
        for (int i = 0; i < SIZE; i++) {
            snapshot[i] = (byte) biomes.ids[i];
        }
        SNAPSHOTS.put(snapshot, new WeakReference<>(chunk));
        return snapshot;
    }

    /*
     * A transformed mod's {@code array[index] = value} on a {@link #snapshotBytes} array. The owning
     * chunk gets the write via {@link #storeFromMod}; the array itself also takes the low byte so a
     * later read through the same snapshot sees the mod's own write. An array the map does not know
     * (a copy the mod made) falls back to a plain byte store.
     */
    public static void storeByte(byte[] array, int index, int value) {
        array[index] = (byte) value;
        WeakReference<Chunk> ref = SNAPSHOTS.get(array);
        Chunk owner = ref == null ? null : ref.get();
        if (owner != null) {
            owner.getBiomeIds().storeFromMod(index, value);
        }
    }

    /*
     * A transformed mod's {@code array[index]} on a {@link #snapshotBytes} array. Identical to BALOAD
     * (sign-extended byte); it exists so the transformer routes reads through one place for logging.
     */
    public static int loadByte(byte[] array, int index) {
        return array[index];
    }

    private static void warnAmbiguous(int lowByte, int[] candidates) {
        String writer = callingMod();
        if (!WARNED_WRITERS.add(writer)) {
            return;
        }
        CleanroomLog.get().warn("{} wrote biome byte {} into Chunk.getBiomeArray(); {} biomes share that low byte and none is in this chunk or placed here by the biome provider. Using {} ({}). " +
                "A byte cannot hold a biome id past 255: this mod needs Chunk.getBiomeIds().set(x, z, id). Logged once per mod.",
            writer, lowByte, candidates.length, candidates[0],
            Biome.getBiome(candidates[0]) == null ? "?" : Biome.getBiome(candidates[0]).getRegistryName()
        );
    }

    /* The first stack frame outside Minecraft, Forge and Cleanroom: the mod that wrote the byte. */
    private static String callingMod() {
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String cls = frame.getClassName();
            if (cls.startsWith("java.") ||
                cls.startsWith("net.minecraft.") ||
                cls.startsWith("net.minecraftforge.") ||
                cls.startsWith("com.cleanroommc.")) {
                continue;
            }
            return cls;
        }
        return "an unknown caller";
    }

    public int getLayers() {
        return layers;
    }

    public int get(int index) {
        return ids[index];
    }

    public int get(int x, int y, int z) {
        int col = (z & 15) << 4 | x & 15;
        if (layers == 1) {
            return ids[col];
        }
        int layer = Math.clamp(y >> 4, 0, layers - 1);
        return ids[layer << 8 | col];
    }

    public int get(int x, int z) {
        return get((z & 15) << 4 | x & 15);
    }

    public void set(int index, int id) {
        ids[index] = id;
    }

    public void set(int x, int y, int z, int id) {
        int col = (z & 15) << 4 | x & 15;
        int layer = Math.clamp(y >> 4, 0, MAX_LAYERS - 1);
        if (layers == 1) {
            if (layer == 0 || ids[col] == id) {
                set(col, id);
                return;
            }
            expandTo16Layers();
        }
        set(layer << 8 | col, id);
    }

    public void set(int x, int z, int id) {
        int col = (z & 15) << 4 | x & 15;
        if (layers == 1) {
            set(col, id);
            return;
        }
        for (int layer = 0; layer < layers; layer++) {
            set(layer << 8 | col, id);
        }
    }

    private void expandTo16Layers() {
        int[] expanded = new int[MAX_LAYERS * SIZE];
        for (int layer = 0; layer < MAX_LAYERS; layer++) {
            System.arraycopy(ids, 0, expanded, layer << 8, SIZE);
        }
        ids = expanded;
        layers = MAX_LAYERS;
    }

    public int[] copy() {
        return ids.clone();
    }

    /*
     * Fills every UNSET column by asking the chunk's world generator (its biome provider) what belongs
     * there. Called by {@code SPacketChunkData} before it copies this array: vanilla's wire format never
     * carried an unset column, so neither may the varint encoding. Does nothing on unit-test chunks
     * (world null).
     */
    public void resolveUnset(Chunk chunk) {
        World world = chunk.getWorld();
        if (world == null) {
            return;
        }
        BiomeProvider provider = world.getBiomeProvider();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == UNSET) {
                int x = i & 15;
                int z = (i >> 4) & 15;
                int y = (i >> 8) << 4;
                ids[i] = Biome.getIdForBiome(provider.getBiome(new BlockPos((chunk.x << 4) | x, y, (chunk.z << 4) | z),
                    Biomes.PLAINS
                ));
            }
        }
    }

    public void setAll(int[] newIds) {
        if (newIds.length != SIZE && newIds.length != MAX_LAYERS * SIZE) {
            throw new IllegalArgumentException("Chunk biomes need " + SIZE + " or " + (MAX_LAYERS * SIZE) +
                    " ids, got " + newIds.length + "."
            );
        }
        layers = newIds.length / SIZE;
        if (ids.length != newIds.length) {
            ids = new int[newIds.length];
        }
        System.arraycopy(newIds, 0, ids, 0, newIds.length);
    }

    /* Old-format bytes, which are full ids (worlds from before the format held at most 256 biomes). */
    public void setLegacy(byte[] bytes) {
        if (bytes.length != SIZE) {
            throw new IllegalArgumentException("Legacy chunk biomes need " + SIZE + " bytes, got " + bytes.length + "."
            );
        }
        int[] newIds = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            if (bytes[i] == (byte) 0xFF) {
                newIds[i] = UNSET;
            } else {
                int savedId = bytes[i] & 0xFF;
                Biome biome = LegacyIds.biome(savedId);
                newIds[i] = biome == null ? UNSET : Biome.getIdForBiome(biome);
            }
        }
        setAll(newIds);
    }

    /* {@code Chunk.setBiomeArray(byte[])}: resolve each byte column by column (0xFF = unset); lossy above 255. */
    public void setAllFromBytes(byte[] bytes) {
        if (bytes.length != SIZE) {
            throw new IllegalArgumentException("Chunk biomes need " + SIZE + " bytes, got " + bytes.length + ".");
        }
        if (layers != 1) {
            layers = 1;
            ids = new int[SIZE];
        }
        for (int i = 0; i < SIZE; i++) {
            ids[i] = bytes[i] == (byte) 0xFF ? UNSET : resolve(bytes[i] & 0xFF, i);
        }
    }

    /* JEID/REID save one full biome id per column; the ids are saved ids, resolved like {@link #setLegacy}. */
    public void setIds(int[] newIds) {
        if (newIds.length != SIZE) {
            throw new IllegalArgumentException("Chunk biomes need " + SIZE + " ids, got " + newIds.length + ".");
        }
        int[] resolved = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            int savedId = newIds[i];
            Biome biome = LegacyIds.biome(savedId);
            resolved[i] = biome == null ? UNSET : Biome.getIdForBiome(biome);
        }
        setAll(resolved);
    }

    /*
     * A mod's byte-array write, resolved to a full id. When the transformer removed the {@code I2B},
     * {@code value} is the full id (> 255); otherwise it is a byte and {@link #resolve} picks which
     * biome that low byte means. Bytes read back through {@link #loadByte} arrive sign-extended, so
     * byte range is -128..255 here. A full id in 0..255 and a byte with that value are the same thing;
     * resolve()'s first rule (unique low byte) handles it.
     */
    public void storeFromMod(int index, int value) {
        int col = index & (SIZE - 1);
        int resolvedId;
        if (value > 255 || value < -128) {
            resolvedId = Biome.getBiome(value) != null ? value : UNSET;
        } else {
            int low = value & 0xFF;
            resolvedId = low == 0xFF ? UNSET : resolve(low, col);
        }
        set(col & 15, col >> 4, resolvedId);
    }

    private int resolve(int lowByte, int index) {
        int[] candidates = GameData.getBiomeIdsWithLowByte(lowByte);
        if (candidates.length <= 1) {
            return candidates.length == 0 ? UNSET : candidates[0];
        }
        int present = candidatePresentInChunk(candidates);
        if (present != UNSET) {
            return present;
        }
        World world = chunk.getWorld();
        if (world != null && !world.isRemote) {
            BlockPos pos = new BlockPos(chunk.x << 4 | index & 15, 64, chunk.z << 4 | index >> 4);
            int generated = Biome.getIdForBiome(world.getBiomeProvider().getBiome(pos, null));
            for (int candidate : candidates) {
                if (candidate == generated) {
                    return candidate;
                }
            }
        }
        warnAmbiguous(lowByte, candidates);
        return candidates[0];
    }

    /* The candidate already used by another column of this chunk, if exactly one of them is. */
    private int candidatePresentInChunk(int[] candidates) {
        int found = UNSET;
        for (int id : ids) {
            if (id == UNSET) {
                continue;
            }
            for (int candidate : candidates) {
                if (id == candidate) {
                    if (found != UNSET && found != id) {
                        return UNSET; // two candidates both present: the byte is ambiguous even within the chunk
                    }
                    found = id;
                }
            }
        }
        return found;
    }

    public void writeNBT(NBTTagCompound chunkTag) {
        Object2IntOpenHashMap<String> indexOf = new Object2IntOpenHashMap<>();
        indexOf.defaultReturnValue(-1);
        NBTTagList palette = new NBTTagList();
        int total = ids.length;
        int[] rawIndices = null;
        for (int i = 0; i < total; i++) {
            Biome biome = ids[i] == UNSET ? null : Biome.getBiome(ids[i]);
            ResourceLocation biomeName = biome == null ? null : biome.getRegistryName();
            String name = biomeName == null ? "" : biomeName.toString();
            int index = indexOf.getInt(name);
            if (index < 0) {
                index = indexOf.size();
                indexOf.put(name, index);
                palette.appendTag(new NBTTagString(name));
            }
            if (rawIndices == null && index != 0) {
                rawIndices = new int[total];
            }
            if (rawIndices != null) {
                rawIndices[i] = index;
            }
        }
        chunkTag.setInteger(LAYERS, layers);
        chunkTag.setTag(PALETTE, palette);
        if (rawIndices == null) {
            chunkTag.removeTag(INDICES);
        } else if (palette.tagCount() <= 256) {
            byte[] byteIndices = new byte[total];
            for (int i = 0; i < total; i++) {
                byteIndices[i] = (byte) rawIndices[i];
            }
            chunkTag.setByteArray(INDICES, byteIndices);
        } else {
            chunkTag.setIntArray(INDICES, rawIndices);
        }
    }

    /* Reads either format; returns false when the chunk has no biome data. */
    public boolean readNBT(NBTTagCompound chunkTag) {
        if (chunkTag.hasKey(PALETTE)) {
            int readLayers = chunkTag.hasKey(LAYERS) ? chunkTag.getInteger(LAYERS) : 1;
            if (chunkTag.hasKey(LAYERS) &&
                (!chunkTag.hasKey(LAYERS, 99) || (readLayers != 1 && readLayers != MAX_LAYERS))) {
                throw corrupt("has an invalid " + LAYERS + "; expected 1 or " + MAX_LAYERS);
            }
            int total = readLayers * SIZE;
            if (!(chunkTag.getTag(PALETTE) instanceof NBTTagList palette) ||
                palette.tagCount() > 0 && palette.getTagType() != 8) {
                throw corrupt("has an invalid " + PALETTE + "; expected a list of biome names");
            }
            if (palette.tagCount() > total) {
                throw corrupt("has " + palette.tagCount() + " biome palette entries for " + total + " cells");
            }
            int[] byIndex = new int[palette.tagCount()];
            for (int i = 0; i < byIndex.length; i++) {
                String name = palette.getStringTagAt(i);
                Biome biome = name.isEmpty() ? null : Biome.REGISTRY.getObject(new ResourceLocation(name));
                byIndex[i] = biome == null ? UNSET : Biome.getIdForBiome(biome);
            }
            if (byIndex.length == 0) {
                throw corrupt("has an empty biome palette");
            }
            int[] newIds = new int[total];
            if (byIndex.length == 1) {
                Arrays.fill(newIds, byIndex[0]);
            } else if (chunkTag.hasKey(INDICES, 11)) {
                int[] indices = chunkTag.getIntArray(INDICES);
                if (indices.length != total) {
                    throw corrupt("has " + byIndex.length + " biome palette entries but no " + total + "-int " +
                            INDICES + " array"
                    );
                }
                for (int i = 0; i < total; i++) {
                    int index = indices[i];
                    if (index < 0 || index >= byIndex.length) {
                        throw corrupt("refers to biome palette entry " + index + " of " + byIndex.length);
                    }
                    newIds[i] = byIndex[index];
                }
            } else {
                byte[] indices = chunkTag.getByteArray(INDICES);
                if (indices.length != total) {
                    throw corrupt("has " + byIndex.length + " biome palette entries but no " + total + "-byte " +
                            INDICES + " array"
                    );
                }
                for (int i = 0; i < total; i++) {
                    int index = indices[i] & 0xFF;
                    if (index >= byIndex.length) {
                        throw corrupt("refers to biome palette entry " + index + " of " + byIndex.length);
                    }
                    newIds[i] = byIndex[index];
                }
            }
            setAll(newIds);
            return true;
        }
        if (chunkTag.hasKey("Biomes")) {
            if (!chunkTag.hasKey("Biomes", 7)) {
                throw corrupt("has legacy Biomes data that is not a byte array");
            }
            byte[] legacy = chunkTag.getByteArray("Biomes");
            if (legacy.length != SIZE) {
                throw corrupt("has " + legacy.length + " legacy biome bytes instead of " + SIZE);
            }
            setLegacy(legacy);
            return true;
        }
        return false;
    }

    private IllegalStateException corrupt(String problem) {
        return new IllegalStateException("Chunk " + chunk.x + ", " + chunk.z + " " + problem + ".");
    }

}
