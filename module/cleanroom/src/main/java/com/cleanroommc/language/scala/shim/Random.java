/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.language.scala.shim;

/**
 * Stands in for {@code scala.util.Random}, which wraps the JDK's with the same methods.
 */
public class Random extends java.util.Random {

    public Random() { }

    public Random(long seed) {
        super(seed);
    }

    public Random(int seed) {
        super(seed);
    }

}
