package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDL_Event;

/**
 * Holds the process-wide input devices and fans SDL events out to them.
 *
 * <p>Internal. Reach these devices through {@link SDL} instead.
 */
public final class InputInternal {

    private static final Keyboard KEYBOARD = new Keyboard();
    private static final Mouse MOUSE = new Mouse();
    private static final Gamepads GAMEPADS = new Gamepads();
    private static final Joysticks JOYSTICKS = new Joysticks();
    private static final Haptics HAPTICS = new Haptics();
    private static final Sensors SENSORS = new Sensors();
    private static final Touches TOUCHES = new Touches();
    private static final Pens PENS = new Pens();

    public static Keyboard keyboard() {
        return KEYBOARD;
    }

    public static Mouse mouse() {
        return MOUSE;
    }

    public static Gamepads gamepads() {
        GAMEPADS.ensure();
        return GAMEPADS;
    }

    public static Joysticks joysticks() {
        JOYSTICKS.ensure();
        return JOYSTICKS;
    }

    public static Haptics haptics() {
        HAPTICS.ensure();
        return HAPTICS;
    }

    public static Sensors sensors() {
        SENSORS.ensure();
        return SENSORS;
    }

    public static Touches touches() {
        return TOUCHES;
    }

    public static Pens pens() {
        return PENS;
    }

    public static void dispatch(SDL_Event event) {
        GAMEPADS.handle(event);
        JOYSTICKS.handle(event);
        SENSORS.handle(event);
        TOUCHES.handle(event);
        PENS.handle(event);
    }

    public static synchronized void reset() {
        GAMEPADS.reset();
        JOYSTICKS.reset();
        HAPTICS.reset();
        SENSORS.reset();
        PENS.reset();
        Cursor.closeAll();
    }

    private InputInternal() { }

}
