package com.cleanroommc.catalogue.platform;

import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class ClientServices {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        CatalogueConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
