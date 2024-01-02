package com.cleanroommc.configanytime;

import net.minecraftforge.common.config.ConfigManager;

/**
 * Author: Rongmario & CleanroomMC.
 * Code from ConfigAnytime.
 */

public class ConfigAnytime {
    /**
      *  Merged ConfigAnytime.
      *  Use this to be able compatibility.
     */
    public static void register(Class<?> configClass) {
        ConfigManager.ConfigAnytime.register(configClass);
    }

}
