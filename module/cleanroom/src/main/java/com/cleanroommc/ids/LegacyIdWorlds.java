/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.ids;

/**
 * Section layouts written by the 1.12 ID-expansion mods. {@link SectionBlockFormat} reads them and the next save
 * writes the Cleanroom format, which those mods and an older Cleanroom cannot read.
 * <ul>
 * <li>JEID/REID: a section's {@code Blocks}/{@code Data} are 12-bit indices into an int-array {@code Palette} of numeric
 * state ids ({@code blockId << 4 | meta}). {@code Biomes} is an int array. Built from the mods' source, not from a real
 * world.</li>
 * <li>NEID: sections have {@code Blocks16} (two bytes per block, big-endian block id) and {@code Data} nibbles. Built from
 * GTNH NEID's source; not checked against a 1.12 NEID world.</li>
 * </ul>
 */
public final class LegacyIdWorlds {

    public static final String BLOCKS16 = "Blocks16";
    /* DataVersion written by JEID and REID ({@code Integer.MAX_VALUE / 2}, {@code "1.13-JEID"}). */
    public static final int JEID_DATA_VERSION = Integer.MAX_VALUE / 2;

    private LegacyIdWorlds() { }

    /* JEID packs a state id as {@code blockId << 4 | meta}. */
    public static int jeidBlockId(int packedState) {
        return packedState >>> 4;
    }

    public static int jeidMeta(int packedState) {
        return packedState & 15;
    }

}
