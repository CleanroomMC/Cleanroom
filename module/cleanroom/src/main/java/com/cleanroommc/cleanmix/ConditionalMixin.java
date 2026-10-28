/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanmix;

import com.cleanroommc.discovery.CleanroomModDiscoverer;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.MixinService;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds mixin configurations only when their conditions, options and the installed mods allow it.
 * Mods are checked against {@link CleanroomModDiscoverer}, so this works from mods and
 * {@link org.spongepowered.asm.mixin.connect.IMixinConnector}s alike.
 * {@snippet :
 * ConditionalMixin.of("mixins.mymod.jei.json")
 *         .option(MyConfig.class, "jeiIntegration")
 *         .requires("jei").version("[4.16,)").orCrash()
 *         .conflicts("otherfix").because("Both rewrite the same method")
 *         .recommends("jei").named("Had Enough Items")
 *         .register();
 * }
 * {@link #named(String)}, {@link #version(String)}, {@link #because(String)}, {@link #orWarn()} and {@link #orCrash()}
 * refine the mod given just before them.
 *
 * @see MixinConfiguration
 */
public final class ConditionalMixin {

    private static final Map<String, Map<String, Boolean>> options = new HashMap<>();
    private static final Set<Class<?>> loadedConfigs = new HashSet<>();

    private final String[] configurations;
    private final List<ModRule> rules = new ArrayList<>();

    private String disabledBy;

    private ConditionalMixin(boolean condition, String[] configurations) {
        this.configurations = configurations;
        if (!condition) {
            this.disable("has its condition unmet");
        }
    }

    /**
     * @param configurations mixin configurations to add together
     */
    public static ConditionalMixin of(String... configurations) {
        return new ConditionalMixin(true, configurations);
    }

    /**
     * @param condition      any other condition the configurations depend on
     * @param configurations mixin configurations to add together
     */
    public static ConditionalMixin of(boolean condition, String... configurations) {
        return new ConditionalMixin(condition, configurations);
    }

    private static Map<String, Boolean> readBooleans(String config) {
        Map<String, Boolean> values = new HashMap<>();
        File file = new File(Launch.minecraftHome, "config/" + config + ".cfg");
        if (!file.isFile()) {
            return values;
        }
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("B:")) {
                    continue;
                }
                String entry = line.substring(2);
                boolean quoted = entry.startsWith("\"");
                int split = quoted ? entry.indexOf("\"=", 1) + 1 : entry.indexOf('=');
                if (split > 0) {
                    String name = quoted ? entry.substring(1, split - 1) : entry.substring(0, split);
                    values.merge(name, Boolean.parseBoolean(entry.substring(split + 1).trim()), Boolean::logicalOr);
                }
            }
        } catch (IOException e) {
            MixinService.getService()
                .getLogger("CleanMix")
                .error("Failed to read {}, using default values for its options.", file, e);
        }
        return values;
    }

    /**
     * Reads a boolean option from {@code config/<config>.cfg} before Forge loads it. The option is matched by name in
     * any category.
     *
     * @param config       the config file name, without {@code .cfg}
     * @param option       the option name
     * @param defaultValue used when the file or option does not exist yet
     */
    public ConditionalMixin option(String config, String option, boolean defaultValue) {
        Boolean value = options.computeIfAbsent(config, ConditionalMixin::readBooleans).get(option);
        if (!(value == null ? defaultValue : value)) {
            this.disable("is turned off by " + option + " in " + config + ".cfg");
        }
        return this;
    }

    /**
     * Reads a boolean field of a Forge {@code @Config} class. Forge only loads the class from its config file once the
     * mod is constructed, so it is loaded here first.
     *
     * @param config the {@code @Config} class
     * @param field  the name of a {@code public static boolean} field in it
     * @throws IllegalArgumentException if the class is not a {@code @Config} class or the field is not a public static boolean
     */
    public ConditionalMixin option(Class<?> config, String field) {
        if (!config.isAnnotationPresent(Config.class)) {
            throw new IllegalArgumentException(config.getName() + " is not annotated with @Config");
        }
        try {
            Field option = config.getField(field);
            if (option.getType() != boolean.class || !Modifier.isStatic(option.getModifiers())) {
                throw new IllegalArgumentException(config.getName() + "#" + field + " is not a static boolean");
            }
            if (loadedConfigs.add(config)) {
                ConfigManager.register(config);
            }
            if (!option.getBoolean(null)) {
                this.disable("is turned off by " + field + " in " + config.getName());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(config.getName() + "#" + field + " is not a public field", e);
        }
        return this;
    }

    /**
     * Skips the configurations unless the mod is present.
     */
    public ConditionalMixin requires(String modId) {
        return this.rule(modId, true, true);
    }

    /**
     * Skips the configurations if the mod is present.
     */
    public ConditionalMixin conflicts(String modId) {
        return this.rule(modId, false, true);
    }

    /**
     * Warns, but still adds the configurations, unless the mod is present.
     */
    public ConditionalMixin recommends(String modId) {
        return this.rule(modId, true, false);
    }

    /**
     * Warns, but still adds the configurations, if the mod is present.
     */
    public ConditionalMixin discourages(String modId) {
        return this.rule(modId, false, false);
    }

    /**
     * Only counts the last given mod when it has exactly this name, to tell apart forks sharing a mod id.
     */
    public ConditionalMixin named(String name) {
        this.lastRule().name = name;
        return this;
    }

    /**
     * Only counts the last given mod when its version is in range.
     *
     * @param range a range in the same format as {@code @Mod(dependencies = ...)} such as {@code [1.2,2.0)}
     * @throws IllegalArgumentException if the range is invalid
     */
    public ConditionalMixin version(String range) {
        try {
            VersionRange.createFromVersionSpec(range);
        } catch (InvalidVersionSpecificationException e) {
            throw new IllegalArgumentException("Invalid version range " + range, e);
        }
        this.lastRule().range = range;
        return this;
    }

    /**
     * Explains the last given mod rule in the log.
     */
    public ConditionalMixin because(String reason) {
        this.lastRule().reason = reason;
        return this;
    }

    /**
     * Logs a warning, instead of info, when the last given mod rule fails and skips the configurations.
     */
    public ConditionalMixin orWarn() {
        this.lastRule().warn = true;
        return this;
    }

    /**
     * Crashes the game when the last given mod rule fails, if the configurations are not turned off otherwise.
     */
    public ConditionalMixin orCrash() {
        this.lastRule().crash = true;
        return this;
    }

    /**
     * Adds the configurations if every condition, option and required mod allows it, logging why otherwise.
     * Rules marked with {@link #orCrash()} crash the game once every mixin connector has run.
     *
     * @return whether the configurations were added
     */
    public boolean register() {
        ILogger logger = MixinService.getService().getLogger("CleanMix");
        String configurations = Arrays.toString(this.configurations);
        // Options turned off are common and the user's own choice
        if (this.disabledBy != null) {
            logger.debug("Skipping {} as it {}.", configurations, this.disabledBy);
            return false;
        }
        List<String> unmet = new ArrayList<>();
        List<String> crashes = new ArrayList<>();
        boolean warn = false;
        for (ModRule rule : this.rules) {
            if (rule.desired == rule.matches()) {
                continue;
            }
            if (rule.crash) {
                crashes.add(rule.describe(rule.desired ? "needs" : "conflicts with"));
            } else if (rule.disables) {
                unmet.add(rule.describe(rule.desired ? "needs" : "conflicts with"));
                warn |= rule.warn;
            } else {
                logger.warn(
                    "{} may have issues as it {}.", configurations,
                    rule.describe(rule.desired ? "works best with" : "works badly with")
                );
            }
        }
        if (!crashes.isEmpty()) {
            CleanMixHooks.crash(configurations + " " + String.join(", and ", crashes) + ".");
            return false;
        }
        if (!unmet.isEmpty()) {
            if (warn) {
                logger.warn("Skipping {} as it {}.", configurations, String.join(", and ", unmet));
            } else {
                logger.info("Skipping {} as it {}.", configurations, String.join(", and ", unmet));
            }
            return false;
        }
        for (String configuration : this.configurations) {
            logger.info("Adding [{}] mixin configuration.", configuration);
            Mixins.addConfiguration(configuration);
        }
        return true;
    }

    private void disable(String reason) {
        if (this.disabledBy == null) {
            this.disabledBy = reason;
        }
    }

    private ConditionalMixin rule(String modId, boolean desired, boolean disables) {
        this.rules.add(new ModRule(modId, desired, disables));
        return this;
    }

    private ModRule lastRule() {
        if (this.rules.isEmpty()) {
            throw new IllegalStateException(
                "No mod was given to refine, call requires, conflicts, recommends or discourages first"
            );
        }
        return this.rules.get(this.rules.size() - 1);
    }

    private static final class ModRule {

        private final String modId;
        private final boolean desired;
        private final boolean disables;

        private String name;
        private String range;
        private String reason;
        private boolean warn;
        private boolean crash;

        private ModRule(String modId, boolean desired, boolean disables) {
            this.modId = modId;
            this.desired = desired;
            this.disables = disables;
        }

        private boolean matches() {
            CleanroomModDiscoverer discoverer = CleanroomModDiscoverer.instance();
            return discoverer.isModPresent(this.modId) &&
                (this.name == null || discoverer.isModNamed(this.modId, this.name)) &&
                (this.range == null || discoverer.isModVersionInRange(this.modId, this.range));
        }

        private String describe(String relation) {
            StringBuilder description = new StringBuilder(relation).append(' ').append(this.modId);
            if (this.name != null) {
                description.append(" named ").append(this.name);
            }
            if (this.range != null) {
                description.append(' ').append(this.range);
            }
            if (this.reason != null) {
                description.append(" (").append(this.reason).append(')');
            }
            return description.toString();
        }

    }

}
