/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.discovery;

import com.cleanroommc.language.scala.ScalaUsage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

public record DiscoveredMod(
    File file,
    Attributes attributes,
    List<String> modIds,
    Map<String, String> modNames,
    Map<String, String> modVersions,
    List<String> mixinConfigurationClasses,
    String modType,
    boolean hasMixinManifestAttributes,
    boolean mixinTweakerForceMod,
    boolean coreModContainsMod,
    String coremod,
    String tweaker,
    boolean onClasspath,
    Map<String, String> accessTransformers,
    Set<String> dependencies,
    List<String> fermiumConfigClasses,
    ScalaUsage scala) {

    public DiscoveredMod {
        file = file.getAbsoluteFile();
        modIds = List.copyOf(modIds);
        modNames = Map.copyOf(modNames);
        modVersions = Map.copyOf(modVersions);
        mixinConfigurationClasses = List.copyOf(mixinConfigurationClasses);
        accessTransformers = Map.copyOf(accessTransformers);
        dependencies = Set.copyOf(dependencies);
        fermiumConfigClasses = List.copyOf(fermiumConfigClasses);
    }

    public DiscoveredMod withRewrittenFile(File rewritten) {
        return new DiscoveredMod(
            rewritten, attributes, modIds, modNames, modVersions, mixinConfigurationClasses, modType,
            hasMixinManifestAttributes, mixinTweakerForceMod, coreModContainsMod, coremod, tweaker, onClasspath,
            accessTransformers, dependencies, fermiumConfigClasses, new ScalaUsage(scala.kind(), scala.config(), file)
        );
    }

    public boolean isDirectoryBased() {
        return file.isDirectory();
    }

}
