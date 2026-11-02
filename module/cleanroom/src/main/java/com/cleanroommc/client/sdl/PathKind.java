/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLFileSystem;

/** What {@link FileSystem#info} found at a path. */
public enum PathKind {

    NONE(SDLFileSystem.SDL_PATHTYPE_NONE),
    FILE(SDLFileSystem.SDL_PATHTYPE_FILE),
    DIRECTORY(SDLFileSystem.SDL_PATHTYPE_DIRECTORY),
    OTHER(SDLFileSystem.SDL_PATHTYPE_OTHER);

    private final int value;

    PathKind(int value) {
        this.value = value;
    }

    public static PathKind of(int value) {
        return switch (value) {
            case SDLFileSystem.SDL_PATHTYPE_FILE -> FILE;
            case SDLFileSystem.SDL_PATHTYPE_DIRECTORY -> DIRECTORY;
            case SDLFileSystem.SDL_PATHTYPE_OTHER -> OTHER;
            default -> NONE;
        };
    }

    public int value() {
        return value;
    }

}
