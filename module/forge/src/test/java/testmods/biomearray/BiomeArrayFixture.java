/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package testmods.biomearray;

import net.minecraft.world.chunk.Chunk;

/**
 * Real javac output for the byte-array patterns mods use on {@code Chunk.getBiomeArray()}.
 * Lives outside the transformer's skipped packages so it gets rewritten like a mod class.
 */
@SuppressWarnings("deprecation")
public final class BiomeArrayFixture {

    private BiomeArrayFixture() { }

    /* I2B immediately before BASTORE: the full int id survives to storeByte. */
    public static void fill(Chunk c, int id) {
        byte[] b = c.getBiomeArray();
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) id;
        }
    }

    /* Read-modify-write: BALOAD feeding BASTORE, byte to byte, no I2B. */
    public static void copyColumn(Chunk c) {
        byte[] b = c.getBiomeArray();
        for (int i = 1; i < 16; i++) {
            b[i] = b[0];
        }
    }

    /* I2B separated from BASTORE by a local store/load: the truncated byte reaches storeByte. */
    public static void viaLocal(Chunk c, int id) {
        byte[] b = c.getBiomeArray();
        byte v = (byte) id;
        b[3] = v;
    }

    /* Read only. */
    public static boolean isDesertAt(Chunk c, int i, int desertId) {
        return c.getBiomeArray()[i] == (byte) desertId;
    }

    /* The array escapes: the write inside Helper is not tracked and is lost. */
    public static void viaHelper(Chunk c, int id) {
        byte[] b = c.getBiomeArray();
        Helper.write(b, 5, id);
    }

    /* A BASTORE in the same class but untouched: its array is not a getBiomeArray() result. */
    public static void unrelated(byte[] other, int v) {
        other[0] = (byte) v;
    }

    public static final class Helper {

        public static void write(byte[] arr, int i, int v) {
            arr[i] = (byte) v;
        }

    }

}
