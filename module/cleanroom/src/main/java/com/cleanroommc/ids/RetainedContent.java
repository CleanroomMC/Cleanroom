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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The names a chunk holds that no registry resolved, kept verbatim so a load-save cycle while a mod is
 * absent loses nothing: block-state strings per section cell, biome names per column (on
 * {@link ChunkBiomes}), and whole tile-entity and entity compounds. A real write to a cell drops its name
 * (last write wins); everything else goes back out exactly as it came in and returns to life when the mod
 * is reinstalled.
 * <p>
 * TODO items: an ItemStack with an unknown item is EMPTY everywhere vanilla looks, so retaining item NBT
 * needs a separate design and is not done here.
 */
public final class RetainedContent {

    private static final Logger LOGGER = LogManager.getLogger("Cleanroom");
    static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();

    /* A retained cell: the saved state string and the vanilla state it reads as meanwhile (null = air). */
    public record Retained(String name, @Nullable IBlockState standIn) { }

    /* A section's cells still holding a name no block answered: storage index (y << 8 | z << 4 | x) -> the saved state string and its stand-in. */
    /* A section's cells still holding a name no block answered: storage index (y &lt;&lt; 8 | z &lt;&lt; 4 | x) -> the saved state string and its stand-in. */
    public static final class Section {

        private final Int2ObjectMap<Retained> cells = new Int2ObjectOpenHashMap<>();

        public void retain(int index, String name) {
            retain(index, name, null);
        }

        public void retain(int index, String name, @Nullable IBlockState standIn) {
            cells.put(index, new Retained(name, standIn));
        }

        public void clear(int index) {
            cells.remove(index);
        }

        @Nullable
        public String name(int index) {
            Retained cell = cells.get(index);
            return cell == null ? null : cell.name();
        }

        /* The stand-in the cell was retained with, or null when it reads as air (or the cell is not retained). */
        @Nullable
        public IBlockState standIn(int index) {
            Retained cell = cells.get(index);
            return cell == null ? null : cell.standIn();
        }

        /* The state the cell reads as while retained: the stand-in, or air. */
        public IBlockState reads(int index) {
            IBlockState standIn = standIn(index);
            return standIn == null ? Blocks.AIR.getDefaultState() : standIn;
        }

        public boolean isEmpty() {
            return cells.isEmpty();
        }

        public Int2ObjectMap<Retained> cells() {
            return cells;
        }

    }

    /* Level-compound key marking a chunk generated while the world's saved generator type was a missing mod's. */
    private static final String GENERATED_WITHOUT_WORLD_TYPE = "GeneratedWithoutWorldType";

    private final Int2ObjectMap<Section> sections = new Int2ObjectOpenHashMap<>();
    /* Tile-entity compounds that named no registered block entity, kept verbatim. */
    public final List<NBTTagCompound> tileEntities = new ArrayList<>();
    /* Entity compounds that named no registered entity, kept verbatim (riders included). */
    public final List<NBTTagCompound> entities = new ArrayList<>();
    /* Scheduled block-tick compounds that named no registered block, kept verbatim. */
    public final List<NBTTagCompound> tileTicks = new ArrayList<>();

    /*
     * The world type this chunk's terrain was generated without, when the saved generator name named a
     * missing mod's type: the terrain is vanilla stand-in, and a later phase can offer to regenerate it.
     * Other dimensions share the overworld's WorldInfo, so a non-overworld chunk can carry the mark even
     * though its generator ignored the type; a later prompt filters by dimension.
     */
    @Nullable private String generatedWithoutWorldType;

    @Nullable
    public String getGeneratedWithoutWorldType() {
        return generatedWithoutWorldType;
    }

    public void setGeneratedWithoutWorldType(@Nullable String name) {
        generatedWithoutWorldType = name;
    }

    public void readLevelTags(NBTTagCompound level) {
        generatedWithoutWorldType = level.hasKey(GENERATED_WITHOUT_WORLD_TYPE, 8)
            ? level.getString(GENERATED_WITHOUT_WORLD_TYPE)
            : null;
    }

    public void writeLevelTags(NBTTagCompound level) {
        if (generatedWithoutWorldType != null) {
            level.setString(GENERATED_WITHOUT_WORLD_TYPE, generatedWithoutWorldType);
        }
    }

    public Section section(int y) {
        return sections.computeIfAbsent(y, k -> new Section());
    }

    @Nullable
    public Section sectionOrNull(int y) {
        return sections.get(y);
    }

    public void section(int y, @Nullable Section section) {
        if (section == null || section.isEmpty()) {
            sections.remove(y);
        } else {
            sections.put(y, section);
        }
    }

    /* Once per session per name, so a removed mod logs one line instead of one per chunk or stack. */
    public static void noteMissing(String name, @Nullable ChunkPos pos) {
        if (LOGGED.add(name)) {
            if (pos == null) {
                LOGGER.info("Retaining content of {} (not registered). Reinstall the mod to restore it.", name);
            } else {
                LOGGER.info("Retaining content of {} (not registered; first seen in chunk {},{}). Reinstall the mod to restore it.",
                    name, pos.x, pos.z
                );
            }
        }
    }

    /* Once per session per item name; the stack is kept as {@code cleanroom:missing_item} carrying its original compound. */
    public static void noteMissingItem(String name) {
        if (LOGGED.add("item " + name)) {
            LOGGER.info("Retaining item {} (not registered; kept as a missing-item stack). Reinstall the mod to restore it.",
                name
            );
        }
    }

    /*
     * Once per session per block (not per state string -> a vine has many connection states), with the
     * stand-in the retained cells read as in the message.
     */
    public static void noteMissingBlock(String stateName, @Nullable IBlockState standIn, @Nullable ChunkPos pos) {
        int open = stateName.indexOf('[');
        String block = open < 0 ? stateName : stateName.substring(0, open);
        if (LOGGED.add("block " + block)) {
            String how = standIn == null
                ? "reads as air"
                : "standing in: " + Block.REGISTRY.getNameForObject(standIn.getBlock());
            if (pos == null) {
                LOGGER.info("Retaining content of {} (not registered; {}). Reinstall the mod to restore it.", block, how
                );
            } else {
                LOGGER.info("Retaining content of {} (not registered; {}; first seen in chunk {},{}). Reinstall the mod to restore it.",
                    block, how, pos.x, pos.z
                );
            }
        }
    }

    /* Once per session per biome name, naming the vanilla biome the column reads as meanwhile. */
    public static void noteMissingBiome(String name, net.minecraft.world.biome.@Nullable Biome standIn,
        @Nullable ChunkPos pos
    ) {
        if (LOGGED.add("biome " + name)) {
            String how = standIn == null
                ? "reads as the world's provider"
                : "reads as " + net.minecraft.world.biome.Biome.REGISTRY.getNameForObject(standIn);
            if (pos == null) {
                LOGGER.info("Retaining content of {} (not registered; {}). Reinstall the mod to restore it.", name, how
                );
            } else {
                LOGGER.info("Retaining content of {} (not registered; {}; first seen in chunk {},{}). Reinstall the mod to restore it.",
                    name, how, pos.x, pos.z
                );
            }
        }
    }

    private static boolean standInTerrainNoted;

    /* Once per session, the first chunk generated while the world's real generator type was missing. */
    public static void noteStandInTerrain(ChunkPos pos, String worldType) {
        if (!standInTerrainNoted) {
            standInTerrainNoted = true;
            LOGGER.info("Chunk {},{} generated while world type {} was missing (vanilla terrain); it can be regenerated when the mod returns.",
                pos.x, pos.z, worldType
            );
        }
    }

    /* Namespaces the user chose to drop at this world's Retain/Remove prompt (the {@code Decisions} read "remove"). */
    private static final Set<String> REMOVED_NAMESPACES = ConcurrentHashMap.newKeySet();

    /* Per-world Retain/Remove answers keyed by namespace, written back to {@code cleanroom_retained.dat} on save. */
    private static final Map<String, String> RETENTION_DECISIONS = new ConcurrentHashMap<>();

    /* True while this session drops {@code name}'s namespace on read instead of retaining it. Accepts a raw name or a state string ({@code mod:block[props]}). */
    public static boolean isRemoved(String name) {
        int colon = name.indexOf(':');
        return colon > 0 && REMOVED_NAMESPACES.contains(name.substring(0, colon));
    }

    public static Set<String> removedNamespaces() {
        return REMOVED_NAMESPACES;
    }

    public static Map<String, String> retentionDecisions() {
        return RETENTION_DECISIONS;
    }

    /*
     * Session index of chunks carrying the stand-in-terrain mark: world type name -> dimension id
     * -> chunk positions ({@link ChunkPos#asLong}). AnvilChunkLoader's write path maintains it, and
     * cleanroom_retained.dat persists it, so world load knows the marked count without scanning region
     * files. Worlds last saved before this index existed carry marks in chunks but no index entries;
     * they are indexed as their chunks re-save.
     */
    private static final Map<String, Map<Integer, LongSet>> STAND_IN_TERRAIN = new ConcurrentHashMap<>();

    public static Map<String, Map<Integer, LongSet>> standInTerrain() {
        return STAND_IN_TERRAIN;
    }

    /* Chunk write: index the mark, or drop this position from every type when it was written unmarked. */
    public static void indexStandInChunk(@Nullable String worldType, int dimension, ChunkPos pos) {
        long key = ChunkPos.asLong(pos.x, pos.z);
        if (worldType != null) {
            STAND_IN_TERRAIN.computeIfAbsent(worldType, t -> new ConcurrentHashMap<>())
                .computeIfAbsent(dimension, d -> LongSets.synchronize(new LongOpenHashSet()))
                .add(key);
        } else {
            for (Map<Integer, LongSet> dims : STAND_IN_TERRAIN.values()) {
                for (LongSet chunks : dims.values()) {
                    chunks.remove(key);
                }
            }
        }
    }

    /* Once per session per name: the user's "Remove" answer deleting this content as it is read. */
    public static void noteRemoved(String name) {
        if (LOGGED.add("removed " + name)) {
            LOGGER.info("Removing content of {} (chosen on load)", name);
        }
    }

    /* World closed: the next world this session opens gets its own file, decisions and per-name logging. */
    public static void clearWorldState() {
        RetentionFile.clearSessionState();
        REMOVED_NAMESPACES.clear();
        RETENTION_DECISIONS.clear();
        STAND_IN_TERRAIN.clear();
        resetLogged();
    }

    public static void resetLogged() {
        LOGGED.clear();
        standInTerrainNoted = false;
    }

    /* The compound names no registered tile entity; read failures of known ids are not retainable. */
    public static boolean unknownTileEntity(NBTTagCompound tag) {
        return !TileEntity.isRegistered(tag.getString("id"));
    }

    /* The compound names no registered entity. */
    public static boolean unknownEntity(NBTTagCompound tag) {
        return EntityList.getClass(new ResourceLocation(tag.getString("id"))) == null;
    }

    /*
     * Centralized helper for reading a list of PotionEffects (ActiveEffects, Effects, CustomPotionEffects)
     * while retaining any entries whose Potion belongs to a temporarily absent mod (and was not removed).
     */
    @Nullable
    public static List<NBTTagCompound> readPotionEffects(NBTTagCompound compound, String key,
        java.util.function.Consumer<net.minecraft.potion.PotionEffect> liveConsumer
    ) {
        if (!compound.hasKey(key, 9)) {
            return null;
        }
        net.minecraft.nbt.NBTTagList list = compound.getTagList(key, 10);
        List<NBTTagCompound> retained = null;
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound effectTag = list.getCompoundTagAt(i);
            net.minecraft.potion.PotionEffect effect = net.minecraft.potion.PotionEffect.readCustomPotionEffectFromNBT(effectTag
            );
            if (effect != null) {
                liveConsumer.accept(effect);
            } else {
                String potName = effectTag.hasKey("Name", 8) ? effectTag.getString("Name") : null;
                if ((potName == null || potName.isEmpty()) && effectTag.hasKey("Id", 99)) {
                    int rawId = effectTag.getTagId("Id") == 1
                        ? (effectTag.getByte("Id") & 0xFF)
                        : effectTag.getInteger("Id");
                    ResourceLocation leg = LegacyIds.potionName(rawId);
                    potName = leg == null ? null : leg.toString();
                }
                if (potName != null && !potName.isEmpty() && !isRemoved(potName)) {
                    if (retained == null) {
                        retained = new ArrayList<>();
                    }
                    NBTTagCompound copy = effectTag.copy();
                    copy.setString("Name", potName);
                    retained.add(copy);
                }
            }
        }
        return retained;
    }

    /*
     * Centralized helper for writing live PotionEffects alongside any retained missing-mod effect tags.
     */
    public static void writePotionEffects(NBTTagCompound compound, String key,
        java.util.Collection<net.minecraft.potion.PotionEffect> liveEffects,
        @Nullable List<NBTTagCompound> retainedEffects
    ) {
        if (liveEffects.isEmpty() && (retainedEffects == null || retainedEffects.isEmpty())) {
            return;
        }
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        for (net.minecraft.potion.PotionEffect effect : liveEffects) {
            list.appendTag(effect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
        }
        if (retainedEffects != null) {
            for (NBTTagCompound retainedTag : retainedEffects) {
                list.appendTag(retainedTag.copy());
            }
        }
        compound.setTag(key, list);
    }

    /*
     * Centralized helper for reading a PotionType key ("Potion") and returning the raw string to retain
     * when the PotionType is unregistered and not removed.
     */
    @Nullable
    public static String readRetainedPotionType(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 8)) {
            return null;
        }
        String name = compound.getString(key);
        if (!name.isEmpty() &&
            !net.minecraft.potion.PotionType.REGISTRY.containsKey(new ResourceLocation(name)) &&
            !isRemoved(name)) {
            return name;
        }
        return null;
    }

    /*
     * Centralized helper for reading an entity/tile-entity block-state compound (or legacy block string/id)
     * and returning a synthetic or copied state tag when the referenced block is currently absent and not removed.
     */
    @Nullable
    public static NBTTagCompound readRetainedBlockState(NBTTagCompound compound, String stateKey,
        @Nullable String legacyNameKey, @Nullable String legacyIdKey, boolean idIsShort, boolean idIsByte
    ) {
        if (compound.hasKey(stateKey, 10)) {
            NBTTagCompound stateTag = compound.getCompoundTag(stateKey);
            String blockName = stateTag.getString("Name");
            if (!blockName.isEmpty() && !Block.REGISTRY.containsKey(new ResourceLocation(blockName))) {
                if (isRemoved(blockName)) {
                    noteRemoved(blockName);
                    return null;
                }
                return stateTag.copy();
            }
            return null;
        }
        String legacyName = null;
        if (legacyNameKey != null && compound.hasKey(legacyNameKey, 8)) {
            legacyName = compound.getString(legacyNameKey);
        } else if (legacyIdKey != null && compound.hasKey(legacyIdKey, 99)) {
            int legId = idIsShort
                ? (compound.getShort(legacyIdKey) & 0xFFFF)
                : (idIsByte ? (compound.getByte(legacyIdKey) & 0xFF) : compound.getInteger(legacyIdKey));
            ResourceLocation legRl = LegacyIds.blockName(legId);
            if (legRl != null && !Block.REGISTRY.containsKey(legRl)) {
                legacyName = legRl.toString();
            }
        }
        if (legacyName != null &&
            !legacyName.isEmpty() &&
            !Block.REGISTRY.containsKey(new ResourceLocation(legacyName))) {
            if (isRemoved(legacyName)) {
                noteRemoved(legacyName);
                return null;
            }
            NBTTagCompound synth = new NBTTagCompound();
            synth.setString("Name", legacyName);
            return synth;
        }
        return null;
    }

    public record DyeRead(net.minecraft.item.EnumDyeColor color, @Nullable String retainedName, int retainedNumber) { }

    /*
     * Centralized helper for reading a named/numbered EnumDyeColor (Bed, Banner, Sheep, Wolf)
     * and retaining custom dye colors from temporarily absent mods.
     */
    @SuppressWarnings("deprecation")
    public static DyeRead readDyeColor(NBTTagCompound compound, String nameKey, String numberKey, boolean byDyeDamage,
        net.minecraft.item.EnumDyeColor fallbackWhenMissing
    ) {
        int rawNumber = compound.hasKey(numberKey, 99)
            ? compound.getInteger(numberKey)
            : (byDyeDamage ? fallbackWhenMissing.getDyeDamage() : fallbackWhenMissing.getMetadata());
        if (compound.hasKey(nameKey, 8)) {
            String cName = compound.getString(nameKey);
            for (net.minecraft.item.EnumDyeColor c : net.minecraft.item.EnumDyeColor.values()) {
                if (c.getName().equals(cName)) {
                    return new DyeRead(c, null, -1);
                }
            }
            if (!cName.isEmpty() && !isRemoved(cName)) {
                return new DyeRead(fallbackWhenMissing, cName, rawNumber);
            }
        }
        net.minecraft.item.EnumDyeColor resolved = byDyeDamage
            ? net.minecraft.item.EnumDyeColor.byDyeDamage(rawNumber)
            : net.minecraft.item.EnumDyeColor.byMetadata(rawNumber);
        int resolvedNum = byDyeDamage ? resolved.getDyeDamage() : resolved.getMetadata();
        if (resolvedNum != rawNumber && rawNumber >= 16) {
            return new DyeRead(resolved, null, rawNumber);
        }
        return new DyeRead(resolved, null, -1);
    }

    @SuppressWarnings("deprecation")
    public static void writeDyeColor(NBTTagCompound compound, String nameKey, String numberKey, boolean byDyeDamage,
        boolean byteForSmall, net.minecraft.item.EnumDyeColor liveColor, @Nullable String retainedName,
        int retainedNumber
    ) {
        int num = (retainedName != null || retainedNumber >= 16) && retainedNumber >= 0
            ? retainedNumber
            : (byDyeDamage ? liveColor.getDyeDamage() : liveColor.getMetadata());
        if (byteForSmall && num >= 0 && num <= Byte.MAX_VALUE) {
            compound.setByte(numberKey, (byte) num);
        } else {
            compound.setInteger(numberKey, num);
        }
        if (retainedName != null) {
            compound.setString(nameKey, retainedName);
        } else if (retainedNumber < 16) {
            compound.setString(nameKey, liveColor.getName());
        }
    }

}
