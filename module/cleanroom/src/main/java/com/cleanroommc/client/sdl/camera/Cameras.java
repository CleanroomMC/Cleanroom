package com.cleanroommc.client.sdl.camera;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.CameraEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLCamera;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.sdl.SDL_CameraDeviceEvent;
import org.lwjgl.sdl.SDL_Event;

import java.nio.IntBuffer;
import java.util.List;

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

    public static void handle(SDL_Event event) {
        INSTANCE.dispatch(event);
    }

    public static void reset() {
        INSTANCE.clear();
    }

    private final Int2ObjectMap<Camera> cameras = new Int2ObjectArrayMap<>();

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
        long timestampNs = device.timestamp();
        switch (type) {
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_ADDED ->
                    SDL.EVENT_BUS.post(new CameraEvent.Added(timestampNs, remember(id)));
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_REMOVED -> {
                Camera camera = cameras.remove(id);
                if (camera != null) {
                    camera.close();
                }
                SDL.EVENT_BUS.post(new CameraEvent.Removed(id, timestampNs));
            }
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_APPROVED -> {
                Camera camera = cameras.get(id);
                if (camera != null) {
                    SDL.EVENT_BUS.post(new CameraEvent.Approved(timestampNs, camera));
                }
            }
            case SDLEvents.SDL_EVENT_CAMERA_DEVICE_DENIED ->
                    SDL.EVENT_BUS.post(new CameraEvent.Denied(id, timestampNs));
        }
    }

    synchronized void clear() {
        for (Camera camera : cameras.values()) {
            camera.close();
        }
        cameras.clear();
        started = false;
    }

    synchronized Camera injectAdded(int id) {
        started = true;
        Camera camera = remember(id);
        SDL.EVENT_BUS.post(new CameraEvent.Added(SDLTimer.SDL_GetTicksNS(), camera));
        return camera;
    }

    synchronized void injectRemoved(int id) {
        Camera camera = cameras.remove(id);
        if (camera != null) {
            camera.close();
        }
        SDL.EVENT_BUS.post(new CameraEvent.Removed(id, SDLTimer.SDL_GetTicksNS()));
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
