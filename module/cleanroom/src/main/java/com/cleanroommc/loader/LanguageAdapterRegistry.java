package com.cleanroommc.loader;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ILanguageAdapter;

import java.util.Map;

public class LanguageAdapterRegistry {
    private static final Map<String, ILanguageAdapter> adapterRegistry = Maps.newHashMap();
    static {
        registerLanguageAdapter("java", new ILanguageAdapter.JavaAdapter());
    }
    public static void registerLanguageAdapter(String language, ILanguageAdapter languageAdapter) {
        if (adapterRegistry.containsKey(language)) {
            FMLLog.log.error("Language adapter {} of language {} already exists!", adapterRegistry.get(language).getClass().getName(), language);
        } else {
            adapterRegistry.put(language, languageAdapter);
            FMLLog.log.debug("Registering language adapter {} for language {}.", languageAdapter.getClass().getName(), language);
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
