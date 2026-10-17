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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;

import java.util.HashMap;
import java.util.Map;

/**
 * The per-world id table recorded in level.dat ({@code FML}/{@code Registries}). Live registry ids are
 * derived each session and differ from the numbers a Forge-era save used, so every numeric id read out of
 * an old world's NBT must go through this table: saved id -> saved name -> live object. Vanilla entries
 * keep their vanilla ids and resolve identically either way.
 *
 * <p>One world is open per process in 1.12 (all dimensions share level.dat), so a single static instance
 * is correct. Installed from {@code FMLContainer.readData} before {@code GameData.loadWorldSnapshot}, cleared
 * when the server stops so the next world opened in this session does not inherit it.</p>
 */
public final class LegacyIds {

    private static final ScopedValue<LegacyIds> SCOPED = ScopedValue.newInstance();
    private static final ThreadLocal<LegacyIds> THREAD_SCOPED = new ThreadLocal<>();
    private static final java.util.Set<
        String> WARNED_NUMERIC_BLOCK_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final java.util.Set<
        String> WARNED_NUMERIC_ITEM_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Nullable private static volatile LegacyIds current;

    /* The {@code Registries} tag this table was built from, verbatim, for re-writing on save. */
    private final NBTTagCompound registries;
    /* Registry name -> saved id -> saved name. */
    private final Map<ResourceLocation, Int2ObjectMap<ResourceLocation>> tables = new HashMap<>();

    public LegacyIds(NBTTagCompound registries) {
        this.registries = registries;
        for (String key : registries.getKeySet()) {
            ForgeRegistry.Snapshot snapshot = ForgeRegistry.Snapshot.read(registries.getCompoundTag(key));
            if (snapshot.ids.isEmpty()) {
                continue;
            }
            Int2ObjectMap<ResourceLocation> table = new Int2ObjectOpenHashMap<>(snapshot.ids.size());
            snapshot.ids.forEach((name, id) -> table.put((int) id, name));
            this.tables.put(new ResourceLocation(key), table);
        }
    }

    public static void install(NBTTagCompound registriesTag) {
        LegacyIds table = new LegacyIds(registriesTag);
        current = table;
        int entries = table.tables.values().stream().mapToInt(Int2ObjectMap::size).sum();
        LogManager.getLogger("Cleanroom")
            .info("LegacyIds: saved id table installed for {} registries ({} entries)", table.tables.size(), entries);
    }

    public static void clear() {
        current = null;
        LogManager.getLogger("Cleanroom").debug("LegacyIds: saved id table cleared");
    }

    @Nullable
    public static LegacyIds current() {
        if (SCOPED.isBound()) {
            return SCOPED.get();
        }
        LegacyIds local = THREAD_SCOPED.get();
        return local != null ? local : current;
    }

    @Nullable
    public static LegacyIds push(@Nullable LegacyIds table) {
        LegacyIds prev = THREAD_SCOPED.get();
        if (table == null) {
            THREAD_SCOPED.remove();
        } else {
            THREAD_SCOPED.set(table);
        }
        return prev;
    }

    public static void pop(@Nullable LegacyIds prev) {
        if (prev == null) {
            THREAD_SCOPED.remove();
        } else {
            THREAD_SCOPED.set(prev);
        }
    }

    /*
     * Binds {@code table} for the duration of {@code op} on the current thread (used by
     * {@code AnvilChunkLoader} so chunk and entity loads use the save's own table even on worker threads).
     */
    public static <T> T callWith(@Nullable LegacyIds table, java.util.function.Supplier<T> op) {
        if (table == null) {
            return op.get();
        }
        return ScopedValue.where(SCOPED, table).call(op::get);
    }

    public static void runWith(@Nullable LegacyIds table, Runnable op) {
        if (table == null) {
            op.run();
            return;
        }
        ScopedValue.where(SCOPED, table).run(op);
    }

    /*
     * The verbatim {@code Registries} tag for writing back to level.dat, or null when no table was
     * installed. The saved table is preserved, never regenerated from live ids.
     */
    @Nullable
    public static NBTTagCompound registriesForWriting() {
        LegacyIds table = current();
        return table == null ? null : table.registries;
    }

    /* A saved block id (chunk sections, piston TEs, endermen). Falls back to the live id. */
    @Nullable
    public static Block block(int savedId) {
        LegacyIds table = current();
        if (table != null) {
            ResourceLocation name = table.lookup(GameData.BLOCKS, savedId);
            if (name != null) {
                return Block.REGISTRY.getObject(name);
            }
        }
        return Block.getBlockById(savedId);
    }

    /* The saved registry name for a legacy block id in the world's table, or null when not in the table. */
    @Nullable
    public static ResourceLocation blockName(int savedId) {
        LegacyIds table = current();
        return table == null ? null : table.lookup(GameData.BLOCKS, savedId);
    }

    /* A saved item id (jukeboxes, flower pots). Falls back to the live id. */
    @Nullable
    public static Item item(int savedId) {
        LegacyIds table = current();
        if (table != null) {
            ResourceLocation name = table.lookup(GameData.ITEMS, savedId);
            if (name != null) {
                return Item.REGISTRY.getObject(name);
            }
        }
        return Item.getItemById(savedId);
    }

    /* The saved registry name for a legacy item id in the world's table, or null when not in the table. */
    @Nullable
    public static ResourceLocation itemName(int savedId) {
        LegacyIds table = current();
        return table == null ? null : table.lookup(GameData.ITEMS, savedId);
    }

    /* A saved biome id (legacy biome bytes, JEID int arrays). Falls back to the live id. */
    @Nullable
    public static Biome biome(int savedId) {
        LegacyIds table = current();
        if (table != null) {
            ResourceLocation name = table.lookup(GameData.BIOMES, savedId);
            if (name != null) {
                return Biome.REGISTRY.getObject(name);
            }
        }
        return Biome.getBiome(savedId);
    }

    /* The saved registry name for a legacy biome id in the world's table, or null when not in the table. */
    @Nullable
    public static ResourceLocation biomeName(int savedId) {
        LegacyIds table = current();
        return table == null ? null : table.lookup(GameData.BIOMES, savedId);
    }

    /* A saved potion id (PotionEffect, beacons). Falls back to the live id. */
    @Nullable
    public static Potion potion(int savedId) {
        LegacyIds table = current();
        if (table != null) {
            ResourceLocation name = table.lookup(GameData.POTIONS, savedId);
            if (name != null) {
                return Potion.REGISTRY.getObject(name);
            }
        }
        return Potion.getPotionById(savedId);
    }

    /* A saved enchantment id. Falls back to the live id. */
    @Nullable
    public static Enchantment enchantment(int savedId) {
        LegacyIds table = current();
        if (table != null) {
            ResourceLocation name = table.lookup(GameData.ENCHANTMENTS, savedId);
            if (name != null) {
                return Enchantment.REGISTRY.getObject(name);
            }
        }
        return Enchantment.getEnchantmentByID(savedId);
    }

    public static void writePotionName(NBTTagCompound compound, String key, @Nullable Potion potion) {
        if (potion != null) {
            ResourceLocation name = Potion.REGISTRY.getNameForObject(potion);
            if (name != null) {
                compound.setString(key, name.toString());
            }
        }
    }

    @Nullable
    public static Potion beaconEffect(NBTTagCompound compound, String nameKey, String idKey,
        java.util.Set<Potion> validEffects
    ) {
        Potion potion = compound.hasKey(nameKey, 8)
            ? Potion.REGISTRY.getObject(new ResourceLocation(compound.getString(nameKey)))
            : potion(compound.getInteger(idKey));
        return validEffects.contains(potion) ? potion : null;
    }

    public static void warnIfNumericBlockId(String name) {
        if (isNumericId(name) && WARNED_NUMERIC_BLOCK_IDS.add(name)) {
            net.minecraftforge.fml.common.FMLLog.log.warn("Numeric block ID '{}' is not supported; use a registry name instead",
                name
            );
        }
    }

    public static void warnIfNumericItemId(String id) {
        if (isNumericId(id) && WARNED_NUMERIC_ITEM_IDS.add(id)) {
            net.minecraftforge.fml.common.FMLLog.log.warn("Numeric item ID '{}' is not supported; use a registry name instead",
                id
            );
        }
    }

    private static boolean isNumericId(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        int start = s.charAt(0) == '-' || s.charAt(0) == '+' ? 1 : 0;
        if (start == s.length()) {
            return false;
        }
        for (int i = start; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private ResourceLocation lookup(ResourceLocation registry, int savedId) {
        Int2ObjectMap<ResourceLocation> table = tables.get(registry);
        return table == null ? null : table.get(savedId);
    }

}
