package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLPen;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_PenAxisEvent;
import org.lwjgl.sdl.SDL_PenButtonEvent;
import org.lwjgl.sdl.SDL_PenMotionEvent;
import org.lwjgl.sdl.SDL_PenProximityEvent;
import org.lwjgl.sdl.SDL_PenTouchEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Drawing tablets. No extra SDL subsystem as video already delivers the events.
 */
public final class Pens {

    private final List<PenListener> listeners = new CopyOnWriteArrayList<>();

    Pens() { }

    public PenType type(int pen) {
        return PenType.of(SDLPen.SDL_GetPenDeviceType(pen));
    }

    public Pens listen(PenListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
        return this;
    }

    public Pens mute(PenListener listener) {
        listeners.remove(listener);
        return this;
    }

    public void handle(SDL_Event event) {
        if (listeners.isEmpty()) {
            return;
        }
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_PEN_PROXIMITY_IN, SDLEvents.SDL_EVENT_PEN_PROXIMITY_OUT -> {
                SDL_PenProximityEvent proximity = event.pproximity();
                boolean in = type == SDLEvents.SDL_EVENT_PEN_PROXIMITY_IN;
                for (PenListener listener : listeners) {
                    listener.proximity(proximity.which(), in);
                }
            }
            case SDLEvents.SDL_EVENT_PEN_DOWN, SDLEvents.SDL_EVENT_PEN_UP -> {
                SDL_PenTouchEvent touch = event.ptouch();
                boolean down = type == SDLEvents.SDL_EVENT_PEN_DOWN;
                for (PenListener listener : listeners) {
                    if (down) {
                        listener.down(touch.which(), touch.x(), touch.y(), touch.eraser());
                    } else {
                        listener.up(touch.which(), touch.x(), touch.y(), touch.eraser());
                    }
                }
            }
            case SDLEvents.SDL_EVENT_PEN_MOTION -> {
                SDL_PenMotionEvent motion = event.pmotion();
                for (PenListener listener : listeners) {
                    listener.motion(motion.which(), motion.x(), motion.y());
                }
            }
            case SDLEvents.SDL_EVENT_PEN_BUTTON_DOWN, SDLEvents.SDL_EVENT_PEN_BUTTON_UP -> {
                SDL_PenButtonEvent button = event.pbutton();
                for (PenListener listener : listeners) {
                    listener.button(button.which(), button.x(), button.y(), button.button() & 0xFF, button.down());
                }
            }
            case SDLEvents.SDL_EVENT_PEN_AXIS -> {
                SDL_PenAxisEvent axis = event.paxis();
                PenAxis kind = PenAxis.of(axis.axis());
                if (kind == null) {
                    return;
                }
                for (PenListener listener : listeners) {
                    listener.axis(axis.which(), axis.x(), axis.y(), kind, axis.value());
                }
            }
            default -> { }
        }
    }

    void reset() {
        listeners.clear();
    }

}
