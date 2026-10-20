/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

/**
 * Section layouts written by the 1.12.2 ID-expansion mods. {@link SectionBlockFormat} reads them and the next save
 * writes the Cleanroom format, which those mods and an older Cleanroom cannot read.
 * <ul>
 * <li>JEID/REID: a section's {@code Blocks}/{@code Data} are 12-bit indices into an int-array {@code Palette} of numeric
 * state ids ({@code blockId << 4 | meta}). {@code Biomes} is an int array.</li>
 * <li>NEID (1.12.2): sections have {@code Blocks}, {@code Data}, optional {@code Add}, and nibble {@code Add2} for
 * block ID bits 12..15.</li>
 * </ul>
 */
public final class LegacyIdWorlds {

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
