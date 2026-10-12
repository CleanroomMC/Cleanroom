/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: temp
public final class CleanroomLog {

    private static final Logger LOGGER = LoggerFactory.getLogger("Cleanroom");

    private CleanroomLog() { }

    public static Logger get() {
        return LOGGER;
    }

}
