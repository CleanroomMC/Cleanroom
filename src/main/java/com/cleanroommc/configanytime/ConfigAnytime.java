package com.cleanroommc.configanytime;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

public class ConfigAnytime {
    /**
     * Register configuration class that is annotated with {@link Config} here for it to be processed immediately with saving and loading supported.
     * Preferably call this method in a static init block at the very end of your configuration class.
     * @param configClass configuration class that is annotated with {@link Config}
     */
    public static void register(Class<?> configClass) {
        ConfigManager.register(configClass);
    }
}
