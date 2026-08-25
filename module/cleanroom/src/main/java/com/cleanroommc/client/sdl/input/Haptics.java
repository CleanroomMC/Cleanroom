package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLHaptic;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Force-feedback devices. First use initializes {@code SDL_INIT_HAPTIC}.
 */
public final class Haptics {

    private final Int2ObjectMap<Haptic> devices = new Int2ObjectArrayMap<>();

    private boolean started;

    Haptics() { }

    synchronized void ensure() {
        if (started) {
            return;
        }
        SDL.ensureSubsystem(SDLInit.SDL_INIT_HAPTIC);
        started = true;
    }

    public synchronized List<Haptic> list() {
        ensure();
        IntBuffer ids = SDLHaptic.SDL_GetHaptics();
        if (ids == null) {
            return List.copyOf(devices.values());
        }
        try {
            while (ids.hasRemaining()) {
                open(ids.get());
            }
        } finally {
            SDLStdinc.SDL_free(ids);
        }
        return List.copyOf(devices.values());
    }

    public Haptic first() {
        List<Haptic> list = list();
        return list.isEmpty() ? null : list.get(0);
    }

    public synchronized Haptic byId(int id) {
        ensure();
        Haptic existing = devices.get(id);
        return existing != null ? existing : open(id);
    }

    /**
     * Opens the haptic side of a joystick, if it has one.
     */
    public synchronized Haptic open(Joystick joystick) {
        if (joystick == null) {
            throw new IllegalArgumentException("Joystick cannot be null");
        }
        ensure();
        if (!SDLHaptic.SDL_IsJoystickHaptic(joystick.handle())) {
            return null;
        }
        long handle = SDLHaptic.SDL_OpenHapticFromJoystick(joystick.handle());
        if (handle == 0L) {
            return null;
        }
        return track(handle);
    }

    public Haptic mouse() {
        ensure();
        if (!SDLHaptic.SDL_IsMouseHaptic()) {
            return null;
        }
        long handle = SDLHaptic.SDL_OpenHapticFromMouse();
        if (handle == 0L) {
            return null;
        }
        return track(handle);
    }

    synchronized void forget(Haptic haptic) {
        devices.values().removeIf(owned -> owned == haptic);
    }

    synchronized void reset() {
        for (Haptic haptic : new ArrayList<>(devices.values())) {
            haptic.close();
        }
        devices.clear();
        started = false;
    }

    private Haptic open(int id) {
        Haptic existing = devices.get(id);
        if (existing != null) {
            return existing;
        }
        long already = SDLHaptic.SDL_GetHapticFromID(id);
        long handle = already != 0L ? already : SDLHaptic.SDL_OpenHaptic(id);
        if (handle == 0L) {
            return null;
        }
        return track(handle);
    }

    private Haptic track(long handle) {
        int id = SDLHaptic.SDL_GetHapticID(handle);
        Haptic existing = devices.get(id);
        if (existing != null) {
            return existing;
        }
        Haptic haptic = new Haptic(id, handle);
        devices.put(id, haptic);
        return haptic;
    }

}
