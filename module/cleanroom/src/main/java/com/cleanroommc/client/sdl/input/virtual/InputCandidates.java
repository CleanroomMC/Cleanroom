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
