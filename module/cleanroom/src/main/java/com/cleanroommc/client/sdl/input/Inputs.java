package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDL_Event;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Process-wide input facade. Keyboard and mouse read the host {@link com.cleanroommc.client.sdl.Window};
 * gamepads, joysticks, haptics and sensors come up on first use.
 */
public final class Inputs {

    private static final Keyboard KEYBOARD = new Keyboard();
    private static final Mouse MOUSE = new Mouse();
    private static final Gamepads GAMEPADS = new Gamepads();
    private static final Joysticks JOYSTICKS = new Joysticks();
    private static final Haptics HAPTICS = new Haptics();
    private static final Sensors SENSORS = new Sensors();
    private static final Touches TOUCHES = new Touches();
    private static final Pens PENS = new Pens();
    private static final Map<SystemCursor, Cursor> SYSTEM_CURSORS = new EnumMap<>(SystemCursor.class);
    private static final Set<Cursor> CUSTOM_CURSORS = new HashSet<>();

    public static Keyboard keyboard() {
        return KEYBOARD;
    }

    public static Mouse mouse() {
        return MOUSE;
    }

    /**
     * Brings up SDL's gamepad subsystem the first time it is called.
     */
    public static Gamepads gamepads() {
        GAMEPADS.ensure();
        return GAMEPADS;
    }

    /**
     * Brings up SDL's joystick subsystem the first time it is called.
     */
    public static Joysticks joysticks() {
        JOYSTICKS.ensure();
        return JOYSTICKS;
    }

    /**
     * Brings up SDL's haptic subsystem the first time it is called.
     */
    public static Haptics haptics() {
        HAPTICS.ensure();
        return HAPTICS;
    }

    /**
     * Brings up SDL's sensor subsystem the first time it is called.
     */
    public static Sensors sensors() {
        SENSORS.ensure();
        return SENSORS;
    }

    /**
     * Touch devices. No extra subsystem; events flow once video is up.
     */
    public static Touches touches() {
        return TOUCHES;
    }

    /**
     * Drawing tablets. No extra subsystem; events flow once video is up.
     */
    public static Pens pens() {
        return PENS;
    }

    /**
     * Forwards device events if the matching subsystem was started (or if touch/pen listeners exist).
     */
    public static void dispatch(SDL_Event event) {
        GAMEPADS.handle(event);
        JOYSTICKS.handle(event);
        SENSORS.handle(event);
        TOUCHES.handle(event);
        PENS.handle(event);
    }

    /**
     * Forwards a gamepad device event if the gamepad subsystem was started.
     */
    public static void dispatchGamepad(SDL_Event event) {
        GAMEPADS.handle(event);
    }

    public static synchronized void reset() {
        GAMEPADS.reset();
        JOYSTICKS.reset();
        HAPTICS.reset();
        SENSORS.reset();
        TOUCHES.reset();
        PENS.reset();
        for (Cursor cursor : List.copyOf(SYSTEM_CURSORS.values())) {
            cursor.close();
        }
        SYSTEM_CURSORS.clear();
        for (Cursor cursor : Set.copyOf(CUSTOM_CURSORS)) {
            cursor.close();
        }
        CUSTOM_CURSORS.clear();
    }

    static synchronized Cursor systemCursor(SystemCursor kind) {
        return SYSTEM_CURSORS.computeIfAbsent(kind, Cursor::system);
    }

    static synchronized void track(Cursor cursor) {
        CUSTOM_CURSORS.add(cursor);
    }

    static synchronized void forget(Cursor cursor) {
        CUSTOM_CURSORS.remove(cursor);
        if (cursor.system()) {
            SYSTEM_CURSORS.values().removeIf(owned -> owned == cursor);
        }
    }

    private Inputs() { }

}
