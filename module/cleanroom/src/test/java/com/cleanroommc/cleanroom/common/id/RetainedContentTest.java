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
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/** Missing-content retention: unresolved names survive a load-save cycle; a real write wins. */
@ForgeTestRunner.Isolated
public class RetainedContentTest {

    private static final String CRYSTAL = "gone_mod:crystal[glow=true]";
    private static final String OPAL = "gone_mod:opal[glow=true]";
    private static final IProperty<Boolean> GLOW = PropertyBool.create("glow");

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private static World stubWorld() {
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

    private static NBTTagCompound chunkTag(NBTTagList blockPalette) {
        NBTTagCompound chunk = new NBTTagCompound();
        chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, blockPalette);
        return chunk;
    }

    private static NBTTagCompound missingSection() {
        NBTTagCompound section = new NBTTagCompound();
        section.setByte("Y", (byte) 0);
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0, 1 });
        long[] states = new long[SectionBlockFormat.longCount(4)];
        states[0] = 1L << 32; // cell 7 keeps entry 0 (the missing name), cell 8 takes entry 1 (stone)
        section.setTag(SectionBlockFormat.STATES, new NBTTagLongArray(states));
        return section;
    }

    private static NBTTagCompound readThenWrite(NBTTagCompound section, NBTTagCompound chunk,
        RetainedContent.Section retained, BlockStateContainer data
    ) {
        SectionBlockFormat.read(section, data, SectionBlockFormat.readChunkPalette(chunk), retained, new ChunkPos(0, 0)
        );
        SectionBlockFormat.ChunkPalette palette = new SectionBlockFormat.ChunkPalette();
        NBTTagCompound written = new NBTTagCompound();
        SectionBlockFormat.write(data, written, palette, retained);
        NBTTagCompound writtenChunk = chunkTag(palette.toNBT());
        written.setTag("CHUNK", writtenChunk);
        return written;
    }

    private static boolean paletteContains(NBTTagList palette, String name) {
        for (int i = 0; i < palette.tagCount(); i++) {
            if (palette.getStringTagAt(i).equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void unknownBlockIsAirButKeepsItsName() {
        NBTTagCompound chunk = chunkTag(new NBTTagList());
        ((NBTTagList) chunk.getTag(SectionBlockFormat.CHUNK_PALETTE)).appendTag(new NBTTagString(OPAL));
        ((NBTTagList) chunk.getTag(SectionBlockFormat.CHUNK_PALETTE)).appendTag(new NBTTagString("minecraft:stone"));
        BlockStateContainer data = new BlockStateContainer();
        RetainedContent.Section retained = new RetainedContent.Section();
        SectionBlockFormat.read(missingSection(), data, SectionBlockFormat.readChunkPalette(chunk), retained,
            new ChunkPos(0, 0)
        );
        assertThat(data.get(7, 0, 0)).isSameAs(Blocks.AIR.getDefaultState());
        assertThat(data.get(8, 0, 0)).isSameAs(Blocks.STONE.getDefaultState());
        assertThat(retained.name(7)).isEqualTo(OPAL);
        assertThat(retained.name(8)).isNull();

        SectionBlockFormat.ChunkPalette out = new SectionBlockFormat.ChunkPalette();
        NBTTagCompound outSection = new NBTTagCompound();
        SectionBlockFormat.write(data, outSection, out, retained);
        NBTTagList outPalette = out.toNBT();
        assertThat(paletteContains(outPalette, OPAL)).isTrue();
        int[] local = outSection.getIntArray(SectionBlockFormat.PALETTE);
        int cell7Entry = SectionBlockFormat.unpack(((NBTTagLongArray) outSection.getTag(SectionBlockFormat.STATES
            )).getLongArray(),
            4
        )[7];
        assertThat(outPalette.getStringTagAt(local[cell7Entry])).isEqualTo(OPAL);

        // stable across cycles: the same section written back still reads as air with the name retained
        BlockStateContainer again = new BlockStateContainer();
        RetainedContent.Section retainedAgain = new RetainedContent.Section();
        SectionBlockFormat.read(outSection, again, SectionBlockFormat.readChunkPalette(chunkTag(outPalette)),
            retainedAgain, new ChunkPos(0, 0)
        );
        assertThat(again.get(7, 0, 0)).isSameAs(Blocks.AIR.getDefaultState());
        assertThat(retainedAgain.name(7)).isEqualTo(OPAL);
    }

    @Test
    public void retainedBlockReturnsWhenTheModDoes() {
        NBTTagCompound chunk = chunkTag(new NBTTagList());
        ((NBTTagList) chunk.getTag(SectionBlockFormat.CHUNK_PALETTE)).appendTag(new NBTTagString(CRYSTAL));
        ((NBTTagList) chunk.getTag(SectionBlockFormat.CHUNK_PALETTE)).appendTag(new NBTTagString("minecraft:stone"));
        BlockStateContainer data = new BlockStateContainer();
        RetainedContent.Section retained = new RetainedContent.Section();
        NBTTagCompound section = missingSection();
        SectionBlockFormat.read(section, data, SectionBlockFormat.readChunkPalette(chunk), retained, new ChunkPos(0, 0)
        );
        SectionBlockFormat.ChunkPalette out = new SectionBlockFormat.ChunkPalette();
        NBTTagCompound outSection = new NBTTagCompound();
        SectionBlockFormat.write(data, outSection, out, retained);

        Block crystal = new Block(Material.ROCK) {

            @Override
            protected net.minecraft.block.state.BlockStateContainer createBlockState() {
                return new net.minecraft.block.state.BlockStateContainer(this, GLOW);
            }

        };
        crystal.setRegistryName(new ResourceLocation("gone_mod", "crystal"));
        ForgeRegistries.BLOCKS.register(crystal);

        BlockStateContainer back = new BlockStateContainer();
        RetainedContent.Section nothingRetained = new RetainedContent.Section();
        SectionBlockFormat.read(outSection, back, SectionBlockFormat.readChunkPalette(chunkTag(out.toNBT())),
            nothingRetained, new ChunkPos(0, 0)
        );
        assertThat(back.get(7, 0, 0)).isSameAs(crystal.getDefaultState().withProperty(GLOW, Boolean.TRUE));
        assertThat(nothingRetained.isEmpty()).isTrue();
    }

    @Test
    public void writingTheCellDropsTheName() {
        ExtendedBlockStorage storage = new ExtendedBlockStorage(0, false);
        RetainedContent.Section retained = new RetainedContent.Section();
        retained.retain(7, "gone_mod:crystal");
        retained.retain(9, "gone_mod:gem");
        storage.retained = retained;
        storage.set(7, 0, 0, Blocks.CHEST.getDefaultState());
        storage.set(9, 0, 0, Blocks.AIR.getDefaultState());
        assertThat(retained.name(7)).isNull();
        assertThat(retained.name(9)).isNull();

        SectionBlockFormat.ChunkPalette out = new SectionBlockFormat.ChunkPalette();
        NBTTagCompound outSection = new NBTTagCompound();
        SectionBlockFormat.write(storage.getData(), outSection, out, retained);
        assertThat(paletteContains(out.toNBT(), "gone_mod:crystal")).isFalse();
        assertThat(paletteContains(out.toNBT(), "gone_mod:gem")).isFalse();
    }

    @Test
    public void realAirIsNotRetained() {
        NBTTagList names = new NBTTagList();
        names.appendTag(new NBTTagString("minecraft:air"));
        BlockStateContainer data = new BlockStateContainer();
        RetainedContent.Section retained = new RetainedContent.Section();
        NBTTagCompound section = new NBTTagCompound();
        section.setIntArray(SectionBlockFormat.PALETTE, new int[] { 0 });
        SectionBlockFormat.read(section, data, SectionBlockFormat.readChunkPalette(chunkTag(names)), retained,
            new ChunkPos(0, 0)
        );
        assertThat(retained.isEmpty()).isTrue();
        assertThat(data.get(0, 0, 0)).isSameAs(Blocks.AIR.getDefaultState());
    }

    @Test
    public void unknownTileEntityIsKeptAndRewrittenOnlyOverAir() throws Exception {
        World world = stubWorld();
        AnvilChunkLoader loader = new AnvilChunkLoader(new File("."), null);
        Chunk chunk = new Chunk(world, 0, 0);
        NBTTagCompound level = new NBTTagCompound();
        NBTTagList tileEntities = new NBTTagList();
        NBTTagCompound furnace = new NBTTagCompound();
        furnace.setString("id", "gone_mod:furnace");
        furnace.setInteger("x", 1);
        furnace.setInteger("y", 2);
        furnace.setInteger("z", 3);
        tileEntities.appendTag(furnace);
        level.setTag("TileEntities", tileEntities);
        loader.loadEntities(world, level, chunk);
        assertThat(chunk.retained.tileEntities.size()).isEqualTo(1);
        assertThat(chunk.retained.tileEntities.get(0)).isSameAs(furnace);

        Method write = AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT", Chunk.class, World.class,
            NBTTagCompound.class
        );
        write.setAccessible(true);
        NBTTagCompound out = new NBTTagCompound();
        write.invoke(loader, chunk, world, out);
        NBTTagList written = out.getTagList("TileEntities", 10);
        assertThat(written.tagCount()).isEqualTo(1);
        assertThat(written.getCompoundTagAt(0).getString("id")).isEqualTo("gone_mod:furnace");

        // a player build at its position wins
        ExtendedBlockStorage section = new ExtendedBlockStorage(0, false);
        section.set(1, 2, 3, Blocks.CHEST.getDefaultState());
        chunk.getBlockStorageArray()[0] = section;
        NBTTagCompound rebuilt = new NBTTagCompound();
        write.invoke(loader, chunk, world, rebuilt);
        NBTTagList second = rebuilt.getTagList("TileEntities", 10);
        for (int i = 0; i < second.tagCount(); i++) {
            assertThat(second.getCompoundTagAt(i).getString("id").equals("gone_mod:furnace")).isFalse();
        }
    }

    @Test
    public void unknownEntityIsKeptAndRewritten() throws Exception {
        World world = stubWorld();
        AnvilChunkLoader loader = new AnvilChunkLoader(new File("."), null);
        Chunk chunk = new Chunk(world, 0, 0);
        NBTTagCompound level = new NBTTagCompound();
        NBTTagList entities = new NBTTagList();
        NBTTagCompound beast = new NBTTagCompound();
        beast.setString("id", "gone_mod:beast");
        entities.appendTag(beast);
        NBTTagCompound pig = new NBTTagCompound();
        pig.setString("id", "minecraft:pig");
        entities.appendTag(pig);
        level.setTag("Entities", entities);
        level.setTag("TileEntities", new NBTTagList());
        loader.loadEntities(world, level, chunk);
        assertThat(chunk.retained.entities.size()).isEqualTo(1);
        assertThat(chunk.retained.entities.get(0)).isSameAs(beast);
        assertThat(chunk.getEntityLists()[0].isEmpty()).isFalse();

        Method write = AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT", Chunk.class, World.class,
            NBTTagCompound.class
        );
        write.setAccessible(true);
        NBTTagCompound out = new NBTTagCompound();
        write.invoke(loader, chunk, world, out);
        NBTTagList written = out.getTagList("Entities", 10);
        boolean found = false;
        for (int i = 0; i < written.tagCount(); i++) {
            if (written.getCompoundTagAt(i).getString("id").equals("gone_mod:beast")) {
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    /* The stand-in-terrain mark is stored in the chunk's Level compound and survives a save/load cycle. */
    @Test
    public void generatedWithoutWorldTypeIsWrittenAndRead() throws Exception {
        World world = stubWorld();
        AnvilChunkLoader loader = new AnvilChunkLoader(new File("."), null);
        Chunk chunk = new Chunk(world, 0, 0);
        chunk.retained.setGeneratedWithoutWorldType("BIOMESOP");

        Method write = AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT", Chunk.class, World.class,
            NBTTagCompound.class
        );
        write.setAccessible(true);
        NBTTagCompound level = new NBTTagCompound();
        write.invoke(loader, chunk, world, level);
        assertThat(level.getString("GeneratedWithoutWorldType")).isEqualTo("BIOMESOP");

        Chunk back = new Chunk(world, 0, 0);
        back.retained.readLevelTags(level); // the seam readChunkFromNBT calls with the Level compound
        assertThat(back.retained.getGeneratedWithoutWorldType()).isEqualTo("BIOMESOP");
    }

    @Test
    public void unmarkedChunksWriteNoGeneratedWithoutWorldTypeTag() throws Exception {
        World world = stubWorld();
        AnvilChunkLoader loader = new AnvilChunkLoader(new File("."), null);
        Chunk chunk = new Chunk(world, 0, 0);
        Method write = AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT", Chunk.class, World.class,
            NBTTagCompound.class
        );
        write.setAccessible(true);
        NBTTagCompound level = new NBTTagCompound();
        write.invoke(loader, chunk, world, level);
        assertThat(level.hasKey("GeneratedWithoutWorldType")).isFalse();
        assertThat(chunk.retained.getGeneratedWithoutWorldType()).isNull();
    }

    /* Generation writes keep the mark; a write after terrain population (gameplay) drops it. */
    @Test
    public void gameplayWriteClearsTheMark() {
        World world = stubWorld();
        Chunk chunk = new Chunk(world, 0, 0);
        chunk.retained.setGeneratedWithoutWorldType("BIOMESOP");
        chunk.setTerrainPopulated(false);
        chunk.setBlockState(new BlockPos(1, 64, 1), Blocks.STONE.getDefaultState());
        assertThat(chunk.retained.getGeneratedWithoutWorldType()).isEqualTo("BIOMESOP");
        chunk.setTerrainPopulated(true);
        chunk.setBlockState(new BlockPos(2, 64, 2), Blocks.DIRT.getDefaultState());
        assertThat(chunk.retained.getGeneratedWithoutWorldType()).isNull();
    }

    /* Population writes (populating != null while isTerrainPopulated is already set) keep the mark too. */
    @Test
    public void populationWritesKeepTheMark() throws Exception {
        World world = stubWorld();
        Chunk chunk = new Chunk(world, 0, 0);
        chunk.retained.setGeneratedWithoutWorldType("BIOMESOP");
        chunk.setTerrainPopulated(true);
        Field populating = Chunk.class.getDeclaredField("populating");
        populating.setAccessible(true);
        try {
            populating.set(null, chunk.getPos());
            chunk.setBlockState(new BlockPos(1, 64, 1), Blocks.STONE.getDefaultState());
            assertThat(chunk.retained.getGeneratedWithoutWorldType()).isEqualTo("BIOMESOP");
        } finally {
            populating.set(null, null);
        }
        chunk.setBlockState(new BlockPos(2, 64, 2), Blocks.DIRT.getDefaultState());
        assertThat(chunk.retained.getGeneratedWithoutWorldType()).isNull();
    }

    @Test
    public void missingNamesAreLoggedOnce() {
        RetainedContent.resetLogged();
        RetainedContent.noteMissing("gone_mod:once", new ChunkPos(0, 0));
        RetainedContent.noteMissing("gone_mod:once", new ChunkPos(5, 5));
        RetainedContent.noteMissing("gone_mod:twice", new ChunkPos(1, 1));
        assertThat(RetainedContent.LOGGED.size()).isEqualTo(2);
        RetainedContent.resetLogged();
    }

}
