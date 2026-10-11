package com.cleanroommc.cleanroom.cache;

import com.google.common.base.Preconditions;
import net.minecraft.launchwrapper.Launch;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * A file cache confined to a root directory.
 * <p>
 * Entries are named by relative paths that may never leave the root, or pass through a symbolic link.
 * <p>
 * Every entry pins its SHA-256 and is verified on each access.
 * <p>
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

    /**
     * @return a cache rooted at {@code name} under this root
     */
    public Cache namespace(String name) {
        return new Cache(base, resolve(name));
    }

    /**
     * @return the absolute directory entry names resolve against
     */
    public Path root() {
        return root;
    }

    /**
     * @param uri an HTTP or HTTPS URL
     */
    public CacheRequest fromUrl(URI uri) {
        var scheme = uri.getScheme();
        Preconditions.checkArgument("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme), "Only HTTP(S) URLs can be cached: %s", uri);
        return new CacheRequest(this, new CacheRequest.Remote(uri));
    }

    /**
     * @param name a resource name as taken by {@link ClassLoader#getResourceAsStream}
     */
    public CacheRequest fromResource(ClassLoader loader, String name) {
        return new CacheRequest(this, new CacheRequest.Resource(loader, name));
    }

    /**
     * The file is copied into the cache on ingestion.
     * Later changes to it are ignored by {@link CacheRequest#get} until the version or expected hash changes.
     */
    public CacheRequest fromFile(Path file) {
        return new CacheRequest(this, new CacheRequest.Local(file));
    }

    /**
     * Caches the string as encoded in UTF-8.
     */
    public CacheRequest fromString(String value) {
        return fromString(value, StandardCharsets.UTF_8);
    }

    /**
     * Caches the string as encoded in {@code charset}.
     */
    public CacheRequest fromString(String value, Charset charset) {
        return fromBytes(value.getBytes(charset));
    }

    /**
     * The array is copied, later changes to it are not seen.
     */
    public CacheRequest fromBytes(byte[] bytes) {
        return new CacheRequest(this, new CacheRequest.Bytes(bytes.clone()));
    }

    /**
     * The buffer's remaining bytes are copied, its position is left untouched.
     */
    public CacheRequest fromBuffer(ByteBuffer buffer) {
        return new CacheRequest(this, new CacheRequest.Bytes(bytes(buffer)));
    }

    /**
     * The content is only produced when the entry has to be ingested, and need not be the same each time.
     */
    public CacheRequest fromBytes(Callable<byte[]> content) {
        return new CacheRequest(this, new CacheRequest.Lazy(content));
    }

    /**
     * The content is only produced when the entry has to be ingested, and need not be the same each time.
     */
    public CacheRequest fromBuffer(Callable<ByteBuffer> content) {
        return fromBytes(() -> bytes(content.call()));
    }

    private static byte[] bytes(ByteBuffer buffer) {
        var bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        return bytes;
    }

    /**
     * @return the validated path naming an entry, never the root itself
     */
    Path entry(String name) {
        var path = resolve(name);
        Preconditions.checkArgument(!path.equals(root), "Not a valid cache entry name: %s", name);

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
        Preconditions.checkArgument(!name.isEmpty() && name.indexOf('\0') < 0 && name.indexOf('\\') < 0 && name.indexOf(':') < 0, "Not a valid cache path: %s", name);
        var path = root.resolve(name).normalize();
        Preconditions.checkArgument(path.startsWith(root), "Cache path escapes its root: %s", name);

        // Entries keep their files beside them under these suffixes
        for (var segment : root.relativize(path)) {
            var part = segment.toString();
            Preconditions.checkArgument(!part.endsWith(META) && !part.endsWith(LOCK) && !part.endsWith(VERSIONS), "Cache path uses a reserved suffix: %s", name);
        }
        return path;
    }

}
