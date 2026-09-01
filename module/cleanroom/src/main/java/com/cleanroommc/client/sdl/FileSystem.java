package com.cleanroommc.client.sdl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLFileSystem;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_PathInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OS well-known folders and path queries. Not a replacement for {@code java.nio.file}.
 */
public final class FileSystem {

    static final FileSystem INSTANCE = new FileSystem();

    private FileSystem() { }

    public Path base() {
        return pathOrNull(SDLFileSystem.SDL_GetBasePath());
    }

    public Path pref(String org, String app) {
        if (org == null || app == null) {
            throw new IllegalArgumentException("org and app cannot be null");
        }
        return pathOrNull(SDLFileSystem.SDL_GetPrefPath(org, app));
    }

    public Path current() {
        return pathOrNull(SDLFileSystem.SDL_GetCurrentDirectory());
    }

    public Path folder(Folder folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Folder cannot be null");
        }
        return pathOrNull(SDLFileSystem.SDL_GetUserFolder(folder.value()));
    }

    public Optional<PathInfo> info(Path path) {
        if (path == null) {
            return Optional.empty();
        }
        return info(path.toAbsolutePath().toString());
    }

    public Optional<PathInfo> info(String path) {
        if (path == null || path.isEmpty()) {
            return Optional.empty();
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_PathInfo nativeInfo = SDL_PathInfo.calloc(stack);
            if (!SDLFileSystem.SDL_GetPathInfo(path, nativeInfo)) {
                return Optional.empty();
            }
            return Optional.of(new PathInfo(
                    PathKind.of(nativeInfo.type()),
                    nativeInfo.size(),
                    nativeInfo.create_time(),
                    nativeInfo.modify_time(),
                    nativeInfo.access_time()
            ));
        }
    }

    public List<Path> glob(Path directory, String pattern) {
        if (directory == null) {
            throw new IllegalArgumentException("Directory cannot be null");
        }
        return glob(directory.toAbsolutePath().toString(), pattern);
    }

    public List<Path> glob(String directory, String pattern) {
        if (directory == null) {
            throw new IllegalArgumentException("Directory cannot be null");
        }
        PointerBuffer matches = SDLFileSystem.SDL_GlobDirectory(directory, pattern, 0);
        if (matches == null) {
            return List.of();
        }
        try {
            List<Path> paths = new ArrayList<>(matches.remaining());
            while (matches.hasRemaining()) {
                String value = MemoryUtil.memUTF8(matches.get());
                if (value != null && !value.isEmpty()) {
                    paths.add(Path.of(value));
                }
            }
            return List.copyOf(paths);
        } finally {
            SDLStdinc.SDL_free(matches);
        }
    }

    private static Path pathOrNull(String value) {
        return value == null || value.isEmpty() ? null : Path.of(value);
    }


}
