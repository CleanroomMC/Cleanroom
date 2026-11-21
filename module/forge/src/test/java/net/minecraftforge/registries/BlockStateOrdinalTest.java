package net.minecraftforge.registries;

import java.util.List;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * {@link IBlockState#getOrdinal()}: each listed state's index inside its container's valid-state list.
 */
@ForgeTestRunner.Isolated
public class BlockStateOrdinalTest
{
    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void ordinalsAreContiguousPerBlock()
    {
        for (net.minecraft.block.Block block : new net.minecraft.block.Block[] { Blocks.AIR, Blocks.STONE, Blocks.WOOL, Blocks.DOUBLE_PLANT })
        {
            List<IBlockState> states = block.getBlockState().getValidStates();
            for (int i = 0; i < states.size(); i++)
            {
                assertEquals(i, states.get(i).getOrdinal(), block.getRegistryName() + " state " + i);
            }
        }
    }

    @Test
    public void ordinalsAreStableAcrossContainersBuiltFromTheSameProperties()
    {
        BlockStateContainer second = new BlockStateContainer(Blocks.WOOL, BlockColored.COLOR);
        List<IBlockState> first = Blocks.WOOL.getBlockState().getValidStates();
        List<IBlockState> again = second.getValidStates();
        assertEquals(first.size(), again.size());
        for (int i = 0; i < first.size(); i++)
        {
            assertNotSame(first.get(i), again.get(i));
            assertEquals(i, again.get(i).getOrdinal());
            assertEquals(first.get(i).getValue(BlockColored.COLOR), again.get(i).getValue(BlockColored.COLOR));
        }
    }
}
