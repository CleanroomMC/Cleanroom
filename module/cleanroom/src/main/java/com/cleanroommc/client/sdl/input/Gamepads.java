package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_GamepadDeviceEvent;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Connected, mapped gamepads. First use initializes {@code SDL_INIT_GAMEPAD}.
 */
public final class Gamepads {

    private final Int2ObjectMap<Gamepad> pads = new Int2ObjectArrayMap<>();
    private final List<GamepadListener> listeners = new CopyOnWriteArrayList<>();

    private boolean started;

    Gamepads() { }

    synchronized void ensure() {
        if (started) {
            return;
        }
        SDL.ensureSubsystem(SDLInit.SDL_INIT_GAMEPAD);
        started = true;
        openConnected();
    }

    public synchronized List<Gamepad> list() {
        ensure();
        return List.copyOf(pads.values());
    }

    public Gamepad first() {
        List<Gamepad> list = list();
        return list.isEmpty() ? null : list.get(0);
    }

    public synchronized Gamepad byId(int instanceId) {
        ensure();
        return pads.get(instanceId);
    }

    public Gamepads listen(GamepadListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
        return this;
    }

    public Gamepads mute(GamepadListener listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * Applies a gamepad device event from the window's pump.
     */
    public synchronized void handle(SDL_Event event) {
        if (!started) {
            return;
        }
        int type = event.type();
        SDL_GamepadDeviceEvent device = event.gdevice();
        int id = device.which();
        switch (type) {
            case SDLEvents.SDL_EVENT_GAMEPAD_ADDED -> {
                Gamepad pad = open(id);
                if (pad != null) {
                    for (GamepadListener listener : listeners) {
                        listener.added(pad);
                    }
                }
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_REMOVED -> {
                Gamepad pad = pads.remove(id);
                if (pad != null) {
                    SDLGamepad.SDL_CloseGamepad(pad.handle());
                }
                for (GamepadListener listener : listeners) {
                    listener.removed(id);
                }
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_REMAPPED -> {
                Gamepad pad = pads.get(id);
                if (pad != null) {
                    for (GamepadListener listener : listeners) {
                        listener.remapped(pad);
                    }
                }
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_DOWN,
                 SDLEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_UP,
                 SDLEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_MOTION -> {
                org.lwjgl.sdl.SDL_GamepadTouchpadEvent touch = event.gtouchpad();
                Gamepad pad = pads.get(touch.which());
                if (pad == null) {
                    return;
                }
                boolean down = type != SDLEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_UP;
                for (GamepadListener listener : listeners) {
                    listener.touchpad(pad, touch.touchpad(), touch.finger(), touch.x(), touch.y(), touch.pressure(), down);
                }
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_SENSOR_UPDATE -> {
                org.lwjgl.sdl.SDL_GamepadSensorEvent update = event.gsensor();
                Gamepad pad = pads.get(update.which());
                if (pad == null) {
                    return;
                }
                SensorType sensor = SensorType.of(update.sensor());
                float[] data = {update.data(0), update.data(1), update.data(2)};
                for (GamepadListener listener : listeners) {
                    listener.sensor(pad, sensor, data);
                }
            }
        }
    }

    synchronized void reset() {
        for (Gamepad pad : pads.values()) {
            SDLGamepad.SDL_CloseGamepad(pad.handle());
        }
        pads.clear();
        listeners.clear();
        started = false;
    }

    private void openConnected() {
        IntBuffer ids = SDLGamepad.SDL_GetGamepads();
        if (ids == null) {
            return;
        }
        try {
            while (ids.hasRemaining()) {
                open(ids.get());
            }
        } finally {
            SDLStdinc.SDL_free(ids);
        }
    }

    private Gamepad open(int instanceId) {
        Gamepad existing = pads.get(instanceId);
        if (existing != null) {
            return existing;
        }
        long already = SDLGamepad.SDL_GetGamepadFromID(instanceId);
        long handle = already != 0L ? already : SDLGamepad.SDL_OpenGamepad(instanceId);
        if (handle == 0L) {
            return null;
        }
        Gamepad pad = new Gamepad(instanceId, handle);
        pads.put(instanceId, pad);
        return pad;
    }

    /** Package-visible for tests that inject add/remove without SDL events. */
    synchronized Gamepad injectAdded(int instanceId, long handle) {
        started = true;
        Gamepad pad = new Gamepad(instanceId, handle);
        pads.put(instanceId, pad);
        List<GamepadListener> snapshot = new ArrayList<>(listeners);
        for (GamepadListener listener : snapshot) {
            listener.added(pad);
        }
        return pad;
    }

    synchronized void injectRemoved(int instanceId) {
        pads.remove(instanceId);
        for (GamepadListener listener : listeners) {
            listener.removed(instanceId);
        }
    }

}
