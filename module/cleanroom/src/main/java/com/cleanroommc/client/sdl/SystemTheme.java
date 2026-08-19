package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLVideo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Light/dark preference reported by the desktop environment. Requires the video subsystem. */
public enum SystemTheme {

    UNKNOWN(SDLVideo.SDL_SYSTEM_THEME_UNKNOWN),
    LIGHT(SDLVideo.SDL_SYSTEM_THEME_LIGHT),
    DARK(SDLVideo.SDL_SYSTEM_THEME_DARK);

    /** Notified from the window pump when the desktop theme changes. */
    public interface Listener {

        void themeChanged(SystemTheme theme);

    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    public static void listen(Listener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void mute(Listener listener) {
        LISTENERS.remove(listener);
    }

    /** Called from the window pump when {@code SDL_EVENT_SYSTEM_THEME_CHANGED} arrives. */
    public static void changed() {
        SystemTheme theme = current();
        for (Listener listener : LISTENERS) {
            listener.themeChanged(theme);
        }
    }

    /** @return the current desktop theme, or {@link #UNKNOWN} when the platform does not report one */
    public static SystemTheme current() {
        SDL.ensureVideo();
        int theme = SDLVideo.SDL_GetSystemTheme();
        for (SystemTheme candidate : values()) {
            if (candidate.value == theme) {
                return candidate;
            }
        }
        return UNKNOWN;
    }

    private final int value;

    SystemTheme(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public boolean dark() {
        return this == DARK;
    }

}
