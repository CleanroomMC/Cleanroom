package com.cleanroommc.client.sdl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLLocale;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_Locale;

import java.util.ArrayList;
import java.util.List;

/** Preferred locales from SDL. */
public final class Locales {

    public record Preferred(String language, String country) { }

    public static List<Preferred> preferred() {
        PointerBuffer pointers = SDLLocale.SDL_GetPreferredLocales();
        if (pointers == null) {
            return List.of();
        }
        try {
            List<Preferred> locales = new ArrayList<>(pointers.remaining());
            while (pointers.hasRemaining()) {
                SDL_Locale locale = SDL_Locale.createSafe(pointers.get());
                if (locale == null) {
                    continue;
                }
                String language = locale.languageString();
                String country = locale.countryString();
                locales.add(new Preferred(language == null ? "" : language, country == null ? "" : country));
            }
            return List.copyOf(locales);
        } finally {
            SDLStdinc.SDL_free(pointers);
        }
    }

    private Locales() { }

}
