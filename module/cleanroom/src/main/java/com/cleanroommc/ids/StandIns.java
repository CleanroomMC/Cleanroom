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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The vanilla state a retained (unregistered) block reads as while its mod is absent. It is a reading only:
 * the saved name is what goes back out (see {@link RetainedContent}), so the real block returns when the mod
 * does. Ground blocks get solid stand-ins so terrain has no holes; plants, fluids and anything unmatched stay
 * air — a vanilla tallgrass pops off on the next update and a fluid flows, and either write would erase the
 * retained name.
 * <p>
 * {@code config/cleanroom/retained_standins.cfg} overrides the built-in rules: lines of
 * {@code substring = namespace:block[property=value,...]} or {@code substring = air}, first match on the
 * lowercase block path wins, {@code #} comments. Biome overrides use a {@code biome:} prefix:
 * {@code biome:substring = namespace:name} or {@code biome:substring = none} (the provider decides).
 */
public final class StandIns {

    private static final Logger LOGGER = LogManager.getLogger("Cleanroom");
    private static final List<String> HEADER = List.of("# Stand-ins for retained (unregistered) block states.",
        "# Lines: substring = namespace:block[property=value,...] or substring = air",
        "# The first match on the lowercase block path wins, before the built-in defaults.",
        "# Example: sapphire_ore = minecraft:lapis_ore",
        "# Biomes: prefix the substring with biome: (e.g. biome:ominous_woods = minecraft:roofed_forest);",
        "# biome:foo = none makes the column fall back to the world's biome provider."
    );

    private record Override(String substring, @Nullable IBlockState state) { }

    private record BiomeOverride(String substring, @Nullable Biome biome) { }

    private static volatile List<Override> overrides = List.of();
    private static volatile List<BiomeOverride> biomeOverrides = List.of();
    private static volatile boolean loaded;

    private StandIns() { }

    /* The stand-in for a saved {@code namespace:block[props]} string, or null when it should read as air. */
    @Nullable
    public static IBlockState forMissing(String savedState) {
        int open = savedState.indexOf('[');
        String block = open < 0 ? savedState : savedState.substring(0, open);
        int colon = block.indexOf(':');
        String path = (colon < 0 ? block : block.substring(colon + 1)).toLowerCase(Locale.ROOT);
        for (Override override : overrides()) {
            if (path.contains(override.substring())) {
                return override.state();
            }
        }
        IBlockState standIn = defaultFor(path, savedState);
        if (standIn == null) {
            return null;
        }
        // leaves pinned non-decaying: a saved decayable=true could break on a block update and lose the name
        return copyProperties(standIn, savedState, standIn.getBlock() == Blocks.LEAVES ? LEAF_PINNED : Set.of());
    }

    private static final Set<String> LEAF_PINNED = Set.of("decayable", "check_decay");

    /* The built-in rules: first match on the lowercase block path. */
    @Nullable
    private static IBlockState defaultFor(String path, String savedState) {
        if (path.contains("leaves")) {
            return Blocks.LEAVES
                .getDefaultState()
                .withProperty(BlockLeaves.DECAYABLE, false)
                .withProperty(BlockLeaves.CHECK_DECAY, false);
        }
        if ((path.contains("log") || path.contains("wood")) && !path.contains("planks")) {
            return Blocks.LOG.getDefaultState();
        }
        if (path.contains("planks")) {
            return Blocks.PLANKS.getDefaultState();
        }
        if (path.contains("stairs")) {
            return Blocks.OAK_STAIRS.getDefaultState();
        }
        if (path.contains("slab")) {
            return path.contains("double")
                ? Blocks.DOUBLE_WOODEN_SLAB.getDefaultState()
                : Blocks.WOODEN_SLAB.getDefaultState();
        }
        if (path.contains("fence_gate")) {
            return Blocks.OAK_FENCE_GATE.getDefaultState();
        }
        if (path.contains("fence")) {
            return Blocks.OAK_FENCE.getDefaultState();
        }
        if (path.contains("grass") &&
            !path.contains("tall") &&
            !path.contains("plant") &&
            !path.contains("flower") &&
            !path.contains("double")) {
            return Blocks.GRASS.getDefaultState();
        }
        if (path.contains("dirt") || path.contains("mud")) {
            IBlockState dirt = Blocks.DIRT.getDefaultState();
            return "true".equals(savedProperty(savedState, "coarse"))
                ? dirt.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
                : dirt;
        }
        if (path.contains("sandstone")) {
            return Blocks.SANDSTONE.getDefaultState();
        }
        if (path.contains("sand")) {
            return Blocks.SAND.getDefaultState();
        }
        if (path.contains("gravel")) {
            return Blocks.GRAVEL.getDefaultState();
        }
        if (path.contains("glass")) {
            boolean colored = savedProperty(savedState, "color") != null;
            if (path.contains("pane")) {
                return (colored ? Blocks.STAINED_GLASS_PANE : Blocks.GLASS_PANE).getDefaultState();
            }
            return (colored ? Blocks.STAINED_GLASS : Blocks.GLASS).getDefaultState();
        }
        if (path.contains("ore") || path.contains("stone") || path.contains("rock")) {
            return Blocks.STONE.getDefaultState();
        }
        return null;
    }

    /* Same-named properties whose saved value parses on the stand-in block carry over; pinned names and the rest keep defaults. */
    private static IBlockState copyProperties(IBlockState state, String savedState, Set<String> pinned) {
        int open = savedState.indexOf('[');
        int close = savedState.endsWith("]") ? savedState.length() - 1 : savedState.length();
        if (open < 0 || open + 1 >= close) {
            return state;
        }
        net.minecraft.block.state.BlockStateContainer definition = state.getBlock().getBlockState();
        for (String pair : savedState.substring(open + 1, close).split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String name = pair.substring(0, eq);
                IProperty<?> property = pinned.contains(name) ? null : definition.getProperty(name);
                if (property != null) {
                    state = withValue(state, property, pair.substring(eq + 1));
                }
            }
        }
        return state;
    }

    @Nullable
    private static String savedProperty(String savedState, String name) {
        int open = savedState.indexOf('[');
        int close = savedState.endsWith("]") ? savedState.length() - 1 : savedState.length();
        if (open < 0 || open + 1 >= close) {
            return null;
        }
        for (String pair : savedState.substring(open + 1, close).split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }

    private static <T extends Comparable<T>> IBlockState withValue(IBlockState state, IProperty<T> property,
        String value
    ) {
        return property.parseValue(value).toJavaUtil().map(v -> state.withProperty(property, v)).orElse(state);
    }

    /*
     * The vanilla biome a retained biome column reads as, or null to fall back to the world's biome provider
     * (the previous behaviour). A reading only: the saved name is what writes back.
     */
    @Nullable
    public static Biome forMissingBiome(String savedName) {
        int colon = savedName.indexOf(':');
        String path = (colon < 0 ? savedName : savedName.substring(colon + 1)).toLowerCase(Locale.ROOT);
        ensureLoaded();
        for (BiomeOverride override : biomeOverrides) {
            if (path.contains(override.substring())) {
                return override.biome();
            }
        }
        return defaultBiomeFor(path);
    }

    /* The built-in biome rules: specific words before generic ones, first match on the lowercase path. */
    @Nullable
    private static Biome defaultBiomeFor(String path) {
        if (contains(path, "mushroom")) {
            return Biomes.MUSHROOM_ISLAND;
        }
        if (contains(path, "nether", "hell", "infern", "ash", "volcan", "magma", "lava", "brimstone")) {
            return Biomes.HELL;
        }
        if (contains(path, "end", "void")) {
            return Biomes.SKY;
        }
        if (contains(path, "ocean", "sea", "kelp", "coral", "reef", "abyss")) {
            return Biomes.OCEAN;
        }
        if (contains(path, "beach", "shore", "coast", "dune")) {
            return Biomes.BEACH;
        }
        if (contains(path, "river", "stream", "brook")) {
            return Biomes.RIVER;
        }
        if (contains(path, "swamp", "bog", "marsh", "fen", "wetland", "bayou", "mangrove", "quagmire", "moor", "mire"
        )) {
            return Biomes.SWAMPLAND;
        }
        if (contains(path, "jungle", "rainforest", "tropic", "bamboo", "overgrown")) {
            return Biomes.JUNGLE;
        }
        if (contains(path, "mesa", "badland", "canyon", "outback", "xeric", "brushland")) {
            return Biomes.MESA;
        }
        if (contains(path, "desert", "arid", "waste", "sahara", "lush_desert")) {
            return Biomes.DESERT;
        }
        // "grove" lives in the forest rule: cherry_blossom_grove and friends are woods, not fields
        if (contains(path, "savanna", "prairie", "steppe", "grassland", "shrubland", "chaparral", "pasture", "meadow",
            "flower", "orchard", "garden", "field"
        )) {
            return Biomes.PLAINS;
        }
        if (contains(path, "snow", "frozen", "tundra", "glacier", "arctic", "polar", "ice", "alps", "boreal", "cold")) {
            return Biomes.ICE_PLAINS;
        }
        if (contains(path, "taiga", "conifer", "redwood", "fir", "spruce", "pine", "sequoia")) {
            return Biomes.TAIGA;
        }
        if (contains(path, "mountain", "highland", "cliff", "crag", "peak", "ridge", "hill")) {
            return Biomes.EXTREME_HILLS;
        }
        if (contains(path, "ominous", "dark", "shadow", "haunted", "dead", "gloom", "mystic", "eerie", "roofed",
            "thick", "dense"
        )) {
            return Biomes.ROOFED_FOREST;
        }
        if (contains(path, "forest", "wood", "grove", "thicket", "cherry", "maple", "birch", "oak", "land", "wald")) {
            return Biomes.FOREST;
        }
        return null;
    }

    private static boolean contains(String path, String... words) {
        for (String word : words) {
            if (path.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private static void ensureLoaded() {
        if (!loaded) {
            synchronized (StandIns.class) {
                if (!loaded) {
                    File configDir = Loader.instance().getConfigDir();
                    Path file = configDir != null
                        ? configDir.toPath().resolve("cleanroom/retained_standins.cfg")
                        : null;
                    if (file != null && !Files.exists(file)) {
                        try {
                            Files.createDirectories(file.getParent());
                            Files.write(file, HEADER);
                        } catch (IOException e) {
                            LOGGER.warn("Could not write default retained stand-ins config {}", file, e);
                        }
                    }
                    load(file);
                }
            }
        }
    }

    private static List<Override> overrides() {
        ensureLoaded();
        return overrides;
    }

    /* Replaces the override tables from a config file; null gives none. A bad line is warned about and skipped. */
    static synchronized void load(@Nullable Path file) {
        List<Override> list = new ArrayList<>();
        List<BiomeOverride> biomeList = new ArrayList<>();
        if (file != null && Files.exists(file)) {
            try {
                for (String line : Files.readAllLines(file)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    int eq = trimmed.indexOf('=');
                    if (eq < 0) {
                        LOGGER.warn("Ignoring bad retained stand-in line: {}", trimmed);
                        continue;
                    }
                    String substring = trimmed.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                    String state = trimmed.substring(eq + 1).trim();
                    if (substring.isEmpty() || state.isEmpty()) {
                        LOGGER.warn("Ignoring bad retained stand-in line: {}", trimmed);
                        continue;
                    }
                    if (substring.startsWith("biome:")) {
                        String word = substring.substring("biome:".length());
                        if (word.isEmpty()) {
                            LOGGER.warn("Ignoring bad retained stand-in line: {}", trimmed);
                            continue;
                        }
                        if (state.equals("none")) {
                            biomeList.add(new BiomeOverride(word, null));
                            continue;
                        }
                        Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(state));
                        if (biome == null) {
                            LOGGER.warn("Ignoring retained stand-in line with an unregistered biome: {}", trimmed);
                            continue;
                        }
                        biomeList.add(new BiomeOverride(word, biome));
                        continue;
                    }
                    IBlockState parsed = SectionBlockFormat.parse(state);
                    if (parsed.getBlock() == Blocks.AIR && !state.equals("air") && !state.equals("minecraft:air")) {
                        LOGGER.warn("Ignoring retained stand-in line with an unregistered block: {}", trimmed);
                        continue;
                    }
                    list.add(new Override(substring, parsed.getBlock() == Blocks.AIR ? null : parsed));
                }
            } catch (IOException e) {
                LOGGER.warn("Could not read retained stand-ins config {}", file, e);
            }
        }
        overrides = list;
        biomeOverrides = biomeList;
        loaded = true;
    }

}
