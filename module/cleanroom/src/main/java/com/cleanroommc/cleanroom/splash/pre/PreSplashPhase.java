package com.cleanroommc.cleanroom.splash.pre;

import com.cleanroommc.discovery.CleanroomModDiscoverer;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.relauncher.CoreModManager;

import java.io.File;

public enum PreSplashPhase {

    /**
     * @see FMLTweaker#injectIntoClassLoader(LaunchClassLoader)
     */
    FML_SETUP,

    /**
     * @see CleanroomModDiscoverer#discover()
     */
    INITIAL_MOD_SCAN,

    /**
     * @see CoreModManager#handleLaunch(File, LaunchClassLoader, FMLTweaker)
     */
    CORE_MOD_LAUNCH,

    /**
     * @see Minecraft#init()
     */
    MINECRAFT_INIT
}
