package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.drop.Drops;
import com.cleanroommc.client.sdl.input.virtual.Text;
import com.cleanroommc.client.sdl.video.Display;
import com.cleanroommc.client.sdl.video.DisplayMode;
import com.cleanroommc.client.sdl.video.Displays;
import com.cleanroommc.lwjgly.spi.WindowBridge;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_KeyboardEvent;
import org.lwjgl.sdl.SDL_MouseButtonEvent;
import org.lwjgl.sdl.SDL_MouseMotionEvent;
import org.lwjgl.sdl.SDL_MouseWheelEvent;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.sdl.SDL_TextEditingCandidatesEvent;
import org.lwjgl.sdl.SDL_TextEditingEvent;
import org.lwjgl.sdl.SDL_TextInputEvent;
import org.lwjgl.sdl.SDL_WindowEvent;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private final Text text = new Text(this);
    private final boolean[] keys = new boolean[MAX_SCANCODES];
    private final boolean[] buttons = new boolean[MAX_MOUSE_BUTTONS + 1];
    private final List<WindowListener> listeners = new CopyOnWriteArrayList<>();

    private String title;
    private int width;
    private int height;
    private int pixelWidth;
    private int pixelHeight;
    private float mouseX;
    private float mouseY;
    private float opacity = 1.0F;
    private boolean focused = true;
    private boolean closeRequested;
    private boolean resized;
    private boolean fullscreen;
    private boolean borderless;
    private boolean minimized;
    private boolean maximized;
    private boolean hidden;
    private boolean resizable;
    private boolean alwaysOnTop;
    private boolean mouseGrabbed;
    private boolean mouseWarped;
    private boolean mouseInside = true;

    private volatile boolean closed;

    /** Attention request passed to {@link #flash(Flash)}. */
    public enum Flash {

        CANCEL(SDLVideo.SDL_FLASH_CANCEL),
        BRIEFLY(SDLVideo.SDL_FLASH_BRIEFLY),
        UNTIL_FOCUSED(SDLVideo.SDL_FLASH_UNTIL_FOCUSED);

        private final int value;

        Flash(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

    }

    /** Taskbar/dock progress passed to {@link #progress(Progress, float)}. */
    public enum Progress {

        NONE(SDLVideo.SDL_PROGRESS_STATE_NONE),
        INDETERMINATE(SDLVideo.SDL_PROGRESS_STATE_INDETERMINATE),
        NORMAL(SDLVideo.SDL_PROGRESS_STATE_NORMAL),
        PAUSED(SDLVideo.SDL_PROGRESS_STATE_PAUSED),
        ERROR(SDLVideo.SDL_PROGRESS_STATE_ERROR);

        private final int value;

        Progress(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

    }

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
     */
    public long nativeHandle() {
        return nativeWindow().handle();
    }

    /**
     * @return which compositor owns the native handle, and the handle itself
     */
    public NativeWindow nativeWindow() {
        this.ensureOpen();
        int properties = SDLVideo.SDL_GetWindowProperties(handle);
        if (properties == 0) {
            return NativeWindow.UNKNOWN;
        }
        for (Compositor compositor : Compositor.values()) {
            if (compositor == Compositor.UNKNOWN) {
                continue;
            }
            long value = compositor.pointer()
                    ? SDLProperties.SDL_GetPointerProperty(properties, compositor.property(), 0L)
                    : SDLProperties.SDL_GetNumberProperty(properties, compositor.property(), 0L);
            if (value != 0L) {
                return new NativeWindow(compositor, value);
            }
        }
        return NativeWindow.UNKNOWN;
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

    public synchronized boolean maximized() {
        return maximized;
    }

    public synchronized boolean hidden() {
        return hidden;
    }

    public synchronized boolean resizable() {
        return resizable;
    }

    public synchronized boolean alwaysOnTop() {
        return alwaysOnTop;
    }

    public synchronized boolean mouseInside() {
        return mouseInside;
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
        notifyListeners(listener -> listener.borderlessChanged(borderless));
        return this;
    }

    public synchronized Window size(int width, int height) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowSize(this.handle, width, height), "SDL_SetWindowSize");
        this.refreshSize();
        return this;
    }

    public synchronized int[] position() {
        this.ensureOpen();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            SDL.check(SDLVideo.SDL_GetWindowPosition(this.handle, x, y), "SDL_GetWindowPosition");
            return new int[] { x.get(0), y.get(0) };
        }
    }

    public synchronized Window position(int x, int y) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowPosition(this.handle, x, y), "SDL_SetWindowPosition");
        return this;
    }

    public synchronized Window minimumSize(int width, int height) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowMinimumSize(this.handle, width, height), "SDL_SetWindowMinimumSize");
        return this;
    }

    public synchronized Window maximumSize(int width, int height) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowMaximumSize(this.handle, width, height), "SDL_SetWindowMaximumSize");
        return this;
    }

    public synchronized Window resizable(boolean resizable) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowResizable(this.handle, resizable), "SDL_SetWindowResizable");
        this.resizable = resizable;
        return this;
    }

    public synchronized Window alwaysOnTop(boolean alwaysOnTop) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowAlwaysOnTop(this.handle, alwaysOnTop), "SDL_SetWindowAlwaysOnTop");
        this.alwaysOnTop = alwaysOnTop;
        return this;
    }

    public synchronized Window opacity(float opacity) {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_SetWindowOpacity(this.handle, opacity), "SDL_SetWindowOpacity");
        this.opacity = opacity;
        return this;
    }

    public synchronized float opacity() {
        return opacity;
    }

    public synchronized Window show() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_ShowWindow(this.handle), "SDL_ShowWindow");
        this.hidden = false;
        return this;
    }

    public synchronized Window hide() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_HideWindow(this.handle), "SDL_HideWindow");
        this.hidden = true;
        return this;
    }

    public synchronized Window raise() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_RaiseWindow(this.handle), "SDL_RaiseWindow");
        return this;
    }

    public synchronized Window minimize() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_MinimizeWindow(this.handle), "SDL_MinimizeWindow");
        this.minimized = true;
        this.maximized = false;
        return this;
    }

    public synchronized Window maximize() {
        this.ensureOpen();
        SDL.check(SDLVideo.SDL_MaximizeWindow(this.handle), "SDL_MaximizeWindow");
        this.maximized = true;
        this.minimized = false;
        return this;
    }

    public synchronized Window flash(Flash flash) {
        this.ensureOpen();
        if (flash == null) {
            throw new IllegalArgumentException("Flash cannot be null");
        }
        SDL.check(SDLVideo.SDL_FlashWindow(this.handle, flash.value), "SDL_FlashWindow");
        return this;
    }

    public synchronized Window progress(Progress state) {
        return progress(state, 0.0F);
    }

    /**
     * @param state taskbar progress kind
     * @param value {@code 0..1} when {@code state} is {@link Progress#NORMAL}, {@link Progress#PAUSED} or {@link Progress#ERROR}
     */
    public synchronized Window progress(Progress state, float value) {
        this.ensureOpen();
        if (state == null) {
            throw new IllegalArgumentException("Progress cannot be null");
        }
        SDL.check(SDLVideo.SDL_SetWindowProgressState(this.handle, state.value), "SDL_SetWindowProgressState");
        if (state != Progress.NONE && state != Progress.INDETERMINATE) {
            SDL.check(SDLVideo.SDL_SetWindowProgressValue(this.handle, Math.clamp(value, 0.0F, 1.0F)), "SDL_SetWindowProgressValue");
        }
        return this;
    }

    public Window icon(BufferedImage... images) {
        this.ensureOpen();
        if (images == null || images.length == 0 || images[0] == null) {
            throw new IllegalArgumentException("At least one icon image is required");
        }
        SDL_Surface primary = Surfaces.from(images[0]);
        try {
            for (int i = 1; i < images.length; i++) {
                if (images[i] == null) {
                    continue;
                }
                SDL_Surface extra = Surfaces.from(images[i]);
                try {
                    SDL.check(SDLSurface.SDL_AddSurfaceAlternateImage(primary, extra), "SDL_AddSurfaceAlternateImage");
                } finally {
                    SDLSurface.SDL_DestroySurface(extra);
                }
            }
            SDL.check(SDLVideo.SDL_SetWindowIcon(this.handle, primary), "SDL_SetWindowIcon");
        } finally {
            SDLSurface.SDL_DestroySurface(primary);
        }
        return this;
    }

    public Window icon(Path path) {
        this.ensureOpen();
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        SDL_Surface surface = SDLSurface.SDL_LoadSurface(path.toAbsolutePath().toString());
        if (surface == null) {
            throw new SDLException("SDL_LoadSurface failed");
        }
        try {
            SDL.check(SDLVideo.SDL_SetWindowIcon(this.handle, surface), "SDL_SetWindowIcon");
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
        return this;
    }

    public Display display() {
        this.ensureOpen();
        return Displays.of(SDLVideo.SDL_GetDisplayForWindow(this.handle));
    }

    public float pixelDensity() {
        this.ensureOpen();
        return SDLVideo.SDL_GetWindowPixelDensity(this.handle);
    }

    public float displayScale() {
        this.ensureOpen();
        return SDLVideo.SDL_GetWindowDisplayScale(this.handle);
    }

    /**
     * Exclusive fullscreen using {@code mode}, or borderless desktop fullscreen when {@code mode} is {@code null}.
     */
    public synchronized Window fullscreenMode(DisplayMode mode) {
        this.ensureOpen();
        if (mode == null) {
            SDL.check(SDLVideo.SDL_SetWindowFullscreenMode(this.handle, null), "SDL_SetWindowFullscreenMode");
            return fullscreen(true);
        }
        PointerBuffer modes = SDLVideo.SDL_GetFullscreenDisplayModes(mode.displayId());
        if (modes == null) {
            return fullscreen(true);
        }
        try {
            SDL_DisplayMode found = null;
            while (modes.hasRemaining()) {
                SDL_DisplayMode candidate = SDL_DisplayMode.createSafe(modes.get());
                if (candidate != null
                        && candidate.w() == mode.width()
                        && candidate.h() == mode.height()
                        && (mode.refreshRate() <= 0 || Math.abs(candidate.refresh_rate() - mode.refreshRate()) < 0.5F)) {
                    found = candidate;
                    break;
                }
            }
            if (found != null) {
                SDL.check(SDLVideo.SDL_SetWindowFullscreenMode(this.handle, found), "SDL_SetWindowFullscreenMode");
            }
        } finally {
            SDLStdinc.SDL_free(modes);
        }
        return fullscreen(true);
    }

    public Window listen(WindowListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
        return this;
    }

    public Window mute(WindowListener listener) {
        listeners.remove(listener);
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

    public Text text() {
        return this.text;
    }

    public Window textInput(boolean enabled) {
        this.text.active(enabled);
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
        Devices.afterPump();
        this.mouseWarped = false;
        return this;
    }

    private void dispatch(SDL_Event event) {
        int type = event.type();
        switch (type) {
            case SDLEvents.SDL_EVENT_QUIT -> closeRequested = true;
            case SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> key(event.key());
            case SDLEvents.SDL_EVENT_TEXT_INPUT -> text(event.text());
            case SDLEvents.SDL_EVENT_TEXT_EDITING -> editing(event.edit());
            case SDLEvents.SDL_EVENT_TEXT_EDITING_CANDIDATES -> editingCandidates(event.edit_candidates());
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
                 SDLEvents.SDL_EVENT_WINDOW_SHOWN,
                 SDLEvents.SDL_EVENT_WINDOW_HIDDEN,
                 SDLEvents.SDL_EVENT_WINDOW_MOUSE_ENTER,
                 SDLEvents.SDL_EVENT_WINDOW_MOUSE_LEAVE,
                 SDLEvents.SDL_EVENT_WINDOW_DISPLAY_CHANGED,
                 SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED,
                 SDLEvents.SDL_EVENT_WINDOW_ENTER_FULLSCREEN,
                 SDLEvents.SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> window(type, event.window());
            case SDLEvents.SDL_EVENT_CLIPBOARD_UPDATE -> Clipboard.updated();
            case SDLEvents.SDL_EVENT_SYSTEM_THEME_CHANGED -> SystemTheme.changed();
            case SDLEvents.SDL_EVENT_DROP_FILE,
                 SDLEvents.SDL_EVENT_DROP_TEXT,
                 SDLEvents.SDL_EVENT_DROP_BEGIN,
                 SDLEvents.SDL_EVENT_DROP_COMPLETE,
                 SDLEvents.SDL_EVENT_DROP_POSITION -> Drops.handle(event);
            case SDLEvents.SDL_EVENT_DISPLAY_ADDED,
                 SDLEvents.SDL_EVENT_DISPLAY_REMOVED,
                 SDLEvents.SDL_EVENT_DISPLAY_MOVED,
                 SDLEvents.SDL_EVENT_DISPLAY_DESKTOP_MODE_CHANGED,
                 SDLEvents.SDL_EVENT_DISPLAY_CURRENT_MODE_CHANGED,
                 SDLEvents.SDL_EVENT_DISPLAY_CONTENT_SCALE_CHANGED -> Displays.handle(type, event.display().displayID());
            default -> Devices.dispatch(event);
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
            this.text.committed();
            this.legacy.text(event.textString(), event.timestamp());
        }
    }

    private void editing(SDL_TextEditingEvent event) {
        if (event.windowID() == this.id) {
            this.text.editing(event);
        }
    }

    private void editingCandidates(SDL_TextEditingCandidatesEvent event) {
        if (event.windowID() == this.id) {
            this.text.editingCandidates(event);
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
            case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED -> {
                this.closeRequested = true;
                notifyListeners(listener -> listener.closeRequested());
            }
            case SDLEvents.SDL_EVENT_WINDOW_RESIZED -> {
                this.width = event.data1();
                this.height = event.data2();
                this.resized = true;
                notifyListeners(listener -> listener.resized(this.width, this.height));
            }
            case SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED -> {
                this.pixelWidth = event.data1();
                this.pixelHeight = event.data2();
                this.resized = true;
            }
            case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED -> {
                this.focused = true;
                notifyListeners(listener -> listener.focus(true));
            }
            case SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> {
                this.focused = false;
                this.releaseHeldInput(event.timestamp());
                notifyListeners(listener -> listener.focus(false));
            }
            case SDLEvents.SDL_EVENT_WINDOW_MINIMIZED -> {
                this.minimized = true;
                this.maximized = false;
            }
            case SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED -> {
                this.maximized = true;
                this.minimized = false;
            }
            case SDLEvents.SDL_EVENT_WINDOW_RESTORED -> {
                this.minimized = false;
                this.maximized = false;
            }
            case SDLEvents.SDL_EVENT_WINDOW_SHOWN -> this.hidden = false;
            case SDLEvents.SDL_EVENT_WINDOW_HIDDEN -> this.hidden = true;
            case SDLEvents.SDL_EVENT_WINDOW_MOUSE_ENTER -> this.mouseInside = true;
            case SDLEvents.SDL_EVENT_WINDOW_MOUSE_LEAVE -> this.mouseInside = false;
            case SDLEvents.SDL_EVENT_WINDOW_DISPLAY_CHANGED -> notifyListeners(WindowListener::displayChanged);
            case SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED ->
                    notifyListeners(listener -> listener.scaleChanged(SDLVideo.SDL_GetWindowDisplayScale(this.handle)));
            case SDLEvents.SDL_EVENT_WINDOW_ENTER_FULLSCREEN -> {
                this.fullscreen = true;
                notifyListeners(listener -> listener.fullscreenChanged(true));
            }
            case SDLEvents.SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> {
                this.fullscreen = false;
                notifyListeners(listener -> listener.fullscreenChanged(false));
            }
        }
    }

    private void notifyListeners(Consumer<WindowListener> action) {
        for (WindowListener listener : listeners) {
            action.accept(listener);
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

    public synchronized boolean keyDown(int scancode) {
        return scancode >= 0 && scancode < this.keys.length && this.keys[scancode];
    }

    public synchronized boolean mouseButtonDown(int button) {
        return button > 0 && button < this.buttons.length && this.buttons[button];
    }

    public synchronized float mouseX() {
        return mouseX;
    }

    public synchronized float mouseY() {
        return mouseY;
    }

    public synchronized void mousePosition(float x, float y) {
        this.ensureOpen();
        SDLMouse.SDL_WarpMouseInWindow(this.handle, x, y);
        this.mouseX = x;
        this.mouseY = y;
        this.mouseWarped = !this.mouseGrabbed;
    }

    public synchronized void grabMouse(boolean grab) {
        this.ensureOpen();
        SDL.check(SDLMouse.SDL_SetWindowRelativeMouseMode(handle, grab), "SDL_SetWindowRelativeMouseMode");
        this.mouseGrabbed = grab;
        this.legacy.resetDeltas();
        this.mouseWarped = true;
    }

    public synchronized boolean mouseGrabbed() {
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
                window.hidden = this.hidden;
                window.resizable = this.resizable;
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
