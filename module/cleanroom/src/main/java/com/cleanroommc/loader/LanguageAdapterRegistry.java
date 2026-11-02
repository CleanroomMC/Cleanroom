/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.loader;

import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ILanguageAdapter;

import java.util.Map;

public final class LanguageAdapterRegistry {

    private static final Map<String, ILanguageAdapter> adapterRegistry = Maps.newHashMap();

    static {
        registerLanguageAdapter("java", new ILanguageAdapter.JavaAdapter());
    }

    private LanguageAdapterRegistry() { }

    public static void registerLanguageAdapter(String language, ILanguageAdapter languageAdapter) {
        if (adapterRegistry.containsKey(language)) {
            FMLLog.log.error(
                "Language adapter {} of language {} already exists!",
                adapterRegistry.get(language).getClass().getName(), language
            );
        } else {
            adapterRegistry.put(language, languageAdapter);
            FMLLog.log.debug(
                "Registering language adapter {} for language {}.", languageAdapter.getClass().getName(), language
            );
        }
    }

    public static ILanguageAdapter getAdapterFor(String language) {
        if (LanguageAdapterRegistry.adapterRegistry.containsKey(language)) {
            return LanguageAdapterRegistry.adapterRegistry.get(language);
        } else {
            return new ILanguageAdapter.JavaAdapter();
        }
    }

}
