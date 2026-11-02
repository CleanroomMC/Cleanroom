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

/**
 * In-progress text, which is not part of the text field's contents until it is committed.
 * Renderers draw it at the caret, underlined, with {@code cursor} and {@code selectionLength}
 * marking the clause that is being worked on.
 *
 * @param text the preedit string, empty when no composition is running
 * @param cursor the caret's offset into {@code text}, or -1 when there is nothing being composed
 * @param selectionLength how many characters after {@code cursor} the has been selected, or -1 otherwise
 */
public record TextComposition(String text, int cursor, int selectionLength) {

    public static final TextComposition NONE = new TextComposition("", -1, -1);

    public boolean active() {
        return !this.text.isEmpty();
    }

}
