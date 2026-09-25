package com.cleanroommc.cleanroom.cache;

import net.minecraft.launchwrapper.Launch;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A file cache confined to a root directory.
 * <p>
 * Entries are named by relative paths that may never leave the root, or pass through a symbolic link.
 * Every entry pins its SHA-256 and is verified on each access.
 * Each content version has its own path, so updates never replace a file that may be open.
 */
public final class Cache {

    static final String META = ".meta", LOCK = ".lock", VERSIONS = ".versions";

    private final Path base;
    private final Path root;

    private Cache(Path base, Path root) {
        this.base = base;
        this.root = root;
    }

    private static Cache at(Path root) {
        var absolute = root.toAbsolutePath().normalize();
        return new Cache(absolute, absolute);
    }

    /**
     * @return the cache of this game instance, {@code <gameDir>/.cleanroom/cache} unless {@code cleanroom.cache.root.instance} is set
     */
    public static Cache instance() {
        var override = System.getProperty("cleanroom.cache.root.instance");
        if (override != null) {
            return at(Path.of(override));
        }
        var gameDir = Launch.minecraftHome == null ? Path.of("") : Launch.minecraftHome.toPath();
        return at(gameDir.resolve(".cleanroom").resolve("cache"));
    }

    /**
     * @return the cache shared by every instance, {@code ~/.cleanroom/cache} unless {@code cleanroom.cache.root.global} is set
     */
    public static Cache global() {
        var override = System.getProperty("cleanroom.cache.root.global");
        return at(override != null ? Path.of(override) : Path.of(System.getProperty("user.home"), ".cleanroom", "cache"));
    }

    public Cache namespace(String name) {
        return new Cache(base, resolve(name));
    }

    public Path root() {
        return root;
    }

    public CacheRequest fromUrl(URI uri) {
        var scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only HTTP(S) URLs can be cached: " + uri);
        }
        return new CacheRequest(this, new CacheRequest.Remote(uri));
    }

    public CacheRequest fromResource(ClassLoader loader, String name) {
        return new CacheRequest(this, new CacheRequest.Resource(loader, name));
    }

    public CacheRequest fromFile(Path file) {
        return new CacheRequest(this, new CacheRequest.Local(file));
    }

    public CacheRequest fromString(String value, Charset charset) {
        return fromBytes(value.getBytes(charset));
    }

    public CacheRequest fromBytes(byte[] bytes) {
        return new CacheRequest(this, new CacheRequest.Bytes(bytes.clone()));
    }

    public CacheRequest fromBuffer(ByteBuffer buffer) {
        var bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        return new CacheRequest(this, new CacheRequest.Bytes(bytes));
    }

    Path entry(String name) {
        var path = resolve(name);
        if (path.equals(root)) {
            throw new IllegalArgumentException("Not a valid cache entry name: " + name);
        }
        return path;
    }

    /**
     * Rejects any existing symbolic link between the outermost root and the path, both inclusive.
     */
    void checkLinks(Path path) throws CacheException {
        for (var current = path; current != null && current.startsWith(base); current = current.getParent()) {
            if (Files.isSymbolicLink(current)) {
                throw new CacheException(CacheException.Kind.UNSAFE_PATH, "Symbolic link in cache path: " + current);
            }
        }
    }

    // Rejecting backslashes and colons on every OS covers drive letters and UNC prefixes
    private Path resolve(String name) {
        if (name.isEmpty() || name.indexOf('\0') >= 0 || name.indexOf('\\') >= 0 || name.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Not a valid cache path: " + name);
        }
        var path = root.resolve(name).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Cache path escapes its root: " + name);
        }
        // Entries keep their files beside them under these suffixes
        for (var segment : root.relativize(path)) {
            var part = segment.toString();
            if (part.endsWith(META) || part.endsWith(LOCK) || part.endsWith(VERSIONS)) {
                throw new IllegalArgumentException("Cache path uses a reserved suffix: " + name);
            }
        }
        return path;
    }

}
