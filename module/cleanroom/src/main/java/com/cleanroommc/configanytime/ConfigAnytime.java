package com.cleanroommc.configanytime;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

@Deprecated
public class ConfigAnytime {
    /**
     * A wrapper of {@link ConfigManager#register(Class)}, created for compatibility.
     * @param configClass configuration class that is annotated with {@link Config}
     */
    public static void register(Class<?> configClass) {
        ConfigManager.register(configClass);
    }
}
