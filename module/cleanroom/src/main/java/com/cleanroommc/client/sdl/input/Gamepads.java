package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.GamepadEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_GamepadDeviceEvent;

import java.nio.IntBuffer;
import java.util.List;

/**
 * Connected, mapped gamepads. First use initializes {@code SDL_INIT_GAMEPAD}.
 */
public final class Gamepads {

    private final Int2ObjectMap<Gamepad> pads = new Int2ObjectArrayMap<>();

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
        long timestampNs = device.timestamp();
        switch (type) {
            case SDLEvents.SDL_EVENT_GAMEPAD_ADDED -> {
                Gamepad pad = open(id);
                if (pad != null) {
                    SDL.EVENT_BUS.post(new GamepadEvent.Added(timestampNs, pad));
                }
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_REMOVED -> {
                Gamepad pad = pads.remove(id);
                if (pad != null) {
                    SDLGamepad.SDL_CloseGamepad(pad.handle());
                }
                SDL.EVENT_BUS.post(new GamepadEvent.Removed(id, timestampNs));
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_REMAPPED -> {
                Gamepad pad = pads.get(id);
                if (pad != null) {
                    SDL.EVENT_BUS.post(new GamepadEvent.Remapped(timestampNs, pad));
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
                SDL.EVENT_BUS.post(new GamepadEvent.Touchpad(touch.timestamp(), pad, touch.touchpad(),
                        touch.finger(), touch.x(), touch.y(), touch.pressure(), down));
            }
            case SDLEvents.SDL_EVENT_GAMEPAD_SENSOR_UPDATE -> {
                org.lwjgl.sdl.SDL_GamepadSensorEvent update = event.gsensor();
                Gamepad pad = pads.get(update.which());
                if (pad == null) {
                    return;
                }
                SensorType sensor = SensorType.of(update.sensor());
                float[] data = {update.data(0), update.data(1), update.data(2)};
                SDL.EVENT_BUS.post(new GamepadEvent.Sensor(update.timestamp(), pad, sensor, data,
                        update.sensor_timestamp()));
            }
        }
    }

    synchronized void reset() {
        for (Gamepad pad : pads.values()) {
            SDLGamepad.SDL_CloseGamepad(pad.handle());
        }
        pads.clear();
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
        SDL.EVENT_BUS.post(new GamepadEvent.Added(SDLTimer.SDL_GetTicksNS(), pad));
        return pad;
    }

    synchronized void injectRemoved(int instanceId) {
        pads.remove(instanceId);
        SDL.EVENT_BUS.post(new GamepadEvent.Removed(instanceId, SDLTimer.SDL_GetTicksNS()));
    }

}
