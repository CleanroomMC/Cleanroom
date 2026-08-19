package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.Window;
import org.lwjgl.sdl.SDLMouse;

/**
 * Pointer position, buttons, grab and cursor for the host window.
 *
 * <p>Coordinates are SDL's, origin at the top-left. LWJGLY still flips Y for {@code org.lwjgl.input.Mouse}.
 */
public final class Mouse {

    Mouse() { }

    public float x() {
        Window window = Window.main();
        return window == null ? 0.0F : window.mouseX();
    }

    public float y() {
        Window window = Window.main();
        return window == null ? 0.0F : window.mouseY();
    }

    public boolean button(MouseButton button) {
        if (button == null) {
            return false;
        }
        Window window = Window.main();
        return window != null && window.mouseButtonDown(button.value());
    }

    public boolean grabbed() {
        Window window = Window.main();
        return window != null && window.mouseGrabbed();
    }

    public Mouse grabbed(boolean grab) {
        Window window = Window.main();
        if (window != null) {
            window.grabMouse(grab);
        }
        return this;
    }

    public Mouse position(float x, float y) {
        Window window = Window.main();
        if (window != null) {
            window.mousePosition(x, y);
        }
        return this;
    }

    public boolean visible() {
        return SDLMouse.SDL_CursorVisible();
    }

    public Mouse visible(boolean visible) {
        if (visible) {
            SDL.check(SDLMouse.SDL_ShowCursor(), "SDL_ShowCursor");
        } else {
            SDL.check(SDLMouse.SDL_HideCursor(), "SDL_HideCursor");
        }
        return this;
    }

    public Mouse cursor(SystemCursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor cannot be null");
        }
        Inputs.systemCursor(cursor).apply();
        return this;
    }

    public Mouse cursor(Cursor cursor) {
        if (cursor == null) {
            SDL.check(SDLMouse.SDL_SetCursor(SDLMouse.SDL_GetDefaultCursor()), "SDL_SetCursor");
            return this;
        }
        cursor.apply();
        return this;
    }

}
