package net.minecraftforge.registries;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Harvest tool/level tables are per valid state (indexed by {@link IBlockState#getOrdinal()}), so a block with
 * more than sixteen states keeps distinct levels.
 */
@ForgeTestRunner.Isolated
public class HarvestLevelTest
{
    private static final class SixtyFourStateBlock extends Block
    {
        private static final PropertyInteger A = PropertyInteger.create("a", 0, 7);
        private static final PropertyInteger B = PropertyInteger.create("b", 0, 7);

        private SixtyFourStateBlock()
        {
            super(Material.ROCK);
        }

        @Override
        protected BlockStateContainer createBlockState()
        {
            return new BlockStateContainer(this, A, B);
        }
    }

    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void perStateLevelsSurviveOnA64StateBlock()
    {
        SixtyFourStateBlock block = new SixtyFourStateBlock();
        assertEquals(64, block.getBlockState().getValidStates().size());
        IBlockState target = block.getDefaultState().withProperty(SixtyFourStateBlock.A, 3).withProperty(SixtyFourStateBlock.B, 5);
        block.setHarvestLevel("pickaxe", 2, target);
        for (IBlockState state : block.getBlockState().getValidStates())
        {
            assertEquals(state == target ? 2 : -1, block.getHarvestLevel(state), String.valueOf(state));
            assertEquals(state == target ? "pickaxe" : null, block.getHarvestTool(state));
        }
    }

    @Test
    public void theAllStatesSetterCoversEveryOrdinal()
    {
        SixtyFourStateBlock block = new SixtyFourStateBlock();
        block.setHarvestLevel("shovel", 1);
        for (IBlockState state : block.getBlockState().getValidStates())
        {
            assertEquals(1, block.getHarvestLevel(state), String.valueOf(state));
            assertEquals("shovel", block.getHarvestTool(state));
        }
    }

    @Test
    public void aFreshBlockReportsNoToolAndLevelMinusOne()
    {
        SixtyFourStateBlock block = new SixtyFourStateBlock();
        for (IBlockState state : block.getBlockState().getValidStates())
        {
            assertNull(block.getHarvestTool(state));
            assertEquals(-1, block.getHarvestLevel(state));
        }
    }
}
