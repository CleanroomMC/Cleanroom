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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** {@link RetentionFile}: the record of a world's content and the Retain/Remove answers it keeps. */
@ForgeTestRunner.Isolated
public class RetentionFileTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private static WorldInfo plainInfo() {
        return new WorldInfo(new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.DEFAULT), "test");
    }

    private static ChunkBiomes chunkBiomesWithPalette(List<String> names) {
        net.minecraft.world.chunk.Chunk chunk = new net.minecraft.world.chunk.Chunk(null, 0, 0);
        NBTTagCompound chunkTag = new NBTTagCompound();
        NBTTagList palette = new NBTTagList();
        for (String n : names) {
            palette.appendTag(new NBTTagString(n));
        }
        chunkTag.setTag(ChunkBiomes.PALETTE, palette);
        if (names.size() > 1) {
            chunkTag.setByteArray(ChunkBiomes.INDICES, new byte[256]);
        }
        chunk.getBiomeIds().readNBT(chunkTag);
        return chunk.getBiomeIds();
    }

    @AfterEach
    public void tearDown() {
        RetainedContent.clearWorldState();
        System.clearProperty("cleanroom.missingContent");
    }

    @Test
    public void roundTripKeepsModdedNamesModsDecisionsAndGenerator(@TempDir Path dir) throws Exception {
        RetentionFile out = new RetentionFile();
        out.registries.put("blocks", List.of("gone_mod:crystal", "gone_mod:opal"));
        out.registries.put("items", List.of("gone_mod:wand"));
        out.mods.put("gonemod", "Gone Mod 1.2.3");
        out.decisions.put("gonemod", RetentionFile.RETAIN);
        out.generator = "GONETYPE";
        out.write(dir.toFile());

        RetentionFile in = RetentionFile.read(dir.toFile());
        assertThat(in).isNotNull();
        assertThat(in.registries.get("blocks")).isEqualTo(List.of("gone_mod:crystal", "gone_mod:opal"));
        assertThat(in.registries.get("items")).isEqualTo(List.of("gone_mod:wand"));
        assertThat(in.mods.get("gonemod")).isEqualTo("Gone Mod 1.2.3");
        assertThat(in.decisions.get("gonemod")).isEqualTo(RetentionFile.RETAIN);
        assertThat(in.generator).isEqualTo("GONETYPE");
    }

    @Test
    public void captureExcludesVanillaNames() {
        WorldInfo info = new WorldInfo(new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.DEFAULT), "test"
        );
        RetentionFile file = RetentionFile.capture(info);
        for (List<String> names : file.registries.values()) {
            for (String name : names) {
                assertThat(name.startsWith("minecraft:")).isFalse();
            }
        }
        assertThat(file.generator).isEqualTo("default");
    }

    @Test
    public void diffGroupsMissingNamesByNamespaceWithCounts() {
        RetentionFile file = new RetentionFile();
        file.registries.put("blocks", List.of("gone_mod:crystal", "gone_mod:opal", "minecraft:stone"));
        file.registries.put("items", List.of("gone_mod:wand"));
        file.registries.put("biomes", List.of("gone_mod:bog", "other_mod:fen"));

        Map<String, Map<String, Integer>> missing = file.missingByNamespace();
        assertThat(missing.get("gone_mod").get("blocks").intValue()).isEqualTo(2);
        assertThat(missing.get("gone_mod").get("items").intValue()).isEqualTo(1);
        assertThat(missing.get("gone_mod").get("biomes").intValue()).isEqualTo(1);
        assertThat(missing.get("other_mod").get("biomes").intValue()).isEqualTo(1);
        assertThat(missing.containsKey("minecraft")).isFalse();
    }

    @Test
    public void returnedNamespaceDropsItsDecision(@TempDir Path dir) {
        // the file remembers a "remove" answer for gonemod, but gonemod:stone resolves now (stone is registered)
        RetentionFile out = new RetentionFile();
        out.registries.put("blocks", List.of("minecraft:stone")); // stands in for a name that resolves again
        out.decisions.put("minecraft", RetentionFile.REMOVE);
        out.write(dir.toFile());

        RetentionFile.onWorldLoad(dir.toFile(), plainInfo());

        assertThat(RetainedContent.retentionDecisions().containsKey("minecraft")).isFalse();
        assertThat(RetainedContent.removedNamespaces().isEmpty()).isTrue();
    }

    @Test
    public void decidedRemoveAppliesSilently(@TempDir Path dir) {
        RetentionFile out = new RetentionFile();
        out.registries.put("blocks", List.of("gone_mod:crystal"));
        out.decisions.put("gone_mod", RetentionFile.REMOVE);
        out.write(dir.toFile());

        RetentionFile.onWorldLoad(dir.toFile(), plainInfo());

        assertThat(RetainedContent.isRemoved("gone_mod:crystal[glow=true]")).isTrue();
        assertThat(RetainedContent.retentionDecisions().get("gone_mod")).isEqualTo(RetentionFile.REMOVE);
    }

    @Test
    public void removedNamespaceMakesBlocksAirWithoutRetention(@TempDir Path dir) {
        RetentionFile out = new RetentionFile();
        out.registries.put("blocks", List.of("gone_mod:crystal"));
        out.decisions.put("gone_mod", RetentionFile.REMOVE);
        out.write(dir.toFile());
        RetentionFile.onWorldLoad(dir.toFile(), plainInfo());

        // a section whose whole palette is the missing block
        NBTTagCompound chunk = new NBTTagCompound();
        NBTTagList palette = new NBTTagList();
        palette.appendTag(new NBTTagString("gone_mod:crystal[glow=true]"));
        chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, palette);
        SectionBlockFormat.ChunkPaletteRead read = SectionBlockFormat.readChunkPalette(chunk);
        assertThat(read.unresolved()).isNull();

        BlockStateContainer data = new BlockStateContainer();
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0 });
        RetainedContent.Section retained = new RetainedContent.Section();
        SectionBlockFormat.read(section, data, read, retained, null);
        assertThat(retained.isEmpty()).isTrue();
        assertThat(data.get(0, 0, 0)).isEqualTo(Blocks.AIR.getDefaultState());
    }

    @Test
    public void removedNamespaceMakesItemsEmpty() {
        RetainedContent.removedNamespaces().add("gone_mod");
        com.cleanroommc.ids.MissingItem.INSTANCE = new MissingItem();
        NBTTagCompound stack = new NBTTagCompound();
        stack.setString("id", "gone_mod:wand");
        stack.setByte("Count", (byte) 1);
        ItemStack s = new ItemStack(stack);
        assertThat(s.isEmpty()).isTrue();
        RetainedContent.removedNamespaces().clear();
    }

    @Test
    public void removedNamespaceBiomeFallsToTheProvider(@TempDir Path dir) {
        RetentionFile out = new RetentionFile();
        out.registries.put("biomes", List.of("gone_mod:bog"));
        out.decisions.put("gone_mod", RetentionFile.REMOVE);
        out.write(dir.toFile());
        RetentionFile.onWorldLoad(dir.toFile(), plainInfo());

        ChunkBiomes biomes = chunkBiomesWithPalette(List.of("gone_mod:bog"));
        NBTTagCompound written = new NBTTagCompound();
        biomes.writeNBT(written);
        NBTTagList palette = written.getTagList(ChunkBiomes.PALETTE, 8);
        for (int i = 0; i < palette.tagCount(); i++) {
            assertThat(palette.getStringTagAt(i).startsWith("gone_mod:")).isFalse();
        }
    }

    @Test
    public void corruptFileIsNoInformation(@TempDir Path dir) throws Exception {
        java.nio.file.Files.write(dir.resolve(RetentionFile.FILE_NAME), new byte[] { 1, 2, 3, 4 });
        assertThat(RetentionFile.read(dir.toFile())).isNull();
        // and the load proceeds without a prompt or an exception
        RetentionFile.onWorldLoad(dir.toFile(), plainInfo());
        assertThat(RetainedContent.removedNamespaces().isEmpty()).isTrue();
    }

    @Test
    public void missingFileIsNoInformation(@TempDir Path dir) {
        assertThat(RetentionFile.read(dir.toFile())).isNull();
        RetentionFile.onWorldLoad(dir.toFile(), plainInfo());
    }

    @Test
    public void captureAfterLoadPreservesMissingCensusAndDecisionAcrossSecondLoad(@TempDir Path dir) {
        RetentionFile initial = new RetentionFile();
        initial.registries.put("blocks", List.of("gone_mod:crystal"));
        initial.mods.put("gone_mod", "Gone Mod 1.0");
        initial.decisions.put("gone_mod", RetentionFile.REMOVE);
        initial.write(dir.toFile());

        WorldInfo info = plainInfo();
        RetentionFile.onWorldLoad(dir.toFile(), info);
        assertThat(RetainedContent.isRemoved("gone_mod:crystal")).isTrue();

        // Save while gone_mod is still uninstalled, then reload a second time
        RetentionFile.capture(info).write(dir.toFile());
        RetentionFile.onWorldLoad(dir.toFile(), info);
        assertThat(RetainedContent.isRemoved("gone_mod:crystal")).isTrue();
        assertThat(RetainedContent.retentionDecisions().get("gone_mod")).isEqualTo(RetentionFile.REMOVE);
    }

}
