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

import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class ChunkBiomesTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private static Chunk chunk() {
        return new Chunk(null, 3, -2);
    }

    private static NBTTagCompound referenceNBT(int[] ids) {
        var indexOf = new java.util.HashMap<Integer, Integer>();
        NBTTagList palette = new NBTTagList();
        byte[] indices = new byte[256];
        for (int i = 0; i < ids.length; i++) {
            Integer index = indexOf.get(ids[i]);
            if (index == null) {
                index = indexOf.size();
                indexOf.put(ids[i], index);
                Biome biome = ids[i] == ChunkBiomes.UNSET ? null : Biome.getBiome(ids[i]);
                palette.appendTag(new NBTTagString(biome == null ? "" : biome.getRegistryName().toString()));
            }
            indices[i] = index.byteValue();
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(ChunkBiomes.LAYERS, 1);
        tag.setTag(ChunkBiomes.PALETTE, palette);
        if (palette.tagCount() > 1) {
            tag.setByteArray(ChunkBiomes.INDICES, indices);
        }
        return tag;
    }

    /* A biome past 255 whose low byte another biome also has, registering the extra biomes if this test runs first. */
    private static int wideBiomeSharingALowByte() {
        for (int i = 0; i < 256; i++) {
            ResourceLocation name = new ResourceLocation("wideids_test", "palette_" + i);
            if (!ForgeRegistries.BIOMES.containsKey(name)) {
                Biome biome = new Biome(new Biome.BiomeProperties("palette_" + i)) { };
                biome.setRegistryName(name);
                ForgeRegistries.BIOMES.register(biome);
            }
        }
        for (Biome biome : ForgeRegistries.BIOMES) {
            int id = Biome.getIdForBiome(biome);
            if (id > 255 && net.minecraftforge.registries.GameData.getBiomeIdsWithLowByte(id & 0xFF).length > 1) {
                return id;
            }
        }
        throw new AssertionError("no wide biome shares a low byte with another");
    }

    private static World desertWorld() {
        ISaveHandler save = new ISaveHandler() {

            @Override
            public WorldInfo loadWorldInfo() {
                return null;
            }

            @Override
            public void checkSessionLock() { }

            @Override
            public IChunkLoader getChunkLoader(WorldProvider provider) {
                return null;
            }

            @Override
            public void saveWorldInfoWithPlayer(WorldInfo info, NBTTagCompound tag) { }

            @Override
            public void saveWorldInfo(WorldInfo info) { }

            @Override
            public IPlayerFileData getPlayerNBTManager() {
                return null;
            }

            @Override
            public void flush() { }

            @Override
            public File getWorldDirectory() {
                return null;
            }

            @Override
            public File getMapFileFromName(String name) {
                return null;
            }

            @Override
            public TemplateManager getStructureTemplateManager() {
                return null;
            }

        };
        WorldProvider provider = new WorldProvider() {

            @Override
            public DimensionType getDimensionType() {
                return DimensionType.OVERWORLD;
            }

            @Override
            public BiomeProvider getBiomeProvider() {
                return new BiomeProviderSingle(Biomes.DESERT);
            }

        };
        WorldInfo info = new WorldInfo(new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.DEFAULT), "test"
        );
        return new World(save, info, provider, new Profiler(), true) {

            @Override
            protected IChunkProvider createChunkProvider() {
                return null;
            }

            @Override
            protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
                return false;
            }

        };
    }

    @Test
    public void byteArraysRequireExactlyOneChunkOfColumns() {
        for (int size : new int[] { 0, 255, 257 }) {
            ChunkBiomes biomes = chunk().getBiomeIds();
            biomes.set(0, Biome.getIdForBiome(Biomes.DESERT));
            int[] before = biomes.copy();
            assertThatThrownBy(() -> biomes.setLegacy(new byte[size])).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> biomes.setAllFromBytes(new byte[size])).isInstanceOf(IllegalArgumentException.class
            );
            assertThat(biomes.copy()).isEqualTo(before);
        }
    }

    @Test
    public void oversizedPaletteIsRejectedWithoutChangingBiomes() {
        ChunkBiomes biomes = chunk().getBiomeIds();
        biomes.set(0, Biome.getIdForBiome(Biomes.DESERT));
        int[] before = biomes.copy();
        NBTTagList palette = new NBTTagList();
        for (int i = 0; i < 257; i++) {
            palette.appendTag(new NBTTagString(""));
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(ChunkBiomes.LAYERS, 1);
        tag.setTag(ChunkBiomes.PALETTE, palette);
        tag.setByteArray(ChunkBiomes.INDICES, new byte[256]);
        assertThatThrownBy(() -> biomes.readNBT(tag)).isInstanceOf(IllegalStateException.class);
        assertThat(biomes.copy()).isEqualTo(before);
    }

    @Test
    public void serializationMatchesThePreviousLayoutThroughAll256Indices() {
        int[] wide = new int[256];
        for (int i = 0; i < wide.length; i++) {
            Biome biome = new Biome(new Biome.BiomeProperties("palette_" + i)) { };
            biome.setRegistryName(new ResourceLocation("wideids_test", "palette_" + i));
            ForgeRegistries.BIOMES.register(biome);
            wide[i] = Biome.getIdForBiome(biome);
        }
        assertThat(java.util.Arrays.stream(wide).anyMatch(id -> id > 255)).isTrue();
        ChunkBiomes biomes = chunk().getBiomeIds();
        for (int distinct : new int[] { 1, 2, 16, 127, 128, 255, 256 }) {
            int[] ids = new int[256];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = wide[i % distinct];
            }
            biomes.setAll(ids);
            NBTTagCompound actual = new NBTTagCompound();
            biomes.writeNBT(actual);
            assertThat(actual).isEqualTo(referenceNBT(ids));
            ChunkBiomes back = chunk().getBiomeIds();
            assertThat(back.readNBT(actual)).isTrue();
            assertThat(back.copy()).isEqualTo(ids);
        }
        wide[0] = ChunkBiomes.UNSET;
        biomes.setAll(wide);
        NBTTagCompound mixed = new NBTTagCompound();
        biomes.writeNBT(mixed);
        assertThat(mixed).isEqualTo(referenceNBT(wide));
    }

    @Test
    public void corruptLegacyArrayIsRejectedWithoutReplacingBiomes() {
        ChunkBiomes biomes = chunk().getBiomeIds();
        biomes.set(0, Biome.getIdForBiome(Biomes.DESERT));
        int[] before = biomes.copy();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByteArray("Biomes", new byte[255]);
        assertThatThrownBy(() -> biomes.readNBT(tag)).isInstanceOf(IllegalStateException.class);
        assertThat(biomes.copy()).isEqualTo(before);
    }

    @Test
    public void malformedPaletteDoesNotFallThroughToLegacyData() {
        ChunkBiomes biomes = chunk().getBiomeIds();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(ChunkBiomes.PALETTE, 1);
        tag.setByteArray("Biomes", new byte[256]);
        assertThatThrownBy(() -> biomes.readNBT(tag)).isInstanceOf(IllegalStateException.class);
        NBTTagList wrongEntries = new NBTTagList();
        wrongEntries.appendTag(new NBTTagInt(0));
        tag.setTag(ChunkBiomes.PALETTE, wrongEntries);
        assertThatThrownBy(() -> biomes.readNBT(tag)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void uniformWriteRemovesOldBiomeIndices() {
        ChunkBiomes biomes = chunk().getBiomeIds();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByteArray(ChunkBiomes.INDICES, new byte[256]);
        biomes.writeNBT(tag);
        assertThat(tag.hasKey(ChunkBiomes.INDICES)).isFalse();
    }

    @Test
    public void startsUnsetAndKeepsTheByteViewInStep() {
        Chunk chunk = chunk();
        ChunkBiomes biomes = chunk.getBiomeIds();
        assertThat(biomes.get(0)).isEqualTo(ChunkBiomes.UNSET);
        int desert = Biome.getIdForBiome(Biomes.DESERT);
        biomes.set(5, 7, desert);
        assertThat(biomes.get(5, 7)).isEqualTo(desert);
        assertThat(chunk.getBiomeArray()[7 << 4 | 5]).isEqualTo((byte) desert);
        assertThat(chunk.getBiomeArray()[0]).isEqualTo((byte) 0xFF);
    }

    @Test
    public void picksUpBytesWrittenIntoTheOldArray() {
        Chunk chunk = chunk();
        int jungle = Biome.getIdForBiome(Biomes.JUNGLE);
        byte[] view = chunk.getBiomeArray();
        for (int i = 0; i < 256; i++) {
            ChunkBiomes.storeByte(view, i, jungle); // what transformed mod code calls (I2B dropped)
        }
        for (int i = 0; i < 256; i++) {
            assertThat(chunk.getBiomeIds().get(i)).isEqualTo(jungle);
        }
        ChunkBiomes.storeByte(view, 17, (byte) 0xFF);
        assertThat(chunk.getBiomeIds().get(17)).isEqualTo(ChunkBiomes.UNSET);
        chunk.setBiomeArray(new byte[256]);
        assertThat(chunk.getBiomeIds().get(17)).isEqualTo(Biome.getIdForBiome(Biomes.OCEAN));
    }

    @Test
    public void byteWritesResolveToTheBiomeAlreadyInTheChunk() {
        int wide = wideBiomeSharingALowByte();
        int[] sharing = net.minecraftforge.registries.GameData.getBiomeIdsWithLowByte(wide & 0xFF);
        assertThat(sharing.length > 1).isTrue();
        assertThat(sharing[0] != wide).isTrue();

        Chunk chunk = chunk();
        chunk.getBiomeIds().set(0, 0, wide); // the proper API put the wide biome here
        byte[] view = chunk.getBiomeArray();
        assertThat(view[0]).isEqualTo((byte) wide);

        // Read-modify-write, the common mod pattern, as transformed bytecode calls it
        for (int i = 1; i < 16; i++) {
            ChunkBiomes.storeByte(view, i, ChunkBiomes.loadByte(view, 0));
        }
        for (int i = 0; i < 16; i++) {
            assertThat(chunk.getBiomeIds().get(i)).isEqualTo(wide);
        }

        // Round trip: the save holds the wide biome's name, not the lowest candidate's
        NBTTagCompound tag = new NBTTagCompound();
        chunk.getBiomeIds().writeNBT(tag);
        ChunkBiomes back = chunk().getBiomeIds();
        assertThat(back.readNBT(tag)).isTrue();
        for (int i = 0; i < 16; i++) {
            assertThat(back.get(i)).isEqualTo(wide);
        }
    }

    @Test
    public void byteWriteOfAWideBiomeNewToAnEmptyChunkFallsBackToTheLowestCandidate() {
        int wide = wideBiomeSharingALowByte();
        int[] sharing = net.minecraftforge.registries.GameData.getBiomeIdsWithLowByte(wide & 0xFF);
        Chunk chunk = chunk(); // world is null: no provider to consult either
        ChunkBiomes.storeByte(chunk.getBiomeArray(), 0, (byte) wide); // the truncated byte path
        assertThat(chunk.getBiomeIds().get(0)).isEqualTo(sharing[0]);
    }

    @Test
    public void savesNamesAndReadsBothFormats() {
        Chunk chunk = chunk();
        int[] ids = new int[256];
        for (int i = 0; i < 256; i++) {
            ids[i] = i % 3 == 0
                ? ChunkBiomes.UNSET
                : Biome.getIdForBiome(i % 3 == 1 ? Biomes.PLAINS : Biomes.SWAMPLAND);
        }
        chunk.getBiomeIds().setAll(ids);
        NBTTagCompound tag = new NBTTagCompound();
        chunk.getBiomeIds().writeNBT(tag);
        assertThat(tag.getTagList(ChunkBiomes.PALETTE, 8).tagCount()).isEqualTo(3);
        assertThat(tag.getTagList(ChunkBiomes.PALETTE, 8).getStringTagAt(1).startsWith("minecraft:")).isTrue();
        Chunk back = chunk();
        assertThat(back.getBiomeIds().readNBT(tag)).isTrue();
        assertThat(back.getBiomeIds().copy()).isEqualTo(ids);

        NBTTagCompound single = new NBTTagCompound();
        Chunk uniform = chunk();
        int[] all = new int[256];
        java.util.Arrays.fill(all, Biome.getIdForBiome(Biomes.TAIGA));
        uniform.getBiomeIds().setAll(all);
        uniform.getBiomeIds().writeNBT(single);
        assertThat(single.hasKey(ChunkBiomes.INDICES)).isFalse();

        NBTTagCompound legacy = new NBTTagCompound();
        byte[] bytes = new byte[256];
        java.util.Arrays.fill(bytes, (byte) Biome.getIdForBiome(Biomes.BEACH));
        bytes[3] = (byte) 0xFF;
        legacy.setByteArray("Biomes", bytes);
        Chunk old = chunk();
        assertThat(old.getBiomeIds().readNBT(legacy)).isTrue();
        assertThat(old.getBiomeIds().get(0)).isEqualTo(Biome.getIdForBiome(Biomes.BEACH));
        assertThat(old.getBiomeIds().get(3)).isEqualTo(ChunkBiomes.UNSET);
        assertThat(chunk().getBiomeIds().readNBT(new NBTTagCompound())).isFalse();
    }

    /* A packet's full-chunk capture fills UNSET columns from the world so the wire never carries 0. */
    @Test
    public void unsetColumnsAreResolvedBeforeTheChunkIsSent() {
        Biome marsh = new Biome(new Biome.BiomeProperties("resolve_me")) { };
        marsh.setRegistryName(new ResourceLocation("wideids_test", "resolve_me"));
        ForgeRegistries.BIOMES.register(marsh);
        Chunk chunk = new Chunk(desertWorld(), 1, 1);
        ChunkBiomes biomes = chunk.getBiomeIds();
        biomes.set(1, Biome.getIdForBiome(marsh));
        new SPacketChunkData(chunk, 0xFFFF);
        for (int i = 0; i < 256; i++) {
            assertThat(biomes.get(i) != ChunkBiomes.UNSET).isTrue();
        }
        assertThat(biomes.get(0)).isEqualTo(Biome.getIdForBiome(Biomes.DESERT));
        assertThat(biomes.get(1)).isEqualTo(Biome.getIdForBiome(marsh));
    }

    /* No stand-in for an unmatched name: the column stays UNSET and the provider fills it as before. */
    @Test
    public void unmatchedBiomeStillFallsBackToTheProvider() {
        Chunk chunk = new Chunk(desertWorld(), 0, 0);
        ChunkBiomes biomes = chunk.getBiomeIds();
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList palette = new NBTTagList();
        palette.appendTag(new NBTTagString("gone_mod:xyzzy"));
        tag.setInteger(ChunkBiomes.LAYERS, 1);
        tag.setTag(ChunkBiomes.PALETTE, palette);
        assertThat(biomes.readNBT(tag)).isTrue();
        assertThat(biomes.get(0)).isEqualTo(ChunkBiomes.UNSET);
        biomes.resolveUnset(chunk);
        for (int i = 0; i < 256; i++) {
            assertThat(biomes.get(i)).isEqualTo(Biome.getIdForBiome(Biomes.DESERT));
        }
    }

    @Test
    public void verticalBiomeLayersExpandRoundTripNbtAndPackets() throws Exception {
        int plains = Biome.getIdForBiome(Biomes.PLAINS);
        int desert = Biome.getIdForBiome(Biomes.DESERT);
        int sky = Biome.getIdForBiome(Biomes.SKY);

        Chunk chunk = new Chunk(desertWorld(), 2, 2);
        ChunkBiomes biomes = chunk.getBiomeIds();
        assertThat(biomes.getLayers()).isEqualTo(1);
        biomes.set(3, 5, plains);
        assertThat(biomes.getLayers()).isEqualTo(1);
        assertThat(biomes.get(3, 0, 5)).isEqualTo(plains);
        assertThat(biomes.get(3, 64, 5)).isEqualTo(plains);

        biomes.set(3, 64, 5, sky);
        assertThat(biomes.getLayers()).isEqualTo(16);
        assertThat(biomes.get(3, 0, 5)).isEqualTo(plains);
        assertThat(biomes.get(3, 64, 5)).isEqualTo(sky);
        assertThat(biomes.get(3, 128, 5)).isEqualTo(plains);

        NBTTagCompound tag = new NBTTagCompound();
        biomes.writeNBT(tag);
        assertThat(tag.getInteger(ChunkBiomes.LAYERS)).isEqualTo(16);

        Chunk nbtBack = new Chunk(desertWorld(), 2, 2);
        assertThat(nbtBack.getBiomeIds().readNBT(tag)).isTrue();
        assertThat(nbtBack.getBiomeIds().getLayers()).isEqualTo(16);
        assertThat(nbtBack.getBiomeIds().get(3, 0, 5)).isEqualTo(plains);
        assertThat(nbtBack.getBiomeIds().get(3, 64, 5)).isEqualTo(sky);
        assertThat(nbtBack.getBiomeIds().get(3, 128, 5)).isEqualTo(plains);

        SPacketChunkData packet = new SPacketChunkData(chunk, 0xFFFF);
        net.minecraft.network.PacketBuffer buf = new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer()
        );
        packet.writePacketData(buf);
        SPacketChunkData readPacket = new SPacketChunkData();
        readPacket.readPacketData(buf);
        Chunk netBack = new Chunk(desertWorld(), 2, 2);
        netBack.read(readPacket.getReadBuffer(), readPacket.getExtractedSize(), readPacket.isFullChunk());
        assertThat(netBack.getBiomeIds().getLayers()).isEqualTo(16);
        assertThat(netBack.getBiomeIds().get(3, 0, 5)).isEqualTo(plains);
        assertThat(netBack.getBiomeIds().get(3, 64, 5)).isEqualTo(sky);
        assertThat(netBack.getBiomeIds().get(0, 64, 0)).isEqualTo(desert);
    }

}
