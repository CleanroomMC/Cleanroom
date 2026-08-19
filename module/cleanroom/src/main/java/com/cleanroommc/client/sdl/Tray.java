package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDLTray;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.sdl.SDL_TrayCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * One system-tray icon. Clicks arrive on the thread that {@link Window#pump() pumps}.
 */
public final class Tray implements AutoCloseable {

    private static final List<Tray> OPEN = new ArrayList<>();

    private final long handle;
    private final List<SDL_TrayCallback> callbacks = new ArrayList<>();

    private boolean closed;

    private Tray(long handle) {
        this.handle = handle;
    }

    public static Tray create(BufferedImage icon, String tooltip) {
        SDL.ensureVideo();
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        SDL_Surface surface = Surfaces.from(icon);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long tooltipAddress = MemoryUtil.memAddress(stack.UTF8(tooltip == null ? "" : tooltip));
            long handle = SDL.checkHandle(SDLTray.nSDL_CreateTray(surface.address(), tooltipAddress), "SDL_CreateTray");
            Tray tray = new Tray(handle);
            synchronized (OPEN) {
                OPEN.add(tray);
            }
            return tray;
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
    }

    public Tray tooltip(String tooltip) {
        ensureOpen();
        SDLTray.SDL_SetTrayTooltip(handle, tooltip == null ? "" : tooltip);
        return this;
    }

    public Tray icon(BufferedImage image) {
        ensureOpen();
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        SDL_Surface surface = Surfaces.from(image);
        try {
            SDLTray.SDL_SetTrayIcon(handle, surface);
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
        return this;
    }

    public Menu menu() {
        ensureOpen();
        long existing = SDLTray.SDL_GetTrayMenu(handle);
        long menu = existing != 0L ? existing : SDLTray.SDL_CreateTrayMenu(handle);
        SDL.checkHandle(menu, "SDL_CreateTrayMenu");
        return new Menu(menu);
    }

    static void pump() {
        synchronized (OPEN) {
            if (!OPEN.isEmpty()) {
                SDLTray.SDL_UpdateTrays();
            }
        }
    }

    public static void reset() {
        synchronized (OPEN) {
            for (Tray tray : List.copyOf(OPEN)) {
                tray.close();
            }
            OPEN.clear();
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The tray is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        synchronized (OPEN) {
            OPEN.remove(this);
        }
        SDLTray.SDL_DestroyTray(handle);
        callbacks.clear();
    }

    public final class Menu {

        private final long handle;

        Menu(long handle) {
            this.handle = handle;
        }

        public Menu button(String label, Runnable action) {
            long entry = insert(label, SDLTray.SDL_TRAYENTRY_BUTTON);
            bind(entry, ignored -> {
                if (action != null) {
                    action.run();
                }
            });
            return this;
        }

        public Menu checkbox(String label, boolean checked, Consumer<Boolean> action) {
            int flags = SDLTray.SDL_TRAYENTRY_CHECKBOX;
            if (checked) {
                flags |= SDLTray.SDL_TRAYENTRY_CHECKED;
            }
            long entry = insert(label, flags);
            bind(entry, clicked -> {
                boolean now = SDLTray.SDL_GetTrayEntryChecked(clicked);
                if (action != null) {
                    action.accept(now);
                }
            });
            return this;
        }

        public Menu separator() {
            insert(null, 0);
            return this;
        }

        public Menu submenu(String label, Consumer<Menu> build) {
            long entry = insert(label, SDLTray.SDL_TRAYENTRY_SUBMENU);
            long submenu = SDLTray.SDL_CreateTraySubmenu(entry);
            SDL.checkHandle(submenu, "SDL_CreateTraySubmenu");
            if (build != null) {
                build.accept(new Menu(submenu));
            }
            return this;
        }

        private long insert(String label, int flags) {
            ensureOpen();
            long entry = label == null
                    ? SDLTray.nSDL_InsertTrayEntryAt(handle, -1, 0L, flags)
                    : SDLTray.SDL_InsertTrayEntryAt(handle, -1, label, flags);
            if (label != null) {
                SDL.checkHandle(entry, "SDL_InsertTrayEntryAt");
            }
            return entry;
        }

        private void bind(long entry, Consumer<Long> action) {
            SDL_TrayCallback callback = SDL_TrayCallback.create((userdata, clicked) -> action.accept(clicked));
            callbacks.add(callback);
            SDLTray.SDL_SetTrayEntryCallback(entry, callback, 0L);
        }

    }

}
