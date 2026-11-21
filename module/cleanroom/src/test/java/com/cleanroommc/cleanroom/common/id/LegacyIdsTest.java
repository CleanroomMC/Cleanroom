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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.registries.ForgeRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LegacyIds}: numeric ids stored in an old world are the Forge-era numbers recorded in level.dat's
 * {@code FML}/{@code Registries} table, not live derived ids. Every legacy numeric read resolves saved id
 * -> saved name -> live object; ids absent from the table fall back to live lookup.
 */
@ForgeTestRunner.Isolated
public class LegacyIdsTest {

    private static final ResourceLocation ORE = new ResourceLocation("wideids_test", "ore");
    private static final ResourceLocation MARSH = new ResourceLocation("wideids_test", "marsh");
    private static final ResourceLocation EFFECT = new ResourceLocation("wideids_test", "legacy_effect");
    private static final ResourceLocation ENCH = new ResourceLocation("wideids_test", "legacy_ench");
    private static final int ORE_SAVED_ID = 300;
    private static final int MARSH_SAVED_ID = 200;
    private static final int EFFECT_SAVED_ID = 77;
    private static final int ENCH_SAVED_ID = 88;

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private static Block ore() {
        if (!ForgeRegistries.BLOCKS.containsKey(ORE)) {
            ForgeRegistries.BLOCKS.register(new Block(Material.ROCK).setRegistryName(ORE));
        }
        return ForgeRegistries.BLOCKS.getValue(ORE);
    }

    private static Biome marsh() {
        if (!ForgeRegistries.BIOMES.containsKey(MARSH)) {
            Biome biome = new Biome(new Biome.BiomeProperties("Marsh")) { };
            biome.setRegistryName(MARSH);
            ForgeRegistries.BIOMES.register(biome);
        }
        return ForgeRegistries.BIOMES.getValue(MARSH);
    }

    private static Potion effect() {
        if (!ForgeRegistries.POTIONS.containsKey(EFFECT)) {
            Potion potion = new Potion(false, 0) { };
            potion.setRegistryName(EFFECT);
            ForgeRegistries.POTIONS.register(potion);
        }
        return ForgeRegistries.POTIONS.getValue(EFFECT);
    }

    private static Enchantment ench() {
        if (!ForgeRegistries.ENCHANTMENTS.containsKey(ENCH)) {
            Enchantment enchantment = new Enchantment(Enchantment.Rarity.RARE, EnumEnchantmentType.ALL,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND }
            ) { };
            enchantment.setRegistryName(ENCH);
            ForgeRegistries.ENCHANTMENTS.register(enchantment);
        }
        return ForgeRegistries.ENCHANTMENTS.getValue(ENCH);
    }

    /* The shape level.dat's {@code Registries} tag has: registry name -> Snapshot {ids: name -> saved id}. */
    private static NBTTagCompound registriesTag() {
        NBTTagCompound registries = new NBTTagCompound();
        ForgeRegistry.Snapshot blocks = new ForgeRegistry.Snapshot();
        blocks.ids.put(ORE, ORE_SAVED_ID);
        blocks.ids.put(new ResourceLocation("minecraft", "stone"), Block.getIdFromBlock(Blocks.STONE));
        registries.setTag("minecraft:blocks", blocks.write());

        ForgeRegistry.Snapshot biomes = new ForgeRegistry.Snapshot();
        biomes.ids.put(MARSH, MARSH_SAVED_ID);
        registries.setTag("minecraft:biomes", biomes.write());

        ForgeRegistry.Snapshot potions = new ForgeRegistry.Snapshot();
        potions.ids.put(EFFECT, EFFECT_SAVED_ID);
        potions.ids.put(new ResourceLocation("minecraft", "speed"), Potion.getIdFromPotion(MobEffects.SPEED));
        registries.setTag("minecraft:potions", potions.write());

        ForgeRegistry.Snapshot enchantments = new ForgeRegistry.Snapshot();
        enchantments.ids.put(ENCH, ENCH_SAVED_ID);
        registries.setTag("minecraft:enchantments", enchantments.write());
        return registries;
    }

    @Test
    public void savedBlockIdResolvesByName() {
        ore();
        try {
            LegacyIds.install(registriesTag());
            assertThat(LegacyIds.block(ORE_SAVED_ID)).isSameAs(ore());
            assertThat(LegacyIds.block(Block.getIdFromBlock(Blocks.STONE))).isSameAs(Blocks.STONE);
            // An id the table does not have falls back to the live registry
            assertThat(LegacyIds.block(2)).isSameAs(Block.getBlockById(2));
            assertThat(LegacyIds.block(2)).isSameAs(Blocks.GRASS);
            LegacyIds scoped = new LegacyIds(registriesTag());
            LegacyIds.clear();
            LegacyIds prev = LegacyIds.push(scoped);
            try {
                assertThat(LegacyIds.block(ORE_SAVED_ID)).isSameAs(ore());
            } finally {
                LegacyIds.pop(prev);
            }
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    public void legacyChunkSectionDecodesThroughTheTable() {
        ore();
        try {
            LegacyIds.install(registriesTag());
            byte[] blocks = new byte[4096];
            NibbleArray data = new NibbleArray();
            NibbleArray add = new NibbleArray();
            // saved id 300 = 0x12C: low byte 0x2C in Blocks, the 1 in the Add nibble
            blocks[0] = (byte) (ORE_SAVED_ID & 0xFF);
            add.set(0, 0, 0, ORE_SAVED_ID >> 8);

            BlockStateContainer container = new BlockStateContainer();
            container.setDataFromNBT(blocks, data, add);
            assertThat(container.get(0, 0, 0).getBlock()).isSameAs(ore());
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    public void legacyBiomeBytesDecodeThroughTheTable() throws Exception {
        Biome marsh = marsh();
        try {
            LegacyIds.install(registriesTag());
            byte[] bytes = new byte[256];
            bytes[0] = (byte) MARSH_SAVED_ID;
            assertThat(Biome.REGISTRY.getObject(MARSH)).isSameAs(marsh);
            assertThat(LegacyIds.biome(MARSH_SAVED_ID)).isSameAs(marsh);
            assertThat(Biome.getIdForBiome(marsh) >= 0).isTrue();
            ChunkBiomes biomes = new Chunk(null, 0, 0).getBiomeIds();
            biomes.setLegacy(bytes);
            assertThat(Biome.getBiome(biomes.get(0))).isSameAs(marsh);
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    public void potionEnchantmentBeaconPistonUseTheTable() {
        Potion effect = effect();
        Enchantment ench = ench();
        ore();
        try {
            LegacyIds.install(registriesTag());

            NBTTagCompound potionTag = new NBTTagCompound();
            potionTag.setByte("Id", (byte) EFFECT_SAVED_ID);
            PotionEffect readEffect = PotionEffect.readCustomPotionEffectFromNBT(potionTag);
            assertThat(readEffect.getPotion()).isSameAs(effect);

            NBTTagCompound enchTag = new NBTTagCompound();
            enchTag.setShort("id", (short) ENCH_SAVED_ID);
            enchTag.setShort("lvl", (short) 1);
            assertThat(EnchantmentHelper.enchantmentFrom(enchTag)).isSameAs(ench);

            // The table maps speed's saved id; beaconEffect still validates against VALID_EFFECTS
            NBTTagCompound beaconTag = new NBTTagCompound();
            beaconTag.setInteger("Primary", Potion.getIdFromPotion(MobEffects.SPEED));
            TileEntityBeacon beacon = new TileEntityBeacon();
            beacon.readFromNBT(beaconTag);
            NBTTagCompound out = beacon.writeToNBT(new NBTTagCompound());
            assertThat(out.getString("PrimaryName")).isEqualTo("minecraft:speed");

            NBTTagCompound pistonTag = new NBTTagCompound();
            pistonTag.setInteger("blockId", ORE_SAVED_ID);
            pistonTag.setInteger("blockData", 0);
            TileEntityPiston piston = new TileEntityPiston();
            piston.readFromNBT(pistonTag);
            NBTTagCompound pistonOut = piston.writeToNBT(new NBTTagCompound());
            assertThat(pistonOut.getCompoundTag("blockState").getString("Name")).isEqualTo(ORE.toString());
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    public void withoutATableLiveIdsAreUsed() {
        LegacyIds.clear();
        assertThat(LegacyIds.current()).isNull();
        assertThat(LegacyIds.block(ORE_SAVED_ID)).isSameAs(Block.getBlockById(ORE_SAVED_ID));
        assertThat(LegacyIds.block(2)).isSameAs(Blocks.GRASS);
        LegacyIds scoped = new LegacyIds(registriesTag());
        LegacyIds.clear();
        LegacyIds prev = LegacyIds.push(scoped);
        try {
            assertThat(LegacyIds.block(ORE_SAVED_ID)).isSameAs(ore());
        } finally {
            LegacyIds.pop(prev);
        }
        assertThat(LegacyIds.block(Block.getIdFromBlock(Blocks.STONE))).isSameAs(Blocks.STONE);
    }

    @Test
    public void tableIsPreservedVerbatim() {
        NBTTagCompound installed = registriesTag();
        NBTTagCompound copy = installed.copy();
        try {
            LegacyIds.install(installed);
            assertThat(LegacyIds.registriesForWriting()).isEqualTo(installed);
            // Registering another block does not touch the preserved table
            ResourceLocation extra = new ResourceLocation("wideids_test", "ore_two");
            if (!ForgeRegistries.BLOCKS.containsKey(extra)) {
                ForgeRegistries.BLOCKS.register(new Block(Material.ROCK).setRegistryName(extra));
            }
            assertThat(LegacyIds.registriesForWriting()).isEqualTo(copy);
        } finally {
            LegacyIds.clear();
        }
    }

    @Test
    public void missingLegacyBlocksAndItemsDoNotThrowOnRead() {
        ResourceLocation diskName = new ResourceLocation("wideids_test", "legacy_disc");
        if (!ForgeRegistries.ITEMS.containsKey(diskName)) {
            ForgeRegistries.ITEMS.register(new net.minecraft.item.Item().setRegistryName(diskName));
        }
        net.minecraft.item.Item disk = ForgeRegistries.ITEMS.getValue(diskName);
        NBTTagCompound tag = registriesTag();
        ForgeRegistry.Snapshot items = new ForgeRegistry.Snapshot();
        items.ids.put(diskName, 4444);
        tag.setTag("minecraft:items", items.write());

        try {
            LegacyIds.install(tag);
            assertThat(LegacyIds.item(4444)).isSameAs(disk);

            net.minecraft.block.BlockJukebox.TileEntityJukebox jukebox = new net.minecraft.block.BlockJukebox.TileEntityJukebox();
            NBTTagCompound jukeboxTag = new NBTTagCompound();
            jukeboxTag.setInteger("Record", 4444);
            jukebox.readFromNBT(jukeboxTag);
            assertThat(jukebox.getRecord().getItem()).isSameAs(disk);

            net.minecraft.tileentity.TileEntityFlowerPot pot = new net.minecraft.tileentity.TileEntityFlowerPot();
            NBTTagCompound potTag = new NBTTagCompound();
            potTag.setInteger("Item", 4444);
            pot.readFromNBT(potTag);
            assertThat(pot.getFlowerPotItem()).isSameAs(disk);

            TileEntityPiston missingPiston = new TileEntityPiston();
            NBTTagCompound badPistonTag = new NBTTagCompound();
            badPistonTag.setInteger("blockId", 999999);
            badPistonTag.setInteger("blockData", 0);
            missingPiston.readFromNBT(badPistonTag);
            assertThat(missingPiston.getPistonState()).isSameAs(Blocks.AIR.getDefaultState());
        } finally {
            LegacyIds.clear();
        }
    }

}
