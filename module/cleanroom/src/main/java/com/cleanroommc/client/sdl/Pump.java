package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.audio.AudioInternal;
import com.cleanroommc.client.sdl.camera.CameraInternal;
import com.cleanroommc.client.sdl.drop.DropInternal;
import com.cleanroommc.client.sdl.input.InputInternal;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;

/**
 * Drains SDL's process-wide event queue once per frame and hands each event to whoever owns it.
 */
final class Pump {

    static void pump(Window window) {
        window.beginPump();
        // The window's own handlers mutate state that its synchronized getters read
        synchronized (window) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                SDL_Event event = SDL_Event.malloc(stack);
                while (SDLEvents.SDL_PollEvent(event)) {
                    dispatch(window, event);
                }
            }
            Tray.pump();
            FileDialogs.pump();
            window.endPump();
        }
    }

    private static void dispatch(Window window, SDL_Event event) {
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_QUIT -> window.requestClose();
            case SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> window.key(event.key());
            case SDLEvents.SDL_EVENT_TEXT_INPUT -> window.text(event.text());
            case SDLEvents.SDL_EVENT_TEXT_EDITING -> window.editing(event.edit());
            case SDLEvents.SDL_EVENT_TEXT_EDITING_CANDIDATES -> window.editingCandidates(event.edit_candidates());
            case SDLEvents.SDL_EVENT_MOUSE_MOTION -> window.motion(event.motion());
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> window.button(event.button());
            case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> window.wheel(event.wheel());
            case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED,
                 SDLEvents.SDL_EVENT_WINDOW_RESIZED,
                 SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED,
                 SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED,
                 SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST,
                 SDLEvents.SDL_EVENT_WINDOW_MINIMIZED,
                 SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED,
                 SDLEvents.SDL_EVENT_WINDOW_RESTORED,
                 SDLEvents.SDL_EVENT_WINDOW_SHOWN,
                 SDLEvents.SDL_EVENT_WINDOW_HIDDEN,
                 SDLEvents.SDL_EVENT_WINDOW_MOUSE_ENTER,
                 SDLEvents.SDL_EVENT_WINDOW_MOUSE_LEAVE,
                 SDLEvents.SDL_EVENT_WINDOW_DISPLAY_CHANGED,
                 SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED,
                 SDLEvents.SDL_EVENT_WINDOW_ENTER_FULLSCREEN,
                 SDLEvents.SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> window.window(type, event.window());
            case SDLEvents.SDL_EVENT_CLIPBOARD_UPDATE -> Clipboard.updated();
            case SDLEvents.SDL_EVENT_SYSTEM_THEME_CHANGED -> SystemTheme.changed(event.common().timestamp());
            case SDLEvents.SDL_EVENT_DROP_FILE,
                 SDLEvents.SDL_EVENT_DROP_TEXT,
                 SDLEvents.SDL_EVENT_DROP_BEGIN,
                 SDLEvents.SDL_EVENT_DROP_COMPLETE,
                 SDLEvents.SDL_EVENT_DROP_POSITION -> DropInternal.dispatch(event);
            case SDLEvents.SDL_EVENT_DISPLAY_ADDED,
                 SDLEvents.SDL_EVENT_DISPLAY_REMOVED,
                 SDLEvents.SDL_EVENT_DISPLAY_MOVED,
                 SDLEvents.SDL_EVENT_DISPLAY_DESKTOP_MODE_CHANGED,
                 SDLEvents.SDL_EVENT_DISPLAY_CURRENT_MODE_CHANGED,
                 SDLEvents.SDL_EVENT_DISPLAY_CONTENT_SCALE_CHANGED -> { }
            case SDLEvents.SDL_EVENT_AUDIO_DEVICE_ADDED,
                 SDLEvents.SDL_EVENT_AUDIO_DEVICE_REMOVED,
                 SDLEvents.SDL_EVENT_AUDIO_DEVICE_FORMAT_CHANGED -> AudioInternal.dispatch(event);
            default -> {
                InputInternal.dispatch(event);
                CameraInternal.dispatch(event);
            }
        }
    }

    private Pump() { }

}
