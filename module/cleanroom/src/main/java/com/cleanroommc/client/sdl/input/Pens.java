package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.PenEvent;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLPen;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_PenAxisEvent;
import org.lwjgl.sdl.SDL_PenButtonEvent;
import org.lwjgl.sdl.SDL_PenMotionEvent;
import org.lwjgl.sdl.SDL_PenProximityEvent;
import org.lwjgl.sdl.SDL_PenTouchEvent;

/**
 * Drawing tablets. No extra SDL subsystem as video already delivers the events.
 */
public final class Pens {

    Pens() { }

    public PenType type(int pen) {
        return PenType.of(SDLPen.SDL_GetPenDeviceType(pen));
    }

    public void handle(SDL_Event event) {
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_PEN_PROXIMITY_IN, SDLEvents.SDL_EVENT_PEN_PROXIMITY_OUT -> {
                SDL_PenProximityEvent proximity = event.pproximity();
                boolean in = type == SDLEvents.SDL_EVENT_PEN_PROXIMITY_IN;
                SDL.EVENT_BUS.post(new PenEvent.Proximity(proximity.which(), proximity.windowID(),
                        proximity.timestamp(), in));
            }
            case SDLEvents.SDL_EVENT_PEN_DOWN, SDLEvents.SDL_EVENT_PEN_UP -> {
                SDL_PenTouchEvent touch = event.ptouch();
                boolean down = type == SDLEvents.SDL_EVENT_PEN_DOWN;
                SDL.EVENT_BUS.post(down
                        ? new PenEvent.Down(touch.which(), touch.windowID(), touch.timestamp(), touch.x(), touch.y(),
                                touch.pen_state(), touch.eraser())
                        : new PenEvent.Up(touch.which(), touch.windowID(), touch.timestamp(), touch.x(), touch.y(),
                                touch.pen_state(), touch.eraser()));
            }
            case SDLEvents.SDL_EVENT_PEN_MOTION -> {
                SDL_PenMotionEvent motion = event.pmotion();
                SDL.EVENT_BUS.post(new PenEvent.Motion(motion.which(), motion.windowID(),
                        motion.timestamp(), motion.x(), motion.y(), motion.pen_state()));
            }
            case SDLEvents.SDL_EVENT_PEN_BUTTON_DOWN, SDLEvents.SDL_EVENT_PEN_BUTTON_UP -> {
                SDL_PenButtonEvent button = event.pbutton();
                SDL.EVENT_BUS.post(new PenEvent.Button(button.which(), button.windowID(),
                        button.timestamp(), button.x(), button.y(), button.pen_state(), button.button() & 0xFF,
                        button.down()));
            }
            case SDLEvents.SDL_EVENT_PEN_AXIS -> {
                SDL_PenAxisEvent axis = event.paxis();
                PenAxis kind = PenAxis.of(axis.axis());
                if (kind == null) {
                    return;
                }
                SDL.EVENT_BUS.post(new PenEvent.Axis(axis.which(), axis.windowID(), axis.timestamp(),
                        axis.x(), axis.y(), axis.pen_state(), kind, axis.value()));
            }
        }
    }

}
