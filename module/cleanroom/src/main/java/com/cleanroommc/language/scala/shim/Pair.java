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
 * Stands in for {@code akka.japi.Pair}.
 */
public record Pair<A, B>(A first, B second) {

    public static <A, B> Pair<A, B> apply(A first, B second) {
        return new Pair<>(first, second);
    }

}
