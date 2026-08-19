package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTouch;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_PinchFingerEvent;
import org.lwjgl.sdl.SDL_TouchFingerEvent;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Touchscreens and trackpads. No extra SDL subsystem as video already delivers the events.
 */
public final class Touches {

    private final List<TouchListener> listeners = new CopyOnWriteArrayList<>();

    Touches() { }

    public List<Touch> list() {
        LongBuffer ids = SDLTouch.SDL_GetTouchDevices();
        if (ids == null) {
            return List.of();
        }
        try {
            List<Touch> devices = new ArrayList<>(ids.remaining());
            while (ids.hasRemaining()) {
                devices.add(new Touch(ids.get()));
            }
            return List.copyOf(devices);
        } finally {
            SDLStdinc.SDL_free(ids);
        }
    }

    public Touches listen(TouchListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
        return this;
    }

    public Touches mute(TouchListener listener) {
        listeners.remove(listener);
        return this;
    }

    public void handle(SDL_Event event) {
        if (listeners.isEmpty()) {
            return;
        }
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_FINGER_DOWN -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                for (TouchListener listener : listeners) {
                    listener.down(finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.pressure());
                }
            }
            case SDLEvents.SDL_EVENT_FINGER_UP -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                for (TouchListener listener : listeners) {
                    listener.up(finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.pressure());
                }
            }
            case SDLEvents.SDL_EVENT_FINGER_MOTION -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                for (TouchListener listener : listeners) {
                    listener.motion(finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.dx(), finger.dy(),
                            finger.pressure());
                }
            }
            case SDLEvents.SDL_EVENT_FINGER_CANCELED -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                for (TouchListener listener : listeners) {
                    listener.canceled(finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.pressure());
                }
            }
            case SDLEvents.SDL_EVENT_PINCH_BEGIN -> {
                SDL_PinchFingerEvent pinch = event.pinch();
                for (TouchListener listener : listeners) {
                    listener.pinchBegin(pinch.scale());
                }
            }
            case SDLEvents.SDL_EVENT_PINCH_UPDATE -> {
                SDL_PinchFingerEvent pinch = event.pinch();
                for (TouchListener listener : listeners) {
                    listener.pinch(pinch.scale());
                }
            }
            case SDLEvents.SDL_EVENT_PINCH_END -> {
                SDL_PinchFingerEvent pinch = event.pinch();
                for (TouchListener listener : listeners) {
                    listener.pinchEnd(pinch.scale());
                }
            }
        }
    }

    void reset() {
        listeners.clear();
    }

}
