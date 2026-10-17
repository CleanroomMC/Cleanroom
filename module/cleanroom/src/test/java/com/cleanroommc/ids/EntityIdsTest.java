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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityIdsTest {

    @Test
    public void countsUpUntilTheCounterWrapsThenSkipsLiveIds() {
        // Before a wrap the counter is used as is (no world lookups)
        assertThat(EntityIds.next(41, id -> true)).isEqualTo(41);
        // Integer.MAX_VALUE + 1 overflowed: start over at 0, skipping ids live entities hold
        Set<Integer> live = Set.of(0, 1, 2, 5);
        int id = EntityIds.next(Integer.MIN_VALUE, live::contains);
        assertThat(id).isEqualTo(3);
        assertThat(EntityIds.next(id + 1, live::contains)).isEqualTo(4);
        assertThat(EntityIds.next(5, live::contains)).isEqualTo(6);
    }

}
