package net.minecraftforge.registries;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;

import com.cleanroommc.ids.SectionBlockFormat;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Save-format cost of a chunk section, old Anvil (Blocks/Data/Add) vs the palette format: encode, decode and
 * compressed size, on sections shaped like the measured world (terrain with ~10 states per section). Prints
 * timings; only asserts that both formats round-trip.
 */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation") // Cleanroom: metadata uses in this file are the legacy view
public class SectionFormatBenchmark
{
    private static final int SECTIONS = 512;
    private static final int ROUNDS = 12;

    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
    }

    private static BlockStateContainer[] sections()
    {
        IBlockState[] common = { Blocks.STONE.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState(),
            Blocks.AIR.getDefaultState(), Blocks.STONE.getStateFromMeta(1), Blocks.STONE.getStateFromMeta(3), Blocks.STONE.getStateFromMeta(5) };
        IBlockState[] rare = { Blocks.COAL_ORE.getDefaultState(), Blocks.IRON_ORE.getDefaultState(), Blocks.WATER.getDefaultState(),
            Blocks.LAVA.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(), Blocks.REDSTONE_ORE.getDefaultState() };
        Random random = new Random(7);
        BlockStateContainer[] sections = new BlockStateContainer[SECTIONS];
        for (int s = 0; s < SECTIONS; s++)
        {
            BlockStateContainer data = new BlockStateContainer();
            for (int i = 0; i < 4096; i++)
            {
                IBlockState state = random.nextInt(50) == 0 ? rare[random.nextInt(rare.length)] : common[(i >> 8) * common.length / 16];
                data.set(i & 15, i >> 8, i >> 4 & 15, state);
            }
            sections[s] = data;
        }
        return sections;
    }

    private static NBTTagCompound writeLegacy(BlockStateContainer data)
    {
        NBTTagCompound tag = new NBTTagCompound();
        byte[] blocks = new byte[4096];
        NibbleArray meta = new NibbleArray();
        NibbleArray add = null;
        for (int i = 0; i < 4096; i++)
        {
            IBlockState state = data.get(i & 15, i >> 8, i >> 4 & 15);
            int j = Block.getIdFromBlock(state.getBlock()) << 4 | state.getBlock().getMetaFromState(state);
            int k = i & 15;
            int l = i >> 8 & 15;
            int i1 = i >> 4 & 15;
            if ((j >> 12 & 15) != 0)
            {
                if (add == null)
                {
                    add = new NibbleArray();
                }
                add.set(k, l, i1, j >> 12 & 15);
            }
            blocks[i] = (byte)(j >> 4 & 0xFF);
            meta.set(k, l, i1, j & 15);
        }
        tag.setByteArray("Blocks", blocks);
        tag.setByteArray("Data", meta.getData());
        if (add != null)
        {
            tag.setByteArray("Add", add.getData());
        }
        return tag;
    }

    private static void readLegacy(NBTTagCompound tag, BlockStateContainer into)
    {
        into.setDataFromNBT(tag.getByteArray("Blocks"), new NibbleArray(tag.getByteArray("Data")),
            tag.hasKey("Add", 7) ? new NibbleArray(tag.getByteArray("Add")) : null);
    }

    /** One chunk: 16 sections and the chunk palette they share. */
    private static NBTTagCompound writePaletteChunk(BlockStateContainer[] sections, int first)
    {
        NBTTagCompound chunk = new NBTTagCompound();
        SectionBlockFormat.ChunkPalette palette = new SectionBlockFormat.ChunkPalette();
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        for (int s = first; s < first + 16; s++)
        {
            NBTTagCompound tag = new NBTTagCompound();
            SectionBlockFormat.write(sections[s], tag, palette);
            list.appendTag(tag);
        }
        chunk.setTag("Sections", list);
        chunk.setTag(SectionBlockFormat.CHUNK_PALETTE, palette.toNBT());
        return chunk;
    }

    private static void readPaletteChunk(NBTTagCompound chunk)
    {
        SectionBlockFormat.ChunkPaletteRead palette = SectionBlockFormat.readChunkPalette(chunk);
        net.minecraft.nbt.NBTTagList list = chunk.getTagList("Sections", 10);
        for (int s = 0; s < list.tagCount(); s++)
        {
            SectionBlockFormat.read(list.getCompoundTagAt(s), new BlockStateContainer(), palette);
        }
    }

    private static NBTTagCompound legacyChunk(NBTTagCompound[] legacy, int first)
    {
        NBTTagCompound chunk = new NBTTagCompound();
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        for (int s = first; s < first + 16; s++)
        {
            list.appendTag(legacy[s]);
        }
        chunk.setTag("Sections", list);
        return chunk;
    }

    private static int compressedSize(NBTTagCompound tag) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bytes)))
        {
            CompressedStreamTools.write(tag, out);
        }
        return bytes.size();
    }

    @Test
    public void legacyVsPalette() throws IOException
    {
        BlockStateContainer[] sections = sections();
        int chunks = SECTIONS / 16;
        NBTTagCompound[] legacy = new NBTTagCompound[SECTIONS];
        NBTTagCompound[] palette = new NBTTagCompound[chunks];
        long legacyWrite = Long.MAX_VALUE;
        long paletteWrite = Long.MAX_VALUE;
        long legacyRead = Long.MAX_VALUE;
        long paletteRead = Long.MAX_VALUE;
        for (int round = 0; round < ROUNDS; round++)
        {
            long t0 = System.nanoTime();
            for (int s = 0; s < SECTIONS; s++)
            {
                legacy[s] = writeLegacy(sections[s]);
            }
            long t1 = System.nanoTime();
            for (int c = 0; c < chunks; c++)
            {
                palette[c] = writePaletteChunk(sections, c * 16);
            }
            long t2 = System.nanoTime();
            for (int s = 0; s < SECTIONS; s++)
            {
                readLegacy(legacy[s], new BlockStateContainer());
            }
            long t3 = System.nanoTime();
            for (int c = 0; c < chunks; c++)
            {
                readPaletteChunk(palette[c]);
            }
            long t4 = System.nanoTime();
            legacyWrite = Math.min(legacyWrite, t1 - t0);
            paletteWrite = Math.min(paletteWrite, t2 - t1);
            legacyRead = Math.min(legacyRead, t3 - t2);
            paletteRead = Math.min(paletteRead, t4 - t3);
        }
        long legacyBytes = 0;
        long paletteBytes = 0;
        for (int c = 0; c < chunks; c++)
        {
            legacyBytes += compressedSize(legacyChunk(legacy, c * 16));
            paletteBytes += compressedSize(palette[c]);
        }
        System.out.printf("[SectionFormatBenchmark] per section (best of %d rounds x %d sections, chunks of 16):%n", ROUNDS, SECTIONS);
        System.out.printf("[SectionFormatBenchmark]   write  legacy %6.1f us  palette %6.1f us%n", legacyWrite / 1e3 / SECTIONS, paletteWrite / 1e3 / SECTIONS);
        System.out.printf("[SectionFormatBenchmark]   read   legacy %6.1f us  palette %6.1f us%n", legacyRead / 1e3 / SECTIONS, paletteRead / 1e3 / SECTIONS);
        System.out.printf("[SectionFormatBenchmark]   size   legacy %6d B   palette %6d B (deflated, per section)%n", legacyBytes / SECTIONS, paletteBytes / SECTIONS);

        SectionBlockFormat.ChunkPaletteRead chunkPalette = SectionBlockFormat.readChunkPalette(palette[0]);
        net.minecraft.nbt.NBTTagList list = palette[0].getTagList("Sections", 10);
        for (int s = 0; s < 16; s++)
        {
            BlockStateContainer back = new BlockStateContainer();
            SectionBlockFormat.read(list.getCompoundTagAt(s), back, chunkPalette);
            BlockStateContainer backLegacy = new BlockStateContainer();
            readLegacy(legacy[s], backLegacy);
            for (int i = 0; i < 4096; i++)
            {
                assertSame(sections[s].get(i & 15, i >> 8, i >> 4 & 15), back.get(i & 15, i >> 8, i >> 4 & 15));
                assertSame(sections[s].get(i & 15, i >> 8, i >> 4 & 15), backLegacy.get(i & 15, i >> 8, i >> 4 & 15));
            }
        }
    }
}
