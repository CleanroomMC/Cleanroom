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

import java.io.PrintStream;

/**
 * Stands in for the static forwarders of {@code scala.Int}, {@code scala.Console}, {@code scala.Array} and
 * {@code scala.collection.concurrent.Debug}. The names match the forwarders, so only the owner is remapped.
 */
// CHECKSTYLE:OFF
public final class Statics {

    private Statics() { }

    public static int MaxValue() {
        return Integer.MAX_VALUE;
    }

    public static int MinValue() {
        return Integer.MIN_VALUE;
    }

    public static int unbox(Object value) {
        return (Integer) value;
    }

    public static void println(Object value) {
        System.out.println(value);
    }

    public static void print(Object value) {
        System.out.print(value);
    }

    public static PrintStream out() {
        return System.out;
    }

    public static String RESET() {
        return "\u001b[0m";
    }

    public static boolean log(Object value) {
        return true;
    }

    public static void copy(
        Object source, int sourcePosition, Object destination, int destinationPosition, int length) {
        System.arraycopy(source, sourcePosition, destination, destinationPosition, length);
    }

    public static int[] emptyIntArray() {
        return new int[0];
    }

}
// CHECKSTYLE:ON
