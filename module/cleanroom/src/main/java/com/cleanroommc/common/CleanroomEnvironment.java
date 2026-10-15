/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common;

import net.minecraftforge.fml.relauncher.Side;

// TODO: temp
public final class CleanroomEnvironment {

    private static final boolean deobfuscatedEnvironment;

    private static Side side;

    // TODO: check obf class name?
    static {
        boolean deobfEnvironment = System.getProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp") != null;
        deobfEnvironment |= System.getProperty("crl.dev.extrapath") != null;

        deobfuscatedEnvironment = deobfEnvironment;
    }

    private CleanroomEnvironment() { }

    public static void setSide(Side side) {
        if (CleanroomEnvironment.side != null) {
            return;
        }
        CleanroomEnvironment.side = side;
        System.setProperty("cleanroom.side", side.name());
    }

    public static boolean isDev() {
        return deobfuscatedEnvironment;
    }

    public static Side side() {
        if (side == null) {
            String property = System.getProperty("cleanroom.side");
            if (property != null) {
                side = Side.valueOf(property);
            }
        }
        if (side == null) {
            throw new IllegalStateException("Side not yet set.");
        }
        return side;
    }

}
