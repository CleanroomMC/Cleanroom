package com.cleanroommc.client.sdl.camera;

import com.cleanroommc.client.sdl.SDL;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLCamera;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_CameraDeviceEvent;
import org.lwjgl.sdl.SDL_Event;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Connected cameras. First use initializes {@code SDL_INIT_CAMERA}.
 */
public final class Cameras {

    private static final Cameras INSTANCE = new Cameras();

    public static Cameras instance() {
        return INSTANCE;
    }

    public static List<Camera> list() {
        return INSTANCE.devices();
    }

    public static Camera first() {
        List<Camera> cameras = list();
        return cameras.isEmpty() ? null : cameras.getFirst();
    }

    public static Camera byId(int id) {
        INSTANCE.ensure();
        return INSTANCE.cameras.get(id);
    }

    public static void listen(CameraListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        INSTANCE.listeners.add(listener);
    }

    public static void mute(CameraListener listener) {
        INSTANCE.listeners.remove(listener);
    }

    public static void handle(SDL_Event event) {
        INSTANCE.dispatch(event);
    }

    public static void reset() {
        INSTANCE.clear();
    }

    private final Int2ObjectMap<Camera> cameras = new Int2ObjectArrayMap<>();
    private final List<CameraListener> listeners = new CopyOnWriteArrayList<>();

    private boolean started;

    private Cameras() { }

    synchronized List<Camera> devices() {
        ensure();
        return List.copyOf(cameras.values());
    }

    synchronized void ensure() {
        if (started) {
            return;
        }
        SDL.ensureSubsystem(SDLInit.SDL_INIT_CAMERA);
        started = true;
        openConnected();
    }

    synchronized void dispatch(SDL_Event event) {
        if (!started) {
            return;
        }
        int type = event.type();
        SDL_CameraDeviceEvent device = event.cdevice();
        int id = device.which();
        switch (type) {
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_ADDED -> {
                Camera camera = remember(id);
                for (CameraListener listener : listeners) {
                    listener.added(camera);
                }
            }
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_REMOVED -> {
                Camera camera = cameras.remove(id);
                if (camera != null) {
                    camera.close();
                }
                for (CameraListener listener : listeners) {
                    listener.removed(id);
                }
            }
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_APPROVED -> {
                Camera camera = cameras.get(id);
                if (camera != null) {
                    for (CameraListener listener : listeners) {
                        listener.approved(camera);
                    }
                }
            }
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_DENIED -> {
                for (CameraListener listener : listeners) {
                    listener.denied(id);
                }
            }
        }
    }

    synchronized void clear() {
        for (Camera camera : cameras.values()) {
            camera.close();
        }
        cameras.clear();
        listeners.clear();
        started = false;
    }

    synchronized Camera injectAdded(int id) {
        started = true;
        Camera camera = remember(id);
        List<CameraListener> snapshot = new ArrayList<>(listeners);
        for (CameraListener listener : snapshot) {
            listener.added(camera);
        }
        return camera;
    }

    synchronized void injectRemoved(int id) {
        Camera camera = cameras.remove(id);
        if (camera != null) {
            camera.close();
        }
        for (CameraListener listener : listeners) {
            listener.removed(id);
        }
    }

    private void openConnected() {
        IntBuffer ids = SDLCamera.SDL_GetCameras();
        if (ids == null) {
            return;
        }
        try {
            while (ids.hasRemaining()) {
                remember(ids.get());
            }
        } finally {
            SDLStdinc.SDL_free(ids);
        }
    }

    private Camera remember(int id) {
        return cameras.computeIfAbsent(id, Camera::new);
    }

}
