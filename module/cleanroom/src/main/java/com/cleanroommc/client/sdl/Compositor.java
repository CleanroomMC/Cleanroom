package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLVideo;

/**
 * The platform window system that owns {@link NativeWindow#handle()}.
 */
public enum Compositor {

    WIN32(SDLVideo.SDL_PROP_WINDOW_WIN32_HWND_POINTER, true),
    WAYLAND(SDLVideo.SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER, true),
    X11(SDLVideo.SDL_PROP_WINDOW_X11_WINDOW_NUMBER, false),
    COCOA(SDLVideo.SDL_PROP_WINDOW_COCOA_WINDOW_POINTER, true),
    ANDROID(SDLVideo.SDL_PROP_WINDOW_ANDROID_WINDOW_POINTER, true),
    UIKIT(SDLVideo.SDL_PROP_WINDOW_UIKIT_WINDOW_POINTER, true),
    VIVANTE(SDLVideo.SDL_PROP_WINDOW_VIVANTE_WINDOW_POINTER, true),
    UNKNOWN("", false);

    private final String property;
    private final boolean pointer;

    Compositor(String property, boolean pointer) {
        this.property = property;
        this.pointer = pointer;
    }

    public String property() {
        return property;
    }

    public boolean pointer() {
        return pointer;
    }

}
