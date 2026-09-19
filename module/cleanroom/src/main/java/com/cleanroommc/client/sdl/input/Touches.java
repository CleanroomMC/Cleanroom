package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.TouchEvent;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTouch;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_PinchFingerEvent;
import org.lwjgl.sdl.SDL_TouchFingerEvent;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Touchscreens and trackpads. No extra SDL subsystem as video already delivers the events.
 */
public final class Touches {

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

    public Touch first() {
        List<Touch> touches = list();
        return touches.isEmpty() ? null : touches.getFirst();
    }

    public Touch byId(long id) {
        for (Touch touch : list()) {
            if (touch.id() == id) {
                return touch;
            }
        }
        return null;
    }

    void handle(SDL_Event event) {
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_FINGER_DOWN -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                SDL.events().post(new TouchEvent.Down(finger.windowID(), finger.timestamp(),
                        finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.pressure()));
            }
            case SDLEvents.SDL_EVENT_FINGER_UP -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                SDL.events().post(new TouchEvent.Up(finger.windowID(), finger.timestamp(),
                        finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.pressure()));
            }
            case SDLEvents.SDL_EVENT_FINGER_MOTION -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                SDL.events().post(new TouchEvent.Motion(finger.windowID(), finger.timestamp(),
                        finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.dx(), finger.dy(),
                        finger.pressure()));
            }
            case SDLEvents.SDL_EVENT_FINGER_CANCELED -> {
                SDL_TouchFingerEvent finger = event.tfinger();
                SDL.events().post(new TouchEvent.Canceled(finger.windowID(), finger.timestamp(),
                        finger.touchID(), finger.fingerID(), finger.x(), finger.y(), finger.pressure()));
            }
            case SDLEvents.SDL_EVENT_PINCH_BEGIN -> {
                SDL_PinchFingerEvent pinch = event.pinch();
                SDL.events().post(new TouchEvent.PinchBegin(pinch.windowID(), pinch.timestamp(), pinch.scale()));
            }
            case SDLEvents.SDL_EVENT_PINCH_UPDATE -> {
                SDL_PinchFingerEvent pinch = event.pinch();
                SDL.events().post(new TouchEvent.PinchUpdate(pinch.windowID(), pinch.timestamp(), pinch.scale()));
            }
            case SDLEvents.SDL_EVENT_PINCH_END -> {
                SDL_PinchFingerEvent pinch = event.pinch();
                SDL.events().post(new TouchEvent.PinchEnd(pinch.windowID(), pinch.timestamp(), pinch.scale()));
            }
        }
    }

}
