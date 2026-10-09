/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.input.virtual;

import java.util.List;

/**
 * The candidate list for an input method.
 *
 * @param entries the candidates, in the input method's order
 * @param selected the index into {@code entries} the input method has highlighted, or -1 otherwise
 * @param horizontal whether the input method wants a horizontal or vertical list
 */
public record InputCandidates(List<String> entries, int selected, boolean horizontal) {

    public static final InputCandidates NONE = new InputCandidates(List.of(), -1, false);

    public boolean active() {
        return !this.entries.isEmpty();
    }

}
