/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanmix.service;

import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.boot.PrefixMatcher;

import org.spongepowered.asm.service.IClassTracker;

import net.minecraft.launchwrapper.Launch;

final class FoundationClassTracker implements IClassTracker {

    private final ActualClassLoader classLoader;

    FoundationClassTracker() {
        this.classLoader = Launch.classLoader;
    }

    private static boolean isExcluded(PrefixMatcher matcher, String name, String transformedName) {
        return matcher.matches(name) || (transformedName != null && matcher.matches(transformedName));
    }

    @Override
    public boolean isClassLoaded(String name) {
        return this.classLoader.isClassLoaded(name);
    }

    @Override
    public String getClassRestrictions(String className) {
        String restrictions = "";
        if (ActualClassLoader.classLoaderExceptions.matches(className)) {
            restrictions = "PACKAGE_CLASSLOADER_EXCLUSION";
        }
        if (ActualClassLoader.transformerExceptions.matches(className)) {
            restrictions = (!restrictions.isEmpty() ? restrictions + "," : "") + "PACKAGE_TRANSFORMER_EXCLUSION";
        }
        return restrictions;
    }

    @Override
    public void registerInvalidClass(String name) {
        this.classLoader.registerInvalidClass(name);
    }

    /** Whether the name or transformedName is excluded from the classloader or from the transformer chain. */
    boolean isClassExcluded(String name, String transformedName) {
        return isExcluded(ActualClassLoader.classLoaderExceptions, name, transformedName) ||
            isExcluded(ActualClassLoader.transformerExceptions, name, transformedName);
    }

}
