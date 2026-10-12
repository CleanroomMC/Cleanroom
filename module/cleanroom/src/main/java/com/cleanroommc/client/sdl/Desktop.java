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

import org.lwjgl.sdl.SDLMisc;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * Opens URLs and files with the platform handler ({@code SDL_OpenURL}).
 */
public final class Desktop {

    static final Desktop INSTANCE = new Desktop();

    private Desktop() { }

    public void open(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        open(uri.toString());
    }

    public void open(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        open(path.toAbsolutePath().toUri());
    }

    public void open(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        open(file.toPath());
    }

    public void open(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        SDL.check(SDLMisc.SDL_OpenURL(url), "SDL_OpenURL");
    }

}
