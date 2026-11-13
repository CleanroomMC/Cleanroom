/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

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
     * @see CleanroomModDiscoverer#CleanroomModDiscoverer()
     */
    INITIAL_MOD_SCAN,

    /**
     * @see CoreModManager#handleLaunch(File, LaunchClassLoader, FMLTweaker)
     */
    CORE_MOD_LAUNCH,

    /**
     * @see Minecraft#init()
     */
    MINECRAFT_INIT,

    /**
     * @see Minecraft#init()
     */
    RIGHT_BEFORE_SPLASH
}
