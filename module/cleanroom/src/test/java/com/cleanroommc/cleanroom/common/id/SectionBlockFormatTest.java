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

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionBlockFormatTest {

    @Test
    public void bitsPerIndex() {
        assertThat(SectionBlockFormat.bitsFor(2)).isEqualTo(4);
        assertThat(SectionBlockFormat.bitsFor(16)).isEqualTo(4);
        assertThat(SectionBlockFormat.bitsFor(17)).isEqualTo(5);
        assertThat(SectionBlockFormat.bitsFor(4096)).isEqualTo(12);
        assertThat(SectionBlockFormat.bitsFor(4097)).isEqualTo(13);
    }

    @Test
    public void longsDontSplitIndices() {
        assertThat(SectionBlockFormat.longCount(4)).isEqualTo(256); // 16 per long
        assertThat(SectionBlockFormat.longCount(5)).isEqualTo(342); // 12 per long, 4 bits unused
        assertThat(SectionBlockFormat.longCount(7)).isEqualTo(456); // 9 per long
        assertThat(SectionBlockFormat.longCount(16)).isEqualTo(1024); // 4 per long
    }

    @Test
    public void packRoundTripsEveryWidth() {
        Random random = new Random(42);
        for (int bits = 4; bits <= 20; bits++) {
            int[] indices = new int[4096];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = random.nextInt(1 << bits);
            }
            indices[0] = (1 << bits) - 1; // the largest value, every bit set
            long[] packed = SectionBlockFormat.pack(indices, bits);
            assertThat(packed.length).isEqualTo(SectionBlockFormat.longCount(bits));
            assertThat(SectionBlockFormat.unpack(packed, bits)).isEqualTo(indices);
        }
    }

}
