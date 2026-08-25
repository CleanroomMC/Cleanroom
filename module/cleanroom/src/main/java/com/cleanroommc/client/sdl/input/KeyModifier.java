package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLKeycode;

/**
 * Modifier bits from {@code SDL_GetModState}. Combine with {@link #has(int)}.
 */
public final class KeyModifier {

    public static final int NONE = SDLKeycode.SDL_KMOD_NONE;
    public static final int LSHIFT = SDLKeycode.SDL_KMOD_LSHIFT;
    public static final int RSHIFT = SDLKeycode.SDL_KMOD_RSHIFT;
    public static final int SHIFT = SDLKeycode.SDL_KMOD_SHIFT;
    public static final int LCTRL = SDLKeycode.SDL_KMOD_LCTRL;
    public static final int RCTRL = SDLKeycode.SDL_KMOD_RCTRL;
    public static final int CTRL = SDLKeycode.SDL_KMOD_CTRL;
    public static final int LALT = SDLKeycode.SDL_KMOD_LALT;
    public static final int RALT = SDLKeycode.SDL_KMOD_RALT;
    public static final int ALT = SDLKeycode.SDL_KMOD_ALT;
    public static final int LGUI = SDLKeycode.SDL_KMOD_LGUI;
    public static final int RGUI = SDLKeycode.SDL_KMOD_RGUI;
    public static final int GUI = SDLKeycode.SDL_KMOD_GUI;
    public static final int NUM = SDLKeycode.SDL_KMOD_NUM;
    public static final int CAPS = SDLKeycode.SDL_KMOD_CAPS;
    public static final int MODE = SDLKeycode.SDL_KMOD_MODE;
    public static final int SCROLL = SDLKeycode.SDL_KMOD_SCROLL;

    public static KeyModifier none() {
        return new KeyModifier(NONE);
    }

    public static KeyModifier of(int bits) {
        return new KeyModifier(bits);
    }

    private final int bits;

    private KeyModifier(int bits) {
        this.bits = bits;
    }

    public int value() {
        return bits;
    }

    public boolean has(int mask) {
        return (bits & mask) != 0;
    }

    public boolean shift() {
        return has(SHIFT);
    }

    public boolean ctrl() {
        return has(CTRL);
    }

    public boolean alt() {
        return has(ALT);
    }

    public boolean gui() {
        return has(GUI);
    }

    public boolean capsLock() {
        return has(CAPS);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof KeyModifier mod && mod.bits == bits;
    }

    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public String toString() {
        return "KeyMod{0x" + Integer.toHexString(bits) + "}";
    }

}
