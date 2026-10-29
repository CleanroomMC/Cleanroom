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

import com.cleanroommc.lwjgly.spi.WindowBridge;

import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.system.Platform;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_0;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_1;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_9;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_DIVIDE;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_MINUS;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_MULTIPLY;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_PERIOD;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_PLUS;
import static org.lwjgl.sdl.SDLKeycode.SDLK_SCANCODE_MASK;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_CTRL;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_GUI;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_LALT;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_MODE;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_NUM;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_RALT;

/** The destructive LWJGL 2 keyboard and mouse queues backed by SDL events. */
final class LegacyInputQueues {

    private static final int CAPACITY = 1024;

    private final Deque<PendingKey> keys = new ArrayDeque<>();
    private final Deque<WindowBridge.MouseEvent> mice = new ArrayDeque<>();

    private PendingKey pendingCharacter;
    private double deltaX;
    private double deltaY;
    private double wheel;

    /**
     * Resolves the character LWJGL 2 reported for a key press.
     *
     * <p>Shortcuts are resolved even with text input on, as TEXT_INPUT never carries them.
     * Ctrl gives the ASCII control character, which mods check before clipboard shortcuts.
     * Otherwise the current layout is used, only while text input is off.
     * Dead keys and compose sequences need text input, so they type nothing here.
     *
     * @return the character, or {@code 0} for keys that type nothing
     */
    static char character(int scancode, short mod, boolean textInput) {
        boolean mac = Platform.get() == Platform.MACOSX;
        boolean altGr = (mod & (SDL_KMOD_RALT | SDL_KMOD_MODE)) != 0;
        if ((mod & SDL_KMOD_CTRL) != 0 && !altGr) {
            // Key event keycodes map non-Latin layouts to Latin letters, like Windows does for Ctrl
            int key = SDLKeyboard.SDL_GetKeyFromScancode(scancode, (short) 0, true);
            return key >= 'a' && key <= 'z' || key >= '@' && key <= '_' ? (char) (key & 0x1F) : 0;
        }
        if ((mod & SDL_KMOD_GUI) != 0) {
            return mac ? printable(SDLKeyboard.SDL_GetKeyFromScancode(scancode, mod, false)) : 0;
        }
        if (textInput || (mod & SDL_KMOD_LALT) != 0 && !altGr && !mac) {
            return 0;
        }
        int key = SDLKeyboard.SDL_GetKeyFromScancode(scancode, mod, false);
        if ((key & SDLK_SCANCODE_MASK) != 0) {
            return keypad(key, (mod & SDL_KMOD_NUM) != 0);
        }
        return printable(key);
    }

    private static <T> void append(Deque<T> queue, T event) {
        if (queue.size() == CAPACITY) {
            queue.removeFirst();
        }
        queue.addLast(event);
    }

    private static char printable(int key) {
        return key < ' ' || key == 0x7F || key > Character.MAX_VALUE ? 0 : (char) key;
    }

    private static char keypad(int key, boolean numLock) {
        return switch (key) {
            case SDLK_KP_DIVIDE -> '/';
            case SDLK_KP_MULTIPLY -> '*';
            case SDLK_KP_MINUS -> '-';
            case SDLK_KP_PLUS -> '+';
            case SDLK_KP_0 -> numLock ? '0' : 0;
            case SDLK_KP_PERIOD -> numLock ? '.' : 0;
            default -> numLock && key >= SDLK_KP_1 && key <= SDLK_KP_9 ? (char) ('1' + key - SDLK_KP_1) : 0;
        };
    }

    synchronized void key(int scancode, int key, boolean pressed, boolean repeat, char character, long timestampNs, short mods) {
        this.pendingCharacter = null;
        PendingKey event = new PendingKey(scancode, key, pressed, repeat, timestampNs, mods);
        event.character = character;
        append(this.keys, event);
        if (pressed) {
            this.pendingCharacter = event;
        }
    }

    synchronized void text(String text, long timestampNs) {
        if (text == null) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (this.pendingCharacter != null && this.pendingCharacter.character == 0) {
                this.pendingCharacter.character = character;
                this.pendingCharacter = null;
            } else {
                PendingKey event = new PendingKey(0, 0, true, false, timestampNs, (short) 0);
                event.character = character;
                append(this.keys, event);
            }
        }
    }

    synchronized void motion(float x, float y, float dx, float dy, long timestampNs) {
        this.deltaX += dx;
        this.deltaY += dy;
        append(this.mice, new WindowBridge.MouseEvent(0, false, x, y, dx, dy, 0.0F, timestampNs));
    }

    synchronized void button(int button, boolean pressed, float x, float y, long timestampNs) {
        append(this.mice, new WindowBridge.MouseEvent(button, pressed, x, y, 0.0F, 0.0F, 0.0F, timestampNs));
    }

    synchronized void wheel(float x, float y, float amount, long timestampNs) {
        this.wheel += amount;
        append(this.mice, new WindowBridge.MouseEvent(0, false, x, y, 0.0F, 0.0F, amount, timestampNs));
    }

    synchronized WindowBridge.KeyEvent nextKey() {
        PendingKey event = keys.pollFirst();
        if (event == null) {
            return null;
        }
        if (event == this.pendingCharacter) {
            this.pendingCharacter = null;
        }
        return event.freeze();
    }

    synchronized int queuedKeys() {
        return this.keys.size();
    }

    synchronized WindowBridge.MouseEvent nextMouse() {
        return this.mice.pollFirst();
    }

    synchronized int queuedMice() {
        return this.mice.size();
    }

    synchronized void resetDeltas() {
        this.deltaX = 0.0;
        this.deltaY = 0.0;
    }

    synchronized float takeDeltaX() {
        float value = (float) this.deltaX;
        this.deltaX = 0.0;
        return value;
    }

    synchronized float takeDeltaY() {
        float value = (float) this.deltaY;
        this.deltaY = 0.0;
        return value;
    }

    synchronized float takeWheel() {
        float value = (float) this.wheel;
        this.wheel = 0.0;
        return value;
    }

    private static final class PendingKey {

        private final int scancode;
        private final int key;
        private final boolean pressed;
        private final boolean repeat;
        private final long timestampNs;
        private final short mods;

        private char character;

        private PendingKey(int scancode, int key, boolean pressed, boolean repeat, long timestampNs, short mods) {
            this.scancode = scancode;
            this.key = key;
            this.pressed = pressed;
            this.repeat = repeat;
            this.timestampNs = timestampNs;
            this.mods = mods;
        }

        private WindowBridge.KeyEvent freeze() {
            return new WindowBridge.KeyEvent(scancode, key, pressed, repeat, character, timestampNs, mods);
        }

    }

}
