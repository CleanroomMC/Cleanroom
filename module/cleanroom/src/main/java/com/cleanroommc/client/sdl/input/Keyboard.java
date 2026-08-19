package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.Window;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.system.MemoryStack;

import java.nio.ShortBuffer;

/**
 * Physical and layout-aware key state for the host window.
 */
public final class Keyboard {

    Keyboard() { }

    /**
     * @return whether {@code scancode} is held on the host window
     */
    public boolean down(Scancode scancode) {
        if (scancode == null) {
            return false;
        }
        Window window = Window.main();
        return window != null && window.keyDown(scancode.value());
    }

    /**
     * @return whether the key that currently produces {@code keycode} is held
     */
    public boolean down(Keycode keycode) {
        if (keycode == null) {
            return false;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ShortBuffer unused = stack.mallocShort(1);
            int scancode = SDLKeyboard.SDL_GetScancodeFromKey(keycode.value(), unused);
            return Window.main() != null && Window.main().keyDown(scancode);
        }
    }

    /**
     * @return the current modifier bits
     */
    public KeyModifier mods() {
        if (Window.main() == null) {
            return KeyModifier.none();
        }
        return KeyModifier.of(SDLKeyboard.SDL_GetModState() & 0xFFFF);
    }

    /**
     * @return SDL's name for {@code scancode}, such as {@code "W"}
     */
    public String name(Scancode scancode) {
        if (scancode == null) {
            return "";
        }
        String name = SDLKeyboard.SDL_GetScancodeName(scancode.value());
        return name == null ? "" : name;
    }

    /**
     * @return SDL's name for {@code keycode}
     */
    public String name(Keycode keycode) {
        if (keycode == null) {
            return "";
        }
        String name = SDLKeyboard.SDL_GetKeyName(keycode.value());
        return name == null ? "" : name;
    }

    /**
     * Asks SDL to release every key. The host window also synthesizes key-ups on focus loss.
     */
    public void reset() {
        SDLKeyboard.SDL_ResetKeyboard();
    }

}
