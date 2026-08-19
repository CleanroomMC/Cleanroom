package com.cleanroommc.client.sdl.camera;

import com.cleanroommc.client.sdl.SDLException;
import com.cleanroommc.client.sdl.Surfaces;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLCamera;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_CameraSpec;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * One camera. {@link #open} acquires the device, {@link #acquire} copies the latest frame into a Java image.
 */
public final class Camera implements AutoCloseable {

    private static CameraSpec fromNative(SDL_CameraSpec spec) {
        return new CameraSpec(spec.width(), spec.height(), spec.format(), spec.framerate_numerator(), spec.framerate_denominator());
    }

    /**
     * @param timestampNs SDL's timestamp, or {@code 0} if unknown
     */
    public record Frame(int width, int height, int format, long timestampNs, BufferedImage image) { }

    private final int id;

    private long handle;

    Camera(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public long handle() {
        return handle;
    }

    public String name() {
        String name = SDLCamera.SDL_GetCameraName(id);
        return name == null ? "" : name;
    }

    public CameraPosition position() {
        return CameraPosition.of(SDLCamera.SDL_GetCameraPosition(id));
    }

    public List<CameraSpec> formats() {
        PointerBuffer pointers = SDLCamera.SDL_GetCameraSupportedFormats(id);
        if (pointers == null) {
            return List.of();
        }
        try {
            List<CameraSpec> specs = new ArrayList<>(pointers.remaining());
            while (pointers.hasRemaining()) {
                SDL_CameraSpec spec = SDL_CameraSpec.createSafe(pointers.get());
                if (spec != null) {
                    specs.add(fromNative(spec));
                }
            }
            return List.copyOf(specs);
        } finally {
            SDLStdinc.SDL_free(pointers);
        }
    }

    public boolean opened() {
        return handle != 0L;
    }

    public Camera open() {
        return open(null);
    }

    public Camera open(CameraSpec spec) {
        if (handle != 0L) {
            return this;
        }
        if (spec == null) {
            handle = SDLCamera.SDL_OpenCamera(id, null);
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                SDL_CameraSpec nativeSpec = SDL_CameraSpec.calloc(stack)
                        .format(spec.format())
                        .width(spec.width())
                        .height(spec.height())
                        .framerate_numerator(spec.fpsNumerator())
                        .framerate_denominator(spec.fpsDenominator());
                handle = SDLCamera.SDL_OpenCamera(id, nativeSpec);
            }
        }
        if (handle == 0L) {
            throw new SDLException("SDL_OpenCamera failed: " + org.lwjgl.sdl.SDLError.SDL_GetError());
        }
        return this;
    }

    public CameraPermission permission() {
        if (handle == 0L) {
            return CameraPermission.PENDING;
        }
        return CameraPermission.of(SDLCamera.SDL_GetCameraPermissionState(handle));
    }

    public CameraSpec spec() {
        if (handle == 0L) {
            return null;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_CameraSpec nativeSpec = SDL_CameraSpec.calloc(stack);
            if (!SDLCamera.SDL_GetCameraFormat(handle, nativeSpec)) {
                return null;
            }
            return fromNative(nativeSpec);
        }
    }

    /**
     * @return the latest frame, or {@code null} if none is ready
     */
    public Frame acquire() {
        if (handle == 0L) {
            throw new IllegalStateException("The camera is not open");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer timestamp = stack.mallocLong(1);
            SDL_Surface surface = SDLCamera.SDL_AcquireCameraFrame(handle, timestamp);
            if (surface == null) {
                return null;
            }
            try {
                return new Frame(surface.w(), surface.h(), surface.format(), timestamp.get(0), Surfaces.toImage(surface));
            } finally {
                SDLCamera.SDL_ReleaseCameraFrame(handle, surface);
            }
        }
    }

    @Override
    public void close() {
        if (handle == 0L) {
            return;
        }
        SDLCamera.SDL_CloseCamera(handle);
        handle = 0L;
    }

}
