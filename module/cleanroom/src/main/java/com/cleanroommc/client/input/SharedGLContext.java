package com.cleanroommc.client.input;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.sdl.SDLVideo;

/** A hidden SDL surface and GL context sharing objects with the main window. */
public final class SharedGLContext implements AutoCloseable {

    /** Creates a context sharing with the main context that is current on this thread. */
    public static SharedGLContext create(Window window) {
        if (window == null) {
            throw new IllegalStateException("The SDL window does not exist");
        }
        long surface = SDL.checkHandle(SDLVideo.SDL_CreateWindow("Cleanroom Shared Context", 1, 1,
                SDLVideo.SDL_WINDOW_OPENGL | SDLVideo.SDL_WINDOW_HIDDEN), "SDL_CreateWindow");
        long context = 0L;
        try {
            SDL.check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, 1),
                    "SDL_GL_SetAttribute(SDL_GL_SHARE_WITH_CURRENT_CONTEXT)");
            context = SDL.checkHandle(SDLVideo.SDL_GL_CreateContext(surface), "SDL_GL_CreateContext");
            window.makeCurrent();
            return new SharedGLContext(window, surface, context);
        } catch (RuntimeException failure) {
            if (context != 0L) {
                SDLVideo.SDL_GL_DestroyContext(context);
            }
            SDLVideo.SDL_DestroyWindow(surface);
            throw failure;
        } finally {
            SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, 0);
        }
    }

    private final Window window;
    private final long surface;
    private final long context;

    private GLCapabilities capabilities;
    private boolean closed;

    private SharedGLContext(Window window, long surface, long context) {
        this.window = window;
        this.surface = surface;
        this.context = context;
    }

    public void makeCurrent() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_GL_MakeCurrent(surface, context), "SDL_GL_MakeCurrent");
        if (this.capabilities == null) {
            this.capabilities = GL.createCapabilities();
        } else {
            GL.setCapabilities(this.capabilities);
        }
    }

    public void release() {
        if (this.closed) {
            return;
        }
        SDL.check(SDLVideo.SDL_GL_MakeCurrent(surface, 0L), "SDL_GL_MakeCurrent");
        GL.setCapabilities(null);
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (SDLVideo.SDL_GL_GetCurrentContext() == this.context) {
            SDLVideo.SDL_GL_MakeCurrent(this.surface, 0L);
            GL.setCapabilities(null);
        }
        SDLVideo.SDL_GL_DestroyContext(this.context);
        SDLVideo.SDL_DestroyWindow(this.surface);
        this.capabilities = null;
    }

    private void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("The shared GL context is closed");
        }
    }

}
