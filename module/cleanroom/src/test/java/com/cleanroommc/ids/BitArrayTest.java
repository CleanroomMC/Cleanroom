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

import org.junit.jupiter.api.Test;

import net.minecraft.util.BitArray;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** The non-straddling backing layout round-trips every width against a naive reference. */
public class BitArrayTest {

    @Test
    public void roundTripsEveryWidth() {
        Random random = new Random(42);
        for (int bits = 1; bits <= 32; bits++) {
            int[] reference = new int[4096];
            BitArray array = new BitArray(bits, 4096);
            for (int i = 0; i < reference.length; i++) {
                int value = bits >= 31 ? random.nextInt() & Integer.MAX_VALUE : random.nextInt(1 << bits);
                reference[i] = value;
                array.setAt(i, value);
            }
            for (int i = 0; i < reference.length; i++) {
                assertThat(array.getAt(i)).isEqualTo(reference[i]);
            }
        }
    }

    /* Entries never span a long boundary: the backing length is ceil(size / (64 / bits)), not ceil(size * bits / 64). */
    @Test
    public void nonStraddlingLongCount() {
        assertThat(new BitArray(1, 4096).getBackingLongArray().length).isEqualTo(64);
        assertThat(new BitArray(4, 4096).getBackingLongArray().length).isEqualTo(256);
        assertThat(new BitArray(5, 4096).getBackingLongArray().length).isEqualTo(342); // 12 per long, old layout was 320
        assertThat(new BitArray(7, 4096).getBackingLongArray().length).isEqualTo(456); // 9 per long
        assertThat(new BitArray(13, 4096).getBackingLongArray().length).isEqualTo(1024); // 4 per long
        assertThat(new BitArray(32, 4096).getBackingLongArray().length).isEqualTo(2048);
    }

    @Test
    public void nonStraddlingCellLayout() {
        // entry i sits in long i / (64/bits) at bit offset (i % (64/bits)) * bits
        BitArray array = new BitArray(5, 4096);
        array.setAt(0, 31);
        array.setAt(1, 7);
        array.setAt(11, 30);
        array.setAt(12, 5); // first entry of the second long
        long[] longs = array.getBackingLongArray();
        assertThat(longs[0]).isEqualTo(31 | 7L << 5 | 30L << 55);
        assertThat(longs[1]).isEqualTo(5L);
    }

}
