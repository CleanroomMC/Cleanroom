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

import net.minecraft.util.math.ChunkPos;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Deletes one chunk from an Anvil region file by zeroing its offset and timestamp entries. The sector
 * payload is left in place — a zeroed offset means "no chunk" to every reader, and the space is
 * reclaimed the next time the region file rewrites that entry. Used by the stand-in-terrain
 * "Regenerate" answer in {@link RetentionFile}.
 */
public final class RegionChunkEraser {

    private RegionChunkEraser() { }

    /*
     * Zero the offset and timestamp entries of {@code pos}'s chunk in {@code regionDir}'s
     * {@code r.X.Z.mca}. A missing region file is nothing to do.
     */
    public static void erase(File regionDir, ChunkPos pos) throws IOException {
        File file = new File(regionDir, "r." + (pos.x >> 5) + "." + (pos.z >> 5) + ".mca");
        if (!file.isFile()) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            int index = ((pos.x & 31) + (pos.z & 31) * 32) * 4;
            raf.seek(index);
            raf.writeInt(0);
            raf.seek(4096 + index);
            raf.writeInt(0);
        }
    }

}
