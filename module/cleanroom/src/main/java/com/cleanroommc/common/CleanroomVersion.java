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

public final class CleanroomVersion {

    public static final String VERSION = getVersion();
    public static final String BUILD_VERSION = getBuildVersion();
    public static final String MOD_ID = "@{ID}";

    private CleanroomVersion() { }

    public static String getVersion() {
        return "@{VERSION}";
    }

    public static String getBuildVersion() {
        return "@{BUILD}";
    }

}
