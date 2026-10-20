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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
public class StandInsTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
        StandIns.load(null);
    }

    @AfterAll
    public static void cleanup() {
        StandIns.load(null);
    }

    @Test
    public void defaultTableCoversGroundBlocksAndSkipsPlants() {
        IBlockState leaves = StandIns.forMissing("gone:leaves_2[decayable=true,variant=redwood]");
        assertThat(leaves.getBlock()).isEqualTo(Blocks.LEAVES);
        assertThat(leaves.getValue(BlockLeaves.DECAYABLE)).isEqualTo(false);
        assertThat(leaves.getValue(BlockLeaves.CHECK_DECAY)).isEqualTo(false);

        assertThat(StandIns.forMissing("gone:log_0[axis=z,variant=cherry]")).isEqualTo(Blocks.LOG
                .getDefaultState()
                .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z)
        );
        assertThat(StandIns.forMissing("gone:planks_0[variant=willow]")).isEqualTo(Blocks.PLANKS.getDefaultState());
        assertThat(StandIns.forMissing("gone:stairs[facing=east,half=top,shape=straight]")).isEqualTo(Blocks.OAK_STAIRS
                .getDefaultState()
                .withProperty(BlockStairs.FACING, EnumFacing.EAST)
                .withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP)
        );
        assertThat(StandIns.forMissing("gone:slab[half=top]")).isEqualTo(Blocks.WOODEN_SLAB
                .getDefaultState()
                .withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP)
        );
        assertThat(StandIns.forMissing("gone:double_slab[]")).isEqualTo(Blocks.DOUBLE_WOODEN_SLAB.getDefaultState());
        assertThat(StandIns.forMissing("gone:fence_0[]")).isEqualTo(Blocks.OAK_FENCE.getDefaultState());
        IBlockState fenceGate = StandIns.forMissing("gone:fence_gate[facing=east,open=true]");
        assertThat(fenceGate.getBlock()).isEqualTo(Blocks.OAK_FENCE_GATE);
        assertThat(StandIns.forMissing("gone:grass[snowy=false,variant=loamy]"
        )).isEqualTo(Blocks.GRASS.getDefaultState());
        assertThat(StandIns.forMissing("gone:dirt[coarse=true,variant=loamy]")).isEqualTo(Blocks.DIRT
                .getDefaultState()
                .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        );
        assertThat(StandIns.forMissing("gone:mud[]")).isEqualTo(Blocks.DIRT.getDefaultState());
        assertThat(StandIns.forMissing("gone:sand_0[variant=white]")).isEqualTo(Blocks.SAND.getDefaultState());
        assertThat(StandIns.forMissing("gone:sandstone[type=smooth]")).isEqualTo(Blocks.SANDSTONE.getDefaultState());
        assertThat(StandIns.forMissing("gone:gravel[]")).isEqualTo(Blocks.GRAVEL.getDefaultState());
        assertThat(StandIns.forMissing("gone:gem_ore[variant=sapphire]")).isEqualTo(Blocks.STONE.getDefaultState());
        assertThat(StandIns.forMissing("gone:rock_0[variant=shale]")).isEqualTo(Blocks.STONE.getDefaultState());
    }

    @Test
    public void plantsFluidsAndUnmatchedReadAsAir() {
        assertThat(StandIns.forMissing("gone:double_plant[half=lower,variant=sea_oats]")).isNull();
        assertThat(StandIns.forMissing("gone:flower_1[variant=x]")).isNull();
        assertThat(StandIns.forMissing("gone:poison[level=0]")).isNull();
        assertThat(StandIns.forMissing("gone:willow_vine[north=true]")).isNull();
        assertThat(StandIns.forMissing("gone:mushroom[variant=brown]")).isNull();
        assertThat(StandIns.forMissing("gone:tall_grass[type=x]")).isNull();
        assertThat(StandIns.forMissing("gone:sapling[variant=palm]")).isNull();
    }

    @Test
    public void configOverridesApplyBeforeTheDefaults() throws Exception {
        Path file = Files.createTempFile("retained_standins", ".cfg");
        Files.write(file, List.of("# comment", "sapphire = minecraft:lapis_ore", "leaves = air", "not a line"));
        StandIns.load(file);
        assertThat(StandIns.forMissing("gone:sapphire_ore[variant=x]")).isEqualTo(Blocks.LAPIS_ORE.getDefaultState());
        assertThat(StandIns.forMissing("gone:leaves_0[variant=x]")).isNull();
        // the defaults still apply to paths no override matched
        assertThat(StandIns.forMissing("gone:gem_ore[]")).isEqualTo(Blocks.STONE.getDefaultState());
        StandIns.load(null);
        assertThat(StandIns.forMissing("gone:leaves_0[]").getBlock()).isEqualTo(Blocks.LEAVES
                .getDefaultState()
                .getBlock()
        );
        Files.deleteIfExists(file);
    }

    @Test
    public void defaultBiomeTable() {
        assertThat(StandIns.forMissingBiome("biomesoplenty:ominous_woods")).isSameAs(Biomes.ROOFED_FOREST);
        assertThat(StandIns.forMissingBiome("biomesoplenty:bayou")).isSameAs(Biomes.SWAMPLAND);
        assertThat(StandIns.forMissingBiome("traverse:mini_jungle")).isSameAs(Biomes.JUNGLE);
        assertThat(StandIns.forMissingBiome("byg:alps")).isSameAs(Biomes.ICE_PLAINS);
        assertThat(StandIns.forMissingBiome("biomesoplenty:outback")).isSameAs(Biomes.MESA);
        assertThat(StandIns.forMissingBiome("biomesoplenty:cherry_blossom_grove")).isSameAs(Biomes.FOREST);
        assertThat(StandIns.forMissingBiome("biomesoplenty:orchard")).isSameAs(Biomes.PLAINS);
        assertThat(StandIns.forMissingBiome("biomesoplenty:redwood_forest")).isSameAs(Biomes.TAIGA);
        assertThat(StandIns.forMissingBiome("biomesoplenty:xyzzy")).isNull();
        assertThat(StandIns.forMissingBiome("biomesoplenty:coral_reef")).isSameAs(Biomes.OCEAN);
        assertThat(StandIns.forMissingBiome("biomesoplenty:snowy_coniferous_forest")).isSameAs(Biomes.ICE_PLAINS);
        assertThat(StandIns.forMissingBiome("twilightforest:dark_forest")).isSameAs(Biomes.ROOFED_FOREST);
        // "sea" matches (ocean group) before "dead" (roofed group) — a dead sea is still an ocean column
        assertThat(StandIns.forMissingBiome("byg:dead_sea")).isSameAs(Biomes.OCEAN);
    }

    @Test
    public void biomeConfigOverridesApplyBeforeTheDefaults() throws Exception {
        Path file = Files.createTempFile("retained_standins", ".cfg");
        Files.write(file, List.of("biome:ominous = minecraft:swampland", "biome:bayou = none"));
        StandIns.load(file);
        assertThat(StandIns.forMissingBiome("biomesoplenty:ominous_woods")).isSameAs(Biomes.SWAMPLAND);
        assertThat(StandIns.forMissingBiome("biomesoplenty:bayou")).isNull();
        StandIns.load(null);
        assertThat(StandIns.forMissingBiome("biomesoplenty:bayou")).isSameAs(Biomes.SWAMPLAND);
        Files.deleteIfExists(file);
    }

}
