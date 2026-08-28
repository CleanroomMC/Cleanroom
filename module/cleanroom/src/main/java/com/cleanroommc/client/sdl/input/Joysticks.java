package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.Power;
import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.JoystickEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLJoystick;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_JoyBatteryEvent;
import org.lwjgl.sdl.SDL_JoyDeviceEvent;

import java.nio.IntBuffer;
import java.util.List;

/**
 * Connected raw joysticks. First use initializes {@code SDL_INIT_JOYSTICK}.
 */
public final class Joysticks {

    private final Int2ObjectMap<Joystick> sticks = new Int2ObjectArrayMap<>();

    private boolean started;

    Joysticks() { }

    synchronized void ensure() {
        if (started) {
            return;
        }
        SDL.ensureSubsystem(SDLInit.SDL_INIT_JOYSTICK);
        started = true;
        openConnected();
    }

    public synchronized List<Joystick> list() {
        ensure();
        return List.copyOf(sticks.values());
    }

    public Joystick first() {
        List<Joystick> list = list();
        return list.isEmpty() ? null : list.get(0);
    }

    public synchronized Joystick byId(int instanceId) {
        ensure();
        return sticks.get(instanceId);
    }

    public synchronized void handle(SDL_Event event) {
        if (!started) {
            return;
        }
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_JOYSTICK_ADDED -> {
                SDL_JoyDeviceEvent device = event.jdevice();
                Joystick stick = open(device.which());
                if (stick != null) {
                    SDL.EVENT_BUS.post(new JoystickEvent.Added(device.timestamp(), stick));
                }
            }
            case SDLEvents.SDL_EVENT_JOYSTICK_REMOVED -> {
                SDL_JoyDeviceEvent device = event.jdevice();
                int id = device.which();
                Joystick stick = sticks.remove(id);
                if (stick != null) {
                    SDLJoystick.SDL_CloseJoystick(stick.handle());
                }
                SDL.EVENT_BUS.post(new JoystickEvent.Removed(id, device.timestamp()));
            }
            case SDLEvents.SDL_EVENT_JOYSTICK_BATTERY_UPDATED -> {
                SDL_JoyBatteryEvent battery = event.jbattery();
                Joystick stick = sticks.get(battery.which());
                if (stick != null) {
                    Power.State state = Power.State.of(battery.state());
                    int percent = battery.percent();
                    SDL.EVENT_BUS.post(new JoystickEvent.Battery(battery.timestamp(), stick, state, percent));
                }
            }
        }
    }

    synchronized void reset() {
        for (Joystick stick : sticks.values()) {
            SDLJoystick.SDL_CloseJoystick(stick.handle());
        }
        sticks.clear();
        started = false;
    }

    private void openConnected() {
        IntBuffer ids = SDLJoystick.SDL_GetJoysticks();
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

    private Joystick open(int instanceId) {
        Joystick existing = sticks.get(instanceId);
        if (existing != null) {
            return existing;
        }
        long already = SDLJoystick.SDL_GetJoystickFromID(instanceId);
        long handle = already != 0L ? already : SDLJoystick.SDL_OpenJoystick(instanceId);
        if (handle == 0L) {
            return null;
        }
        Joystick stick = new Joystick(instanceId, handle);
        sticks.put(instanceId, stick);
        return stick;
    }

    synchronized Joystick injectAdded(int instanceId, long handle) {
        started = true;
        Joystick stick = new Joystick(instanceId, handle);
        sticks.put(instanceId, stick);
        SDL.EVENT_BUS.post(new JoystickEvent.Added(SDLTimer.SDL_GetTicksNS(), stick));
        return stick;
    }

    synchronized void injectRemoved(int instanceId) {
        sticks.remove(instanceId);
        SDL.EVENT_BUS.post(new JoystickEvent.Removed(instanceId, SDLTimer.SDL_GetTicksNS()));
    }

}
