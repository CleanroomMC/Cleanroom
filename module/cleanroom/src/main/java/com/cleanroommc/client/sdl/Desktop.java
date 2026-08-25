package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLMisc;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * Opens URLs and files with the platform handler ({@code SDL_OpenURL}).
 */
public final class Desktop {

    public static void open(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        open(uri.toString());
    }

    public static void open(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        open(path.toAbsolutePath().toUri());
    }

    public static void open(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        open(file.toPath());
    }

    public static void open(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        SDL.check(SDLMisc.SDL_OpenURL(url), "SDL_OpenURL");
    }

    private Desktop() { }

}
