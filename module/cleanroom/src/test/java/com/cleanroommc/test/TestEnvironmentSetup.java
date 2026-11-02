/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test;

import com.cleanroommc.common.CleanroomEnvironment;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import net.minecraftforge.fml.relauncher.Side;

/**
 * Tests run without a tweaker, so nothing sets the side.
 * Anything touching FMLLaunchHandler fails its static init without this.
 */
public final class TestEnvironmentSetup implements LauncherSessionListener {

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        CleanroomEnvironment.setSide(Side.CLIENT); // Fixme: when server?
    }

}
