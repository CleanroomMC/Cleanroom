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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.fml.relauncher.Side;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/** The stand-in-terrain index, its persistence in {@link RetentionFile}, and the Regenerate/Keep choice. */
@ForgeTestRunner.Isolated
public class StandInTerrainTest {

    @BeforeAll
    public static void setup() throws Exception {
        MinecraftTestHarness.bootstrap();
        // StartupQuery.choice -> FMLCommonHandler.getSide() needs a sided delegate; a proxy answering
        // SERVER takes the dedicated-server branch (no GUI; cleanroom.missingContent picks the option).
        Object handler = java.lang.reflect.Proxy.newProxyInstance(StandInTerrainTest.class.getClassLoader(),
            new Class<?>[] { net.minecraftforge.fml.common.IFMLSidedHandler.class },
            (proxy, method, args) -> "getSide".equals(method.getName())
                ? Side.SERVER
                : method.getReturnType() == boolean.class ? false : null
        );
        java.lang.reflect.Field delegate = net.minecraftforge.fml.common.FMLCommonHandler.class.getDeclaredField("sidedDelegate"
        );
        delegate.setAccessible(true);
        delegate.set(net.minecraftforge.fml.common.FMLCommonHandler.instance(), handler);
    }

    private static RetentionFile fileWithIndex(String type, int dim, long... positions) {
        RetentionFile file = new RetentionFile();
        Map<Integer, it.unimi.dsi.fastutil.longs.LongSet> dims = new TreeMap<>();
        dims.put(dim, new LongOpenHashSet(positions));
        file.standInTerrain.put(type, dims);
        return file;
    }

    private static WorldInfo infoWithType(WorldType type) {
        return new WorldInfo(new WorldSettings(0L, GameType.CREATIVE, false, false, type), "test");
    }

    private static void writeChunks(File regionDir, int[][] positions) throws Exception {
        RegionFile region = new RegionFile(new File(regionDir, "r.0.0.mca"));
        try {
            for (int[] pos : positions) {
                try (DataOutputStream out = region.getChunkDataOutputStream(pos[0], pos[1])) {
                    out.writeInt(0xDEAD);
                    out.writeInt(pos[0] * 100 + pos[1]);
                }
            }
        } finally {
            region.close();
        }
    }

    @AfterEach
    public void tearDown() {
        RetainedContent.clearWorldState();
        System.clearProperty("cleanroom.standInTerrain");
        System.clearProperty("fml.doNotBackup");
    }

    @Test
    public void indexTracksMarkedAndUnmarkedWrites() {
        ChunkPos pos = new ChunkPos(3, 4);
        RetainedContent.indexStandInChunk("biomesop", 0, pos);
        RetainedContent.indexStandInChunk("biomesop", -1, new ChunkPos(9, 9));
        assertThat(RetainedContent.standInTerrain().get("biomesop").get(0).contains(pos.asLong(pos.x, pos.z))).isTrue();

        // the same position written without a mark drops out of every type and dimension
        RetainedContent.indexStandInChunk(null, 0, pos);
        assertThat(RetainedContent.standInTerrain().get("biomesop").get(0).contains(pos.asLong(pos.x, pos.z))
        ).isFalse();
        assertThat(RetainedContent.standInTerrain().get("biomesop").get(-1).contains(new ChunkPos(9, 9).asLong(9, 9))
        ).isTrue();
    }

    @Test
    public void fileRoundTripsTheIndex(@TempDir Path dir) {
        RetentionFile out = new RetentionFile();
        Map<Integer, it.unimi.dsi.fastutil.longs.LongSet> dims = new TreeMap<>();
        dims.put(0, new LongOpenHashSet(new long[] { ChunkPos.asLong(1, 2), ChunkPos.asLong(5, 6) }));
        dims.put(-1, new LongOpenHashSet(new long[] { ChunkPos.asLong(7, 8) }));
        out.standInTerrain.put("biomesop", dims);
        out.write(dir.toFile());

        RetentionFile in = RetentionFile.read(dir.toFile());
        assertThat(in).isNotNull();
        assertThat(in.standInTerrain.get("biomesop").get(0).size()).isEqualTo(2);
        assertThat(in.standInTerrain.get("biomesop").get(0).contains(ChunkPos.asLong(1, 2))).isTrue();
        assertThat(in.standInTerrain.get("biomesop").get(-1).contains(ChunkPos.asLong(7, 8))).isTrue();
    }

    @Test
    public void eraserRemovesOneChunkKeepingTheOther(@TempDir Path dir) throws Exception {
        File regionDir = Files.createDirectories(dir.resolve("region")).toFile();
        writeChunks(regionDir, new int[][] { { 3, 4 }, { 5, 6 } });

        RegionChunkEraser.erase(regionDir, new ChunkPos(3, 4));

        RegionFile region = new RegionFile(new File(regionDir, "r.0.0.mca"));
        try {
            assertThat(region.getChunkDataInputStream(3, 4)).isNull();
            assertThat(region.getChunkDataInputStream(5, 6)).isNotNull();
        } finally {
            region.close();
        }
        // a missing region file is nothing to do
        RegionChunkEraser.erase(regionDir, new ChunkPos(40, 40));
    }

    @Test
    public void missingTypeLeavesTheIndexAlone(@TempDir Path dir) {
        RetentionFile out = fileWithIndex("NO_SUCH_TYPE", 0, ChunkPos.asLong(1, 2));
        out.write(dir.toFile());

        RetentionFile.onWorldLoad(dir.toFile(), infoWithType(WorldType.DEFAULT));

        assertThat(RetainedContent.standInTerrain().get("NO_SUCH_TYPE").get(0).contains(ChunkPos.asLong(1, 2))
        ).isTrue();
    }

    @Test
    public void differentCurrentTypeLeavesTheIndexAlone(@TempDir Path dir) {
        RetentionFile out = fileWithIndex("default", 0, ChunkPos.asLong(1, 2));
        out.write(dir.toFile());

        RetentionFile.onWorldLoad(dir.toFile(), infoWithType(WorldType.FLAT));

        assertThat(RetainedContent.standInTerrain().get("default").get(0).contains(ChunkPos.asLong(1, 2))).isTrue();
    }

    @Test
    public void rememberedKeepClearsTheSetSilently(@TempDir Path dir) {
        RetentionFile out = fileWithIndex("default", 0, ChunkPos.asLong(1, 2));
        out.decisions.put("regen:default", "keep");
        out.write(dir.toFile());

        RetentionFile.onWorldLoad(dir.toFile(), infoWithType(WorldType.DEFAULT));

        assertThat(RetainedContent.standInTerrain().get("default").get(0).isEmpty()).isTrue();
    }

    @Test
    public void keepAnswerRecordsTheDecisionAndClears(@TempDir Path dir) {
        RetentionFile out = fileWithIndex("default", 0, ChunkPos.asLong(1, 2));
        out.write(dir.toFile());
        System.setProperty("cleanroom.standInTerrain", "keep"); // dedicated-server choice -> "Keep"

        RetentionFile.onWorldLoad(dir.toFile(), infoWithType(WorldType.DEFAULT));

        assertThat(RetainedContent.retentionDecisions().get("regen:default")).isEqualTo("keep");
        assertThat(RetainedContent.standInTerrain().get("default").get(0).isEmpty()).isTrue();
    }

    @Test
    public void serverDefaultIsKeep(@TempDir Path dir) {
        RetentionFile out = fileWithIndex("default", 0, ChunkPos.asLong(1, 2));
        out.write(dir.toFile());
        // cleanroom.standInTerrain unset: a dedicated server must not erase chunks unasked

        RetentionFile.onWorldLoad(dir.toFile(), infoWithType(WorldType.DEFAULT));

        assertThat(RetainedContent.retentionDecisions().get("regen:default")).isEqualTo("keep");
        assertThat(RetainedContent.standInTerrain().get("default").get(0).isEmpty()).isTrue();
    }

    @Test
    public void regenerateBacksUpAndErasesTheMarkedChunks(@TempDir Path dir) throws Exception {
        File worldDir = dir.toFile();
        File regionDir = Files.createDirectories(dir.resolve("region")).toFile();
        writeChunks(regionDir, new int[][] { { 3, 4 }, { 5, 6 } });
        RetentionFile out = fileWithIndex("default", 0, ChunkPos.asLong(3, 4));
        out.write(dir.toFile());
        // a server only regenerates when explicitly asked: the default answer is Keep
        System.setProperty("cleanroom.standInTerrain", "regenerate");

        RetentionFile.onWorldLoad(worldDir, infoWithType(WorldType.DEFAULT));

        RegionFile region = new RegionFile(new File(regionDir, "r.0.0.mca"));
        try {
            assertThat(region.getChunkDataInputStream(3, 4)).isNull();
            assertThat(region.getChunkDataInputStream(5, 6)).isNotNull();
        } finally {
            region.close();
        }
        assertThat(RetainedContent.standInTerrain().get("default").get(0).isEmpty()).isTrue();
        try (var zips = Files.list(dir.getParent())) {
            assertThat(zips.anyMatch(p -> p.getFileName().toString().endsWith(".zip") &&
                p.getFileName().toString().startsWith(worldDir.getName()))).isTrue();
        }
    }

}
