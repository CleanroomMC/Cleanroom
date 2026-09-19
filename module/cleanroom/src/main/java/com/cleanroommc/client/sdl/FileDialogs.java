package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLMessageBox;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.sdl.SDL_MessageBoxButtonData;
import org.lwjgl.sdl.SDL_MessageBoxData;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Native message boxes and file dialogs.
 *
 * <p>File dialogs complete on a later {@link Window#pump()} and invoke {@code onResult} on that thread.
 */
public final class FileDialogs {

    public record Filter(String name, String pattern) { }

    public record Button(int id, String text, boolean isDefault, boolean isEscape) {

        public Button(int id, String text) {
            this(id, text, false, false);
        }

    }

    private static final Queue<Runnable> PENDING_RELEASE = new ConcurrentLinkedQueue<>();

    public void error(String title, String message) {
        box(SDLMessageBox.SDL_MESSAGEBOX_ERROR, title, message);
    }

    public void warn(String title, String message) {
        box(SDLMessageBox.SDL_MESSAGEBOX_WARNING, title, message);
    }

    public void info(String title, String message) {
        box(SDLMessageBox.SDL_MESSAGEBOX_INFORMATION, title, message);
    }

    /**
     * Blocking custom button box.
     *
     * @return the id of the button that was pressed, or {@code -1} if the box was dismissed
     */
    public int ask(String title, String message, Button... buttons) {
        if (buttons == null || buttons.length == 0) {
            throw new IllegalArgumentException("At least one button is required");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_MessageBoxButtonData.Buffer nativeButtons = SDL_MessageBoxButtonData.calloc(buttons.length, stack);
            for (int i = 0; i < buttons.length; i++) {
                Button button = buttons[i];
                int flags = 0;
                if (button.isDefault()) {
                    flags |= SDLMessageBox.SDL_MESSAGEBOX_BUTTON_RETURNKEY_DEFAULT;
                }
                if (button.isEscape()) {
                    flags |= SDLMessageBox.SDL_MESSAGEBOX_BUTTON_ESCAPEKEY_DEFAULT;
                }
                nativeButtons.get(i)
                        .flags(flags)
                        .buttonID(button.id())
                        .text(stack.UTF8(button.text()));
            }
            SDL_MessageBoxData data = SDL_MessageBoxData.calloc(stack);
            data.flags(0)
                    .window(parentHandle())
                    .title(stack.UTF8(title == null ? "" : title))
                    .message(stack.UTF8(message == null ? "" : message))
                    .buttons(nativeButtons);
            IntBuffer pressed = stack.mallocInt(1);
            SDL.check(SDLMessageBox.SDL_ShowMessageBox(data, pressed), "SDL_ShowMessageBox");
            return pressed.get(0);
        }
    }

    public void openFile(List<Filter> filters, boolean many, Consumer<List<Path>> onResult) {
        fileDialog(filters, many, null, true, false, onResult);
    }

    public void saveFile(List<Filter> filters, String defaultLocation, Consumer<List<Path>> onResult) {
        fileDialog(filters, false, defaultLocation, false, false, onResult);
    }

    public void openFolder(boolean many, Consumer<List<Path>> onResult) {
        fileDialog(List.of(), many, null, false, true, onResult);
    }

    /** Releases the native memory of dialogs that completed during the last event pump. */
    static void pump() {
        Runnable release;
        while ((release = PENDING_RELEASE.poll()) != null) {
            release.run();
        }
    }

    private static void fileDialog(List<Filter> filters, boolean many, String location,
            boolean openFile, boolean folder, Consumer<List<Path>> onResult) {
        if (onResult == null) {
            throw new IllegalArgumentException("onResult cannot be null");
        }
        List<ByteBuffer> strings = new ArrayList<>();
        SDL_DialogFileFilter.Buffer nativeFilters = null;
        if (filters != null && !filters.isEmpty()) {
            nativeFilters = SDL_DialogFileFilter.calloc(filters.size());
            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                ByteBuffer name = MemoryUtil.memUTF8(filter.name());
                ByteBuffer pattern = MemoryUtil.memUTF8(filter.pattern());
                strings.add(name);
                strings.add(pattern);
                nativeFilters.get(i).name(name).pattern(pattern);
            }
        }
        ByteBuffer where = location == null || location.isEmpty() ? null : MemoryUtil.memUTF8(location);
        if (where != null) {
            strings.add(where);
        }

        SDL_DialogFileFilter.Buffer ownedFilters = nativeFilters;
        SDL_DialogFileCallback[] holder = new SDL_DialogFileCallback[1];
        holder[0] = SDL_DialogFileCallback.create((userdata, filelist, filter) -> {
            SDL_DialogFileCallback callback = holder[0];
            try {
                onResult.accept(readPaths(filelist));
            } finally {
                PENDING_RELEASE.add(() -> release(callback, ownedFilters, strings));
            }
        });
        try {
            long parent = parentHandle();
            if (folder) {
                SDLDialog.SDL_ShowOpenFolderDialog(holder[0], 0L, parent, where, many);
            } else if (openFile) {
                SDLDialog.SDL_ShowOpenFileDialog(holder[0], 0L, parent, nativeFilters, where, many);
            } else {
                SDLDialog.SDL_ShowSaveFileDialog(holder[0], 0L, parent, nativeFilters, where);
            }
        } catch (RuntimeException e) {
            release(holder[0], nativeFilters, strings);
            throw e;
        }
    }

    private static void release(SDL_DialogFileCallback callback, SDL_DialogFileFilter.Buffer filters, List<ByteBuffer> strings) {
        if (callback != null) {
            callback.free();
        }
        if (filters != null) {
            filters.free();
        }
        for (ByteBuffer string : strings) {
            MemoryUtil.memFree(string);
        }
        strings.clear();
    }

    private static List<Path> readPaths(long filelist) {
        List<Path> paths = new ArrayList<>();
        if (filelist == 0L) {
            return paths;
        }
        int pointerSize = Pointer.POINTER_SIZE;
        for (int i = 0; ; i++) {
            long address = MemoryUtil.memGetAddress(filelist + (long) i * pointerSize);
            if (address == 0L) {
                break;
            }
            String value = MemoryUtil.memUTF8Safe(address);
            if (value != null && !value.isEmpty()) {
                paths.add(Path.of(value));
            }
        }
        return paths;
    }

    private static void box(int flags, String title, String message) {
        SDL.check(SDLMessageBox.SDL_ShowSimpleMessageBox(flags,
                title == null ? "" : title,
                message == null ? "" : message,
                parentHandle()), "SDL_ShowSimpleMessageBox"
        );
    }

    private static long parentHandle() {
        Window window = Window.main();
        return window == null ? 0L : window.handle();
    }

    static final FileDialogs INSTANCE = new FileDialogs();

    private FileDialogs() { }

}
