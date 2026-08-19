package com.cleanroommc.client.sdl;

import com.cleanroommc.lwjgly.spi.WindowBridge;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_KeyboardEvent;
import org.lwjgl.sdl.SDL_MouseButtonEvent;
import org.lwjgl.sdl.SDL_MouseMotionEvent;
import org.lwjgl.sdl.SDL_MouseWheelEvent;
import org.lwjgl.sdl.SDL_TextInputEvent;
import org.lwjgl.sdl.SDL_WindowEvent;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

/** The single SDL window, OpenGL context and event pump owned by Cleanroom. */
public final class Window implements AutoCloseable {

    private static final int MAX_SCANCODES = 512;
    private static final int MAX_MOUSE_BUTTONS = 8;

    private static volatile Window main;

    private final long handle;
    private final long glContext;
    private final int id;
    private final LegacyInputQueues legacy = new LegacyInputQueues();
    private final boolean[] keys = new boolean[MAX_SCANCODES];
    private final boolean[] buttons = new boolean[MAX_MOUSE_BUTTONS + 1];

    private String title;
    private int width;
    private int height;
    private int pixelWidth;
    private int pixelHeight;
    private float mouseX;
    private float mouseY;
    private boolean focused = true;
    private boolean closeRequested;
    private boolean resized;
    private boolean fullscreen;
    private boolean borderless;
    private boolean minimized;
    private boolean mouseGrabbed;
    private boolean mouseWarped;

    private volatile boolean closed;

    private Window(long handle, long glContext, String title, int width, int height) {
        this.handle = handle;
        this.glContext = glContext;
        this.id = SDLVideo.SDL_GetWindowID(handle);
        this.title = title;
        this.width = width;
        this.height = height;
        this.pixelWidth = width;
        this.pixelHeight = height;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Window main() {
        return main;
    }

    public static void closeMain() {
        Window current = main;
        if (current != null) {
            current.close();
        }
    }

    public long handle() {
        this.ensureOpen();
        return handle;
    }


    /**
     * Returns the platform window handle rather than SDL's opaque window pointer.
     * TODO: Make compositors into an enum
     */
    public long nativeHandle() {
        this.ensureOpen();
        int properties = SDLVideo.SDL_GetWindowProperties(handle);
        if (properties == 0) {
            return 0L;
        }
        long hwnd = SDLProperties.SDL_GetPointerProperty(properties, SDLVideo.SDL_PROP_WINDOW_WIN32_HWND_POINTER, 0L);
        if (hwnd != 0L) {
            return hwnd;
        }
        long wayland = SDLProperties.SDL_GetPointerProperty(properties, SDLVideo.SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER, 0L);
        if (wayland != 0L) {
            return wayland;
        }
        long x11 = SDLProperties.SDL_GetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_X11_WINDOW_NUMBER, 0L);
        if (x11 != 0L) {
            return x11;
        }
        long macOS = SDLProperties.SDL_GetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_COCOA_WINDOW_POINTER, 0L);
        if (macOS != 0L) {
            return macOS;
        }
        long android = SDLProperties.SDL_GetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_ANDROID_WINDOW_POINTER, 0L);
        if (android != 0L) {
            return android;
        }
        long ios = SDLProperties.SDL_GetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_UIKIT_WINDOW_POINTER, 0L);
        if (ios != 0L) {
            return ios;
        }
        return SDLProperties.SDL_GetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_VIVANTE_WINDOW_POINTER, 0L);
    }

    public synchronized int width() {
        return width;
    }

    public synchronized int height() {
        return height;
    }

    public synchronized int pixelWidth() {
        return pixelWidth;
    }

    public synchronized int pixelHeight() {
        return pixelHeight;
    }

    public synchronized String title() {
        return title;
    }

    public synchronized boolean focused() {
        return focused;
    }

    public synchronized boolean minimized() {
        return minimized;
    }

    public synchronized boolean closeRequested() {
        return closeRequested;
    }

    public synchronized boolean fullscreen() {
        return fullscreen;
    }

    public synchronized boolean borderless() {
        return borderless;
    }

    public synchronized boolean consumeResized() {
        boolean value = this.resized;
        this.resized = false;
        return value;
    }

    public synchronized Window title(String title) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowTitle(this.handle, title), "SDL_SetWindowTitle");
        this.title = title;
        return this;
    }

    public synchronized Window fullscreen(boolean fullscreen) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowFullscreen(handle, fullscreen), "SDL_SetWindowFullscreen");
        this.fullscreen = fullscreen;
        this.refreshSize();
        return this;
    }

    public synchronized Window borderless(boolean borderless) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowBordered(this.handle, !borderless), "SDL_SetWindowBordered");
        this.borderless = borderless;
        return this;
    }

    /**
     * Sets the swap interval of whichever context is current on this thread, which SDL keys to the context rather than
     * to the window. Make this window current first if another context may have taken over the thread.
     */
    public Window vsync(boolean enabled) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_GL_SetSwapInterval(enabled ? 1 : 0), "SDL_GL_SetSwapInterval");
        return this;
    }

    public Window makeCurrent() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_GL_MakeCurrent(this.handle, this.glContext), "SDL_GL_MakeCurrent");
        return this;
    }

    public Window releaseContext() {
        if (this.closed) {
            return this;
        }
        SDL.check(SDLVideo.SDL_GL_MakeCurrent(this.handle, 0L), "SDL_GL_MakeCurrent");
        return this;
    }

    public Window swapBuffers() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_GL_SwapWindow(this.handle), "SDL_GL_SwapWindow");
        return this;
    }

    public Window textInput(boolean enabled) {
        this.ensureOpen();
        boolean active = SDLKeyboard.SDL_TextInputActive(this.handle);
        if (enabled && !active) {
            SDL.check(SDLKeyboard.SDL_StartTextInput(this.handle), "SDL_StartTextInput");
        } else if (!enabled && active) {
            SDL.check(SDLKeyboard.SDL_StopTextInput(this.handle), "SDL_StopTextInput");
        }
        return this;
    }

    /** Drains SDL's process-wide event queue exactly once for the frame. */
    public synchronized Window pump() {
        this.ensureOpen();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Event event = SDL_Event.malloc(stack);
            while (SDLEvents.SDL_PollEvent(event)) {
                dispatch(event);
            }
        }
        this.mouseWarped = false;
        return this;
    }

    private void dispatch(SDL_Event event) {
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_QUIT -> closeRequested = true;
            case SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> key(event.key());
            case SDLEvents.SDL_EVENT_TEXT_INPUT -> text(event.text());
            case SDLEvents.SDL_EVENT_MOUSE_MOTION -> motion(event.motion());
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> button(event.button());
            case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> wheel(event.wheel());
            case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED,
                 SDLEvents.SDL_EVENT_WINDOW_RESIZED,
                 SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED,
                 SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED,
                 SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST,
                 SDLEvents.SDL_EVENT_WINDOW_MINIMIZED,
                 SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED,
                 SDLEvents.SDL_EVENT_WINDOW_RESTORED,
                 SDLEvents.SDL_EVENT_WINDOW_ENTER_FULLSCREEN,
                 SDLEvents.SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> window(type, event.window());
            default -> { }
        }
    }

    private void key(SDL_KeyboardEvent event) {
        if (event.windowID() != this.id) {
            return;
        }
        int scancode = event.scancode();
        if (scancode >= 0 && scancode < this.keys.length) {
            this.keys[scancode] = event.down();
        }
        this.legacy.key(scancode, event.key(), event.down(), event.repeat(), event.timestamp());
    }

    private void text(SDL_TextInputEvent event) {
        if (event.windowID() == this.id) {
            this.legacy.text(event.textString(), event.timestamp());
        }
    }

    private void motion(SDL_MouseMotionEvent event) {
        if (event.windowID() != this.id) {
            return;
        }
        this.mouseX = event.x();
        this.mouseY = event.y();
        float dx = this.mouseWarped ? 0.0F : event.xrel();
        float dy = this.mouseWarped ? 0.0F : event.yrel();
        this.mouseWarped = false;
        this.legacy.motion(this.mouseX, this.mouseY, dx, dy, event.timestamp());
    }

    private void button(SDL_MouseButtonEvent event) {
        if (event.windowID() != id) {
            return;
        }
        int button = event.button() & 0xFF;
        this.mouseX = event.x();
        this.mouseY = event.y();
        if (button > 0 && button < this.buttons.length) {
            this.buttons[button] = event.down();
        }
        this.legacy.button(button, event.down(), this.mouseX, this.mouseY, event.timestamp());
    }

    private void wheel(SDL_MouseWheelEvent event) {
        if (event.windowID() != this.id) {
            return;
        }
        float amount = event.y();
        if (event.direction() == SDLMouse.SDL_MOUSEWHEEL_FLIPPED) {
            amount = -amount;
        }
        this.legacy.wheel(event.mouse_x(), event.mouse_y(), amount, event.timestamp());
    }

    private void window(int type, SDL_WindowEvent event) {
        if (event.windowID() != this.id) {
            return;
        }
        switch (type) {
            case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED -> this.closeRequested = true;
            case SDLEvents.SDL_EVENT_WINDOW_RESIZED -> {
                this.width = event.data1();
                this.height = event.data2();
                this.resized = true;
            }
            case SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED -> {
                this.pixelWidth = event.data1();
                this.pixelHeight = event.data2();
                this.resized = true;
            }
            case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED -> this.focused = true;
            case SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> {
                this.focused = false;
                this.releaseHeldInput(event.timestamp());
            }
            case SDLEvents.SDL_EVENT_WINDOW_MINIMIZED -> this.minimized = true;
            case SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED, SDLEvents.SDL_EVENT_WINDOW_RESTORED -> this.minimized = false;
            case SDLEvents.SDL_EVENT_WINDOW_ENTER_FULLSCREEN -> this.fullscreen = true;
            case SDLEvents.SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> this.fullscreen = false;
            default -> { }
        }
    }

    private void releaseHeldInput(long timestampNs) {
        for (int scancode = 0; scancode < this.keys.length; scancode++) {
            if (this.keys[scancode]) {
                this.keys[scancode] = false;
                this.legacy.key(scancode, 0, false, false, timestampNs);
            }
        }
        for (int button = 1; button < this.buttons.length; button++) {
            if (this.buttons[button]) {
                this.buttons[button] = false;
                this.legacy.button(button, false, this.mouseX, this.mouseY, timestampNs);
            }
        }
    }

    /**
     * @throws IllegalStateException if the window is closed, rather than letting SDL fail on a freed handle
     */
    private void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("Cleanroom's SDL window is closed");
        }
    }

    private void refreshSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            if (SDLVideo.SDL_GetWindowSize(this.handle, w, h)) {
                this.width = w.get(0);
                this.height = h.get(0);
            }
            if (SDLVideo.SDL_GetWindowSizeInPixels(this.handle, w, h)) {
                this.pixelWidth = w.get(0);
                this.pixelHeight = h.get(0);
            }
        }
    }

    synchronized boolean keyDown(int scancode) {
        return scancode >= 0 && scancode < this.keys.length && this.keys[scancode];
    }

    synchronized boolean mouseButtonDown(int button) {
        return button > 0 && button < this.buttons.length && this.buttons[button];
    }

    synchronized float mouseX() {
        return mouseX;
    }

    synchronized float mouseY() {
        return mouseY;
    }

    synchronized void mousePosition(float x, float y) {
        this.ensureOpen();
        SDLMouse.SDL_WarpMouseInWindow(this.handle, x, y);
        this.mouseX = x;
        this.mouseY = y;
        this.mouseWarped = !this.mouseGrabbed;
    }

    synchronized void grabMouse(boolean grab) {
        this.ensureOpen();
        SDL.check(SDLMouse.SDL_SetWindowRelativeMouseMode(handle, grab), "SDL_SetWindowRelativeMouseMode");
        this.mouseGrabbed = grab;
        this.legacy.resetDeltas();
        this.mouseWarped = true;
    }

    synchronized boolean mouseGrabbed() {
        return mouseGrabbed;
    }

    WindowBridge.KeyEvent nextKeyEvent() {
        return this.legacy.nextKey();
    }

    int queuedKeyEvents() {
        return this.legacy.queuedKeys();
    }

    WindowBridge.MouseEvent nextMouseEvent() {
        return this.legacy.nextMouse();
    }

    int queuedMouseEvents() {
        return this.legacy.queuedMice();
    }

    float takeMouseDeltaX() {
        return this.legacy.takeDeltaX();
    }

    float takeMouseDeltaY() {
        return this.legacy.takeDeltaY();
    }

    float takeMouseWheel() {
        return this.legacy.takeWheel();
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        Arrays.fill(this.keys, false);
        Arrays.fill(this.buttons, false);
        if (this.glContext != 0L) {
            if (SDLVideo.SDL_GL_GetCurrentContext() == this.glContext) {
                SDLVideo.SDL_GL_MakeCurrent(this.handle, 0L);
                GL.setCapabilities(null);
            }
            SDLVideo.SDL_GL_DestroyContext(this.glContext);
        }
        SDLVideo.SDL_DestroyWindow(this.handle);
        synchronized (Window.class) {
            if (main == this) {
                main = null;
            }
        }
    }

    public static final class Builder {

        private String title = "Window";
        private int width = 800;
        private int height = 600;
        private boolean resizable;
        private boolean hidden;
        private boolean fullscreen;
        private boolean vsync;
        private GLConfig gl;

        private Builder() { }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder resizable() {
            resizable = true;
            return this;
        }

        public Builder hidden() {
            this.hidden = true;
            return this;
        }

        public Builder fullscreen() {
            this.fullscreen = true;
            return this;
        }

        public Builder vsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        public Builder openGL() {
            return openGL(_ -> { });
        }

        public Builder openGL(Consumer<GLConfig> configure) {
            this.gl = new GLConfig();
            configure.accept(this.gl);
            return this;
        }

        public Window build() {
            synchronized (Window.class) {
                if (main != null) {
                    throw new IllegalStateException("Cleanroom's SDL window already exists");
                }
                return create();
            }
        }

        private Window create() {
            SDL.ensureVideo();
            long flags = 0L;
            if (this.gl != null) {
                this.gl.apply();
                flags |= SDLVideo.SDL_WINDOW_OPENGL;
            }
            if (this.resizable) {
                flags |= SDLVideo.SDL_WINDOW_RESIZABLE;
            }
            if (this.hidden) {
                flags |= SDLVideo.SDL_WINDOW_HIDDEN;
            }
            if (this.fullscreen) {
                flags |= SDLVideo.SDL_WINDOW_FULLSCREEN;
            }
            long handle = SDL.checkHandle(SDLVideo.SDL_CreateWindow(this.title, this.width, this.height, flags), "SDL_CreateWindow");
            long context = 0L;
            boolean created = false;
            try {
                if (this.gl != null) {
                    context = SDL.checkHandle(SDLVideo.SDL_GL_CreateContext(handle), "SDL_GL_CreateContext");
                    SDL.check(SDLVideo.SDL_GL_MakeCurrent(handle, context), "SDL_GL_MakeCurrent");
                    SDL.check(SDLVideo.SDL_GL_SetSwapInterval(this.vsync ? 1 : 0), "SDL_GL_SetSwapInterval");
                    GL.createCapabilities();
                }
                Window window = new Window(handle, context, this.title, this.width, this.height);
                window.fullscreen = this.fullscreen;
                window.refreshSize();
                main = window;
                created = true;
                return window;
            } finally {
                if (!created) {
                    GL.setCapabilities(null);
                    if (context != 0L) {
                        SDLVideo.SDL_GL_DestroyContext(context);
                    }
                    SDLVideo.SDL_DestroyWindow(handle);
                }
            }
        }
    }

    public static final class GLConfig {

        private static void attribute(int attribute, int value) {
            SDL.check(SDLVideo.SDL_GL_SetAttribute(attribute, value), "SDL_GL_SetAttribute(" + attribute + ")");
        }

        private int major = 3;
        private int minor = 2;
        private boolean core;
        private int depthBits = 24;
        private int stencilBits = 8;

        private GLConfig() { }

        public GLConfig version(int major, int minor) {
            this.major = major;
            this.minor = minor;
            return this;
        }

        public GLConfig coreProfile() {
            core = true;
            return this;
        }

        public GLConfig compatibilityProfile() {
            core = false;
            return this;
        }

        public GLConfig depthBits(int depthBits) {
            this.depthBits = depthBits;
            return this;
        }

        public GLConfig stencilBits(int stencilBits) {
            this.stencilBits = stencilBits;
            return this;
        }

        private void apply() {
            attribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, this.major);
            attribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, this.minor);
            attribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, core ? SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE : SDLVideo.SDL_GL_CONTEXT_PROFILE_COMPATIBILITY);
            attribute(SDLVideo.SDL_GL_DOUBLEBUFFER, 1);
            attribute(SDLVideo.SDL_GL_DEPTH_SIZE, depthBits);
            attribute(SDLVideo.SDL_GL_STENCIL_SIZE, stencilBits);
        }

    }
}
