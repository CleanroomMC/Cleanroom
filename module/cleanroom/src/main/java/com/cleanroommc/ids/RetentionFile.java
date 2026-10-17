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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.ZipperUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * {@code cleanroom_retained.dat} beside level.dat: the record of what a world contained at last save, so a
 * later load can say which mods' content is missing and ask whether to retain it or delete it.
 * <p>
 * Layout (gzip NBT): {@code Format} int (1); {@code Registries} compound mapping blocks/items/biomes/
 * entities/tileentities to the registered names at last save, {@code minecraft:} excluded since vanilla
 * cannot go missing; {@code Mods} compound mapping modid to "Name version" so the prompt can name a mod
 * that is no longer installed; {@code Decisions} compound mapping namespace to "retain" or "remove" for
 * answers already given; {@code Generator} the generator name level.dat saved.
 * <p>
 * An absent or unparseable file means "no information": the world loads normally and the file is written
 * at the next save. It never refuses a world.
 */
public final class RetentionFile {

    public static final String FILE_NAME = "cleanroom_retained.dat";
    public static final String RETAIN = "retain";
    public static final String REMOVE = "remove";
    private static final int FORMAT = 1;
    private static final Logger LOGGER = LogManager.getLogger("Cleanroom");

    private static final List<Census> CENSUSES = new ArrayList<>();
    private static final Map<String, String> LABELS = new LinkedHashMap<>(); // census key -> plural label for the prompt

    private static final Map<String, String> SINGULAR = new LinkedHashMap<>();

    static {
        SINGULAR.put("blocks", "block");
        SINGULAR.put("items", "item");
        SINGULAR.put("biomes", "biome");
        SINGULAR.put("entities", "entity");
        SINGULAR.put("tileentities", "tile entity");
        LABELS.put("blocks", "blocks");
        LABELS.put("items", "items");
        LABELS.put("biomes", "biomes");
        LABELS.put("entities", "entities");
        LABELS.put("tileentities", "tile entities");
    }

    /* census key (e.g. {@code blocks}) -> registered modded names at last save */
    public final Map<String, List<String>> registries = new TreeMap<>();
    /* modid -> "Name version" at last save */
    public final Map<String, String> mods = new TreeMap<>();
    /* namespace -> "retain" | "remove" */
    public final Map<String, String> decisions = new TreeMap<>();
    /* world type name -> dimension id -> marked stand-in-terrain chunk positions ({@link ChunkPos#asLong}) */
    public final Map<String, Map<Integer, LongSet>> standInTerrain = new TreeMap<>();

    /* the generator name level.dat saved last time */
    @Nullable public String generator;

    RetentionFile() { }

    // Lazy: loading this class must not touch ForgeRegistries (IdFormatContainer references it from
    // getDataForWriting, which also runs in un-bootstrapped unit tests).
    private static synchronized List<Census> censuses() {
        if (CENSUSES.isEmpty()) {
            CENSUSES.add(new Census("blocks", () -> forgeNames(ForgeRegistries.BLOCKS),
                presentIn(ForgeRegistries.BLOCKS)
            ));
            CENSUSES.add(new Census("items", () -> forgeNames(ForgeRegistries.ITEMS), presentIn(ForgeRegistries.ITEMS)
            ));
            CENSUSES.add(new Census("biomes", () -> forgeNames(ForgeRegistries.BIOMES),
                presentIn(ForgeRegistries.BIOMES)
            ));
            CENSUSES.add(new Census("entities", () -> forgeNames(ForgeRegistries.ENTITIES),
                presentIn(ForgeRegistries.ENTITIES)
            ));
            CENSUSES.add(new Census("tileentities",
                () -> {
                    List<String> names = new ArrayList<>();
                    for (ResourceLocation name : TileEntity.getRegisteredKeys()) {
                        if (!"minecraft".equals(name.getNamespace()) && !"cleanroom".equals(name.getNamespace())) {
                            names.add(name.toString());
                        }
                    }
                    Collections.sort(names);
                    return names;
                },
                TileEntity::isRegistered
            ));
        }
        return CENSUSES;
    }

    private static List<String> forgeNames(net.minecraftforge.registries.IForgeRegistry<?> registry) {
        List<String> names = new ArrayList<>();
        for (ResourceLocation name : registry.getKeys()) {
            if (!"minecraft".equals(name.getNamespace()) && !"cleanroom".equals(name.getNamespace())) {
                names.add(name.toString());
            }
        }
        Collections.sort(names);
        return names;
    }

    // IForgeRegistry.containsKey is authoritative; getValue is wrong here because ForgeRegistry is defaulted (missing name -> air default, not null)
    private static Predicate<String> presentIn(net.minecraftforge.registries.IForgeRegistry<?> registry) {
        return name -> registry.containsKey(new ResourceLocation(name));
    }

    /* The file from a world directory, or null when absent/unparseable ("no information"). */
    @Nullable
    public static RetentionFile read(File worldDir) {
        File file = new File(worldDir, FILE_NAME);
        if (!file.isFile()) {
            return null;
        }
        NBTTagCompound tag;
        try {
            tag = CompressedStreamTools.read(file);
        } catch (Exception e) {
            LOGGER.warn("Could not read {}: {}; proceeding without retention information", file.getName(), e.toString()
            );
            return null;
        }
        if (tag == null) {
            return null;
        }
        RetentionFile ret = new RetentionFile();
        NBTTagCompound regs = tag.getCompoundTag("Registries");
        for (String key : regs.getKeySet()) {
            NBTTagList list = regs.getTagList(key, 8);
            List<String> names = new ArrayList<>(list.tagCount());
            for (int i = 0; i < list.tagCount(); i++) {
                names.add(list.getStringTagAt(i));
            }
            ret.registries.put(key, names);
        }
        NBTTagCompound mods = tag.getCompoundTag("Mods");
        for (String key : mods.getKeySet()) {
            ret.mods.put(key, mods.getString(key));
        }
        NBTTagCompound decisions = tag.getCompoundTag("Decisions");
        for (String key : decisions.getKeySet()) {
            ret.decisions.put(key, decisions.getString(key));
        }
        ret.generator = tag.hasKey("Generator", 8) ? tag.getString("Generator") : null;
        NBTTagCompound terrain = tag.getCompoundTag("StandInTerrain");
        for (String type : terrain.getKeySet()) {
            NBTTagCompound dims = terrain.getCompoundTag(type);
            Map<Integer, LongSet> byDim = new TreeMap<>();
            for (String dim : dims.getKeySet()) {
                if (dims.getTag(dim) instanceof NBTTagLongArray longs) {
                    byDim.put(Integer.parseInt(dim), new LongOpenHashSet(longs.getLongArray()));
                }
            }
            ret.standInTerrain.put(type, byDim);
        }
        return ret;
    }

    private static final Map<String, List<String>> RETAINED_CENSUS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, String> RETAINED_MODS = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearSessionState() {
        RETAINED_CENSUS.clear();
        RETAINED_MODS.clear();
    }

    /* The current world as the file should record it: registries, active mods, decisions kept from load. */
    public static RetentionFile capture(WorldInfo info) {
        RetentionFile ret = new RetentionFile();
        for (Census census : censuses()) {
            java.util.TreeSet<String> merged = new java.util.TreeSet<>(census.registered.get());
            List<String> carried = RETAINED_CENSUS.get(census.key);
            if (carried != null) {
                for (String name : carried) {
                    if (!census.present.test(name)) {
                        merged.add(name);
                    }
                }
            }
            ret.registries.put(census.key, new ArrayList<>(merged));
        }
        ret.mods.putAll(RETAINED_MODS);
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            ret.mods.put(mod.getModId(), mod.getName() + " " + mod.getVersion());
        }
        ret.decisions.putAll(RetainedContent.retentionDecisions());
        RetainedContent.standInTerrain().forEach((type, dims) -> {
            Map<Integer, LongSet> copy = new TreeMap<>();
            dims.forEach((dim, chunks) -> {
                synchronized (chunks) {
                    copy.put(dim, new LongOpenHashSet(chunks));
                }
            });
            ret.standInTerrain.put(type, copy);
        });
        String retained = info.getRetainedGeneratorName();
        ret.generator = retained != null ? retained : info.getTerrainType().getName();
        return ret;
    }

    /* The namespace a missing world type most plausibly belongs to (its name sits inside it or vice versa). */
    @Nullable
    private static String namespaceForWorldType(String generator, Iterable<String> namespaces) {
        String lower = generator.toLowerCase();
        for (String ns : namespaces) {
            if (ns.equalsIgnoreCase(generator) || lower.contains(ns) || ns.contains(lower)) {
                return ns;
            }
        }
        return null;
    }

    /*
     * World load: diff the saved census against the live registries, apply remembered decisions, ask the
     * Retain/Remove question for namespaces with missing content and no decision, and record the answer in
     * {@link RetainedContent#retentionDecisions()} / {@link RetainedContent#removedNamespaces()}. A mod
     * that came back has its decision dropped, so removing it later asks again.
     */
    public static void onWorldLoad(File worldDir, WorldInfo info) {
        RetainedContent.clearWorldState();
        RetentionFile file = read(worldDir);
        if (file == null) {
            return;
        }
        Map<String, Map<String, Integer>> missing = file.missingByNamespace();
        for (Census census : censuses()) {
            List<String> saved = file.registries.get(census.key);
            if (saved != null) {
                List<String> stillMissing = new ArrayList<>();
                for (String name : saved) {
                    if (!census.present.test(name)) {
                        stillMissing.add(name);
                    }
                }
                if (!stillMissing.isEmpty()) {
                    RETAINED_CENSUS.put(census.key, stillMissing);
                }
            }
        }
        for (String ns : missing.keySet()) {
            String modDisplay = file.mods.get(ns);
            if (modDisplay != null) {
                RETAINED_MODS.put(ns, modDisplay);
            }
        }
        String worldType = info.getRetainedGeneratorName();
        String worldTypeNamespace = worldType == null ? null : namespaceForWorldType(worldType, missing.keySet());

        // Apply or prune remembered decisions
        List<String> undecided = new ArrayList<>();
        for (String ns : missing.keySet()) {
            String decision = file.decisions.get(ns);
            if (decision == null) {
                undecided.add(ns);
            } else {
                if (REMOVE.equals(decision)) {
                    RetainedContent.removedNamespaces().add(ns);
                }
                RetainedContent.retentionDecisions().put(ns, decision);
                LOGGER.info("World {}: {} entries of {} are missing; applying your '{}' choice", worldDir.getName(),
                    missing.get(ns).values().stream().mapToInt(i -> i).sum(), ns, decision
                );
            }
        }
        // Decisions for namespaces whose content all resolves again are never copied into
        // RetainedContent.retentionDecisions(), so the file forgets them on the next save and a
        // future removal asks again.

        // The world type is missing too: it belongs to one of the missing mods, or stands alone
        String worldTypeKey = null;
        if (worldType != null && worldTypeNamespace == null) {
            worldTypeKey = worldType;
            String decision = file.decisions.get(worldTypeKey);
            if (decision != null) {
                RetainedContent.retentionDecisions().put(worldTypeKey, decision);
                applyWorldTypeDecision(info, worldType, decision);
            } else {
                undecided.add(worldTypeKey);
            }
        }

        if (!undecided.isEmpty()) {
            int pick = StartupQuery.choice(promptText(file, missing, undecided, worldType, worldTypeNamespace),
                "Retain", "Remove"
            );
            String answer = pick == 1 ? REMOVE : RETAIN;
            for (String key : undecided) {
                RetainedContent.retentionDecisions().put(key, answer);
                if (REMOVE.equals(answer)) {
                    if (key.equals(worldTypeKey)) {
                        applyWorldTypeDecision(info, worldType, REMOVE);
                    } else {
                        RetainedContent.removedNamespaces().add(key);
                    }
                }
            }
        }
        // An attributed world type follows its namespace's decision
        if (worldType != null && worldTypeNamespace != null) {
            String decision = RetainedContent.retentionDecisions().get(worldTypeNamespace);
            if (REMOVE.equals(decision)) {
                applyWorldTypeDecision(info, worldType, REMOVE);
            }
        }

        file.standInTerrain.forEach((type, dims) -> {
            Map<Integer, LongSet> dst = RetainedContent.standInTerrain()
                .computeIfAbsent(type, t -> new java.util.concurrent.ConcurrentHashMap<>());
            dims.forEach((dim, chunks) -> dst.computeIfAbsent(dim,
                d -> it.unimi.dsi.fastutil.longs.LongSets.synchronize(new LongOpenHashSet())
            )
                .addAll(chunks));
        });
        askAboutStandInTerrain(worldDir, info, file);
    }

    /*
     * Stand-in terrain the saved generator type can now generate itself: for each indexed type with a
     * non-empty dimension-0 set (only the overworld honours the world type; other dimensions' entries
     * are ignored and dropped), offer Regenerate or Keep when the type is back, the world runs it, and
     * no {@code regen:<type>} decision is recorded.
     */
    private static void askAboutStandInTerrain(File worldDir, WorldInfo info, RetentionFile file) {
        for (Map.Entry<String, Map<Integer, LongSet>> entry : RetainedContent.standInTerrain().entrySet()) {
            String typeName = entry.getKey();
            Map<Integer, LongSet> dims = entry.getValue();
            // dimensions other than the overworld never honour its world type: ignored and dropped
            dims.entrySet().removeIf(e -> !e.getKey().equals(0));
            LongSet marked = dims.get(0);
            if (marked == null || marked.isEmpty()) {
                continue;
            }
            WorldType type = WorldType.byName(typeName);
            if (type == null || info.getTerrainType() != type) {
                continue; // still missing, or the world runs a different type now: the index persists
            }
            if ("keep".equals(file.decisions.get("regen:" + typeName))) {
                marked.clear(); // a remembered Keep never asks again
                continue;
            }
            // dedicated-server default is Keep (index 1): nobody should regenerate by accident
            int pick = StartupQuery.choice(regenPromptText(marked.size(), typeName), 1, "cleanroom.standInTerrain",
                "Regenerate", "Keep"
            );
            if (pick == 0) {
                backupForRegen(worldDir);
                RegionFileCache.clearRegionFileReferences(); // release the open .mca handles first
                File regionDir = new File(worldDir, "region");
                int erased = 0;
                for (long key : marked) {
                    try {
                        RegionChunkEraser.erase(regionDir, new ChunkPos(ChunkPos.getX(key), ChunkPos.getZ(key)));
                        erased++;
                    } catch (IOException e) {
                        LOGGER.warn("Could not erase stand-in chunk {},{} of {}", ChunkPos.getX(key),
                            ChunkPos.getZ(key), typeName, e
                        );
                    }
                }
                LOGGER.info("Erased {} stand-in chunks of {}; they regenerate on next visit", erased, typeName);
            } else {
                RetainedContent.retentionDecisions().put("regen:" + typeName, "keep");
            }
            marked.clear();
        }
    }

    private static void backupForRegen(File worldDir) {
        try {
            String skip = System.getProperty("fml.doNotBackup");
            if (skip == null || !"true".equals(skip)) {
                ZipperUtil.backupWorld(worldDir);
            } else {
                for (int x = 0; x < 10; x++) {
                    LOGGER.error("!!!!!!!!!! UPDATING WORLD WITHOUT DOING BACKUP !!!!!!!!!!!!!!!!");
                }
            }
        } catch (IOException e) {
            StartupQuery.notify("The world backup couldn't be created.\n\n" + e);
            StartupQuery.abort();
        }
    }

    private static String regenPromptText(int count, String typeName) {
        return count + " chunks of the overworld were generated with vanilla terrain while world type " + typeName +
            " was missing, and nothing has been built in them.\n\n" + "Regenerate: those chunks are deleted now and " +
            typeName + " generates them fresh when you next visit; " +
            "chunks you built in are never touched, so seams remain only around your builds. A backup of the world is made first.\n" +
            "Keep: the vanilla terrain stays and you will not be asked again for these chunks.";
    }

    /* Clear the retained generator name (the world runs on DEFAULT, and stays there). */
    private static void applyWorldTypeDecision(WorldInfo info, String worldType, String decision) {
        if (REMOVE.equals(decision)) {
            LOGGER.info("World type {} is missing and was removed on load; the world keeps the default generator.",
                worldType
            );
            info.setTerrainType(WorldType.DEFAULT); // a deliberate choice: drops the retained name
        }
    }

    private static String promptText(RetentionFile file, Map<String, Map<String, Integer>> missing,
        List<String> undecided, @Nullable String worldType, @Nullable String worldTypeNamespace
    ) {
        StringBuilder sb = new StringBuilder("Content from mods that are not installed was found in this world:\n\n");
        for (String key : undecided) {
            Map<String, Integer> counts = missing.get(key);
            if (counts == null) {
                // the unattributed world type stands alone
                sb.append("  World type ").append(key).append(" is missing (its mod is not installed)\n");
                continue;
            }
            String display = file.mods.getOrDefault(key, key);
            sb.append("  ").append(display);
            if (!display.equals(key)) {
                sb.append(" (").append(key).append(")");
            }
            sb.append(": ");
            List<String> parts = new ArrayList<>();
            counts.forEach((census, count) -> {
                String label = count == 1 ? SINGULAR.getOrDefault(census, census) : LABELS.getOrDefault(census, census);
                parts.add(count + " " + label);
            });
            sb.append(String.join(", ", parts)).append('\n');
            if (worldType != null && key.equals(worldTypeNamespace)) {
                sb.append("  World type ").append(worldType).append(" also belongs to this mod\n");
            }
        }
        sb.append("\nRetain: the world remembers this content and it comes back when the mod is reinstalled. Anything you build meanwhile stays.\n"
        );
        sb.append("Remove: the content is deleted for good as chunks are loaded; a reinstalled mod will not bring it back."
        );
        return sb.toString();
    }

    public void write(File worldDir) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Format", FORMAT);
        NBTTagCompound regs = new NBTTagCompound();
        registries.forEach((key, names) -> {
            NBTTagList list = new NBTTagList();
            for (String name : names) {
                list.appendTag(new NBTTagString(name));
            }
            regs.setTag(key, list);
        });
        tag.setTag("Registries", regs);
        NBTTagCompound mods = new NBTTagCompound();
        this.mods.forEach(mods::setString);
        tag.setTag("Mods", mods);
        NBTTagCompound decisions = new NBTTagCompound();
        this.decisions.forEach(decisions::setString);
        tag.setTag("Decisions", decisions);
        if (generator != null) {
            tag.setString("Generator", generator);
        }
        NBTTagCompound terrain = new NBTTagCompound();
        standInTerrain.forEach((type, dims) -> {
            NBTTagCompound byDim = new NBTTagCompound();
            dims.forEach((dim, chunks) -> byDim.setTag(String.valueOf(dim), new NBTTagLongArray(chunks.toLongArray())));
            terrain.setTag(type, byDim);
        });
        tag.setTag("StandInTerrain", terrain);
        try {
            CompressedStreamTools.write(tag, new File(worldDir, FILE_NAME));
        } catch (Exception e) {
            LOGGER.warn("Could not write {}; retention information was not updated", FILE_NAME, e);
        }
    }

    /* namespace -> census key -> count of saved names that resolve to nothing now; namespaces with none absent. */
    public Map<String, Map<String, Integer>> missingByNamespace() {
        Map<String, Map<String, Integer>> missing = new TreeMap<>();
        for (Map.Entry<String, List<String>> e : registries.entrySet()) {
            Census census = null;
            for (Census c : censuses()) {
                if (c.key.equals(e.getKey())) {
                    census = c;
                    break;
                }
            }
            if (census == null) {
                continue; // an unknown registry key from a newer file: nothing to check it against
            }
            for (String name : e.getValue()) {
                if (!census.present.test(name)) {
                    String namespace = new ResourceLocation(name).getNamespace();
                    missing.computeIfAbsent(namespace, k -> new TreeMap<>()).merge(census.key, 1, Integer::sum);
                }
            }
        }
        return missing;
    }

    /* One census list: its key in the file, the registered names now, and whether a saved name resolves. */
    private static final class Census {

        private final String key;
        private final Supplier<List<String>> registered;
        private final Predicate<String> present;

        Census(String key, Supplier<List<String>> registered, Predicate<String> present) {
            this.key = key;
            this.registered = registered;
            this.present = present;
        }

    }

}
