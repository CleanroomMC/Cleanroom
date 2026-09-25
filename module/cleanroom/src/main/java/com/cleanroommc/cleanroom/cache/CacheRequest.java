package com.cleanroommc.cleanroom.cache;

import com.cleanroommc.util.CleanroomLog;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Describes where an entry comes from and how it is trusted, then fetches it with {@link #get} or {@link #put}.
 * <p>
 * A request is a builder for a single caller. The cache behind it is safe across threads and processes.
 */
public final class CacheRequest {

    private static final String SHA256 = "sha256", VERSION = "version", CHECKED = "checked", URL = "url", ETAG = "etag", MODIFIED = "modified";
    private static final Pattern HEX_SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final Duration GRACE = Duration.ofDays(1);

    // Serializes threads of this JVM, file locks only exclude other processes
    private static final ConcurrentHashMap<Path, ReentrantLock> LOCKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Path, CompletableFuture<Void>> INGESTING = new ConcurrentHashMap<>();
    private static final Set<Path> SWEPT = ConcurrentHashMap.newKeySet();

    sealed interface Source { }

    record Remote(URI uri) implements Source { }

    record Resource(ClassLoader loader, String name) implements Source { }

    record Local(Path file) implements Source { }

    record Bytes(byte[] bytes) implements Source { }

    private enum Mode { GET, PUT, REVALIDATE }

    /**
     * @param path     the verified content, null when it has to be ingested
     * @param expected the hash ingested content must match
     */
    private record Lookup(Properties meta, @Nullable Path path, boolean stale, @Nullable String expected) { }

    /**
     * @param hash null when the server confirmed the cached content is current
     */
    private record Fetched(@Nullable String hash, @Nullable String etag, @Nullable String modified) { }

    @FunctionalInterface
    private interface Locked<T> {

        T run(Properties meta) throws IOException;

    }

    private final Cache cache;
    private final Source source;
    private @Nullable String sha256;
    private @Nullable String version;
    private @Nullable Duration maxAge;

    CacheRequest(Cache cache, Source source) {
        this.cache = cache;
        this.source = source;
    }

    /**
     * Content must match this SHA-256, otherwise it is pinned on first ingestion and verified from then on.
     */
    public CacheRequest expectSha256(String hex) {
        if (hex.length() != 64) {
            throw new IllegalArgumentException("SHA-256 must be 64 hexadecimal characters: " + hex);
        }
        this.sha256 = HexFormat.of().formatHex(HexFormat.of().parseHex(hex));
        return this;
    }

    /**
     * Re-ingests the entry when this version is greater than the cached one.
     */
    public CacheRequest version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Serves a URL entry for this long after it was last checked, then keeps serving it while it revalidates in the background.
     */
    public CacheRequest revalidateIf(Duration maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * @return the entry's content, ingested only when missing, corrupt, outdated or expected with another hash.
     * The path changes with the content.
     */
    public Path get(String name) throws IOException {
        return fetch(name, Mode.GET);
    }

    /**
     * @return the entry's content, always ingested from the source
     */
    public Path put(String name) throws IOException {
        return fetch(name, Mode.PUT);
    }

    public CompletableFuture<Path> getAsync(String name) {
        return async(() -> get(name));
    }

    public CompletableFuture<Path> putAsync(String name) {
        return async(() -> put(name));
    }

    private Path fetch(String name, Mode mode) throws IOException {
        var file = cache.entry(name);
        if (mode == Mode.PUT) {
            return ingest(file, new Properties(), sha256, false);
        }
        var lookup = lookup(file);
        if (mode == Mode.REVALIDATE) {
            return ingest(file, lookup.meta(), lookup.expected(), lookup.path() != null);
        }
        if (lookup.path() != null) {
            if (lookup.stale() && claimRevalidation(file, lookup.meta().getProperty(SHA256))) {
                async(() -> fetch(name, Mode.REVALIDATE)).exceptionally(t -> {
                    CleanroomLog.get().warn("Cannot revalidate cache entry {}, keeping it", file, t);
                    return null;
                });
            }
            return lookup.path();
        }
        // Concurrent gets of one entry share a single ingestion, then look again
        var ingesting = new CompletableFuture<Void>();
        var running = INGESTING.putIfAbsent(file, ingesting);
        if (running != null) {
            await(running);
            return fetch(name, mode);
        }
        try {
            lookup = lookup(file);
            var path = lookup.path() != null ? lookup.path() : ingest(file, lookup.meta(), lookup.expected(), false);
            ingesting.complete(null);
            return path;
        } catch (Throwable t) {
            ingesting.completeExceptionally(t);
            throw t;
        } finally {
            INGESTING.remove(file, ingesting);
        }
    }

    private Lookup lookup(Path file) throws IOException {
        var meta = locked(file, current -> {
            if (SWEPT.add(file)) {
                sweep(file, current.getProperty(SHA256));
            }
            return current;
        });
        var pinned = meta.getProperty(SHA256);
        if (pinned == null || outdated(meta) || (sha256 != null && !sha256.equals(pinned))) {
            return new Lookup(meta, null, false, sha256);
        }
        // Stored versions never change, so they are verified unlocked
        var path = stored(file, pinned);
        cache.checkLinks(path);
        if (holds(path, pinned)) {
            return new Lookup(meta, path, isStale(meta), sha256);
        }
        CleanroomLog.get().warn("Cache entry {} is missing or corrupt, restoring it", path);
        return new Lookup(meta, null, false, pinned);
    }

    // Marking the entry checked up front stops concurrent and failing revalidations from repeating
    private boolean claimRevalidation(Path file, String pinned) throws IOException {
        return locked(file, meta -> {
            if (!pinned.equals(meta.getProperty(SHA256)) || !isStale(meta)) {
                return false;
            }
            meta.setProperty(CHECKED, Long.toString(System.currentTimeMillis()));
            writeMeta(file, meta);
            return true;
        });
    }

    // Runs unlocked so readers are served during the download
    private Path ingest(Path file, Properties seen, @Nullable String expected, boolean conditional) throws IOException {
        var versions = versions(file);
        cache.checkLinks(versions);
        Files.createDirectories(versions);
        var temp = Files.createTempFile(versions, null, ".part");
        try {
            Fetched fetched;
            if (source instanceof Remote(var uri)) {
                fetched = download(uri, file, seen, conditional, temp);
            } else {
                try (var input = open()) {
                    fetched = new Fetched(write(input, temp), null, null);
                }
            }
            if (fetched.hash() != null && expected != null && !expected.equals(fetched.hash())) {
                throw new CacheException(CacheException.Kind.HASH_MISMATCH, "Expected SHA-256 " + expected + " for " + file + ", got " + fetched.hash());
            }
            return commit(file, seen, temp, fetched);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private Path commit(Path file, Properties seen, Path temp, Fetched fetched) throws IOException {
        return locked(file, meta -> {
            var previous = meta.getProperty(SHA256);
            var hash = fetched.hash();
            if (hash == null) {
                // A 304 only vouches for the content it was asked about
                if (!Objects.equals(previous, seen.getProperty(SHA256))) {
                    return stored(file, seen.getProperty(SHA256));
                }
            } else {
                var target = stored(file, hash);
                cache.checkLinks(target);
                // Identical content is kept, it may be open elsewhere
                if (!holds(target, hash)) {
                    Files.createDirectories(target.getParent());
                    move(temp, target);
                }
                meta.setProperty(SHA256, hash);
            }
            // Descriptions of replaced content are dropped, unchanged content keeps what this fetch does not restate
            var changed = hash != null && !hash.equals(previous);
            if (hash != null) {
                set(meta, VERSION, version, changed);
                set(meta, URL, source instanceof Remote(var uri) ? uri.toString() : null, changed);
            }
            set(meta, ETAG, fetched.etag(), changed);
            set(meta, MODIFIED, fetched.modified(), changed);
            meta.setProperty(CHECKED, Long.toString(System.currentTimeMillis()));
            writeMeta(file, meta);
            if (changed && previous != null) {
                // Starts the superseded version's grace period
                touch(versions(file).resolve(directory(previous)));
            }
            return stored(file, hash != null ? hash : previous);
        });
    }

    private Fetched download(URI uri, Path file, Properties seen, boolean conditional, Path temp) throws IOException {
        if (Boolean.getBoolean("cleanroom.offline")) {
            throw offline(file, seen, "Offline, cannot fetch " + uri, null);
        }
        var request = HttpRequest.newBuilder(uri).timeout(Http.TIMEOUT);
        // Validators only describe what the URL they came from served
        var validate = conditional && uri.toString().equals(seen.getProperty(URL));
        if (validate) {
            var etag = seen.getProperty(ETAG);
            if (etag != null) {
                request.header("If-None-Match", etag);
            }
            var modified = seen.getProperty(MODIFIED);
            if (modified != null) {
                request.header("If-Modified-Since", modified);
            }
        }
        HttpResponse<InputStream> response;
        try {
            response = Http.CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted fetching " + uri);
        } catch (IOException e) {
            throw offline(file, seen, "Cannot reach " + uri, e);
        }
        try (var body = response.body()) {
            String hash = null;
            if (response.statusCode() == 200) {
                hash = write(guard(body, uri), temp);
            } else if (response.statusCode() != 304 || !validate) {
                throw new CacheException(CacheException.Kind.HTTP, "HTTP " + response.statusCode() + " fetching " + uri);
            }
            return new Fetched(hash, response.headers().firstValue("ETag").orElse(null), response.headers().firstValue("Last-Modified").orElse(null));
        }
    }

    private CacheException offline(Path file, Properties seen, String message, @Nullable Throwable cause) {
        if (seen.getProperty(SHA256) != null && outdated(seen)) {
            message += ", " + file + " is cached at version " + seen.getProperty(VERSION, "unknown") + " but " + version + " is required";
        }
        return new CacheException(CacheException.Kind.OFFLINE, message, cause);
    }

    // Read failures, including a body stalled past the timeout, mean the source was lost
    private static InputStream guard(InputStream body, URI uri) {
        return new FilterInputStream(body) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                var watchdog = Http.WATCHDOG.schedule(Thread.currentThread()::interrupt, Http.TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                try {
                    return super.read(b, off, len);
                } catch (IOException e) {
                    throw new CacheException(CacheException.Kind.OFFLINE, "Lost connection fetching " + uri, e);
                } finally {
                    // An interrupt that fired as the read returned must not reach the file write
                    if (!watchdog.cancel(false)) {
                        Thread.interrupted();
                    }
                }
            }
        };
    }

    private <T> T locked(Path file, Locked<T> action) throws IOException {
        var lock = LOCKS.computeIfAbsent(file, _ -> new ReentrantLock());
        lock.lock();
        try {
            cache.checkLinks(metaFile(file));
            Files.createDirectories(file.getParent());
            var lockFile = file.resolveSibling(file.getFileName() + Cache.LOCK);
            try (var channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
                 var _ = channel.lock()) {
                return action.run(readMeta(file));
            }
        } finally {
            lock.unlock();
        }
    }

    private static Properties readMeta(Path file) throws IOException {
        var meta = new Properties();
        try (var reader = Files.newBufferedReader(metaFile(file), UTF_8)) {
            meta.load(reader);
        } catch (NoSuchFileException _) {
            return meta;
        } catch (IllegalArgumentException | CharacterCodingException e) {
            CleanroomLog.get().warn("Cache metadata {} is unreadable, discarding it", metaFile(file), e);
            return new Properties();
        }
        var pinned = meta.getProperty(SHA256);
        if (pinned != null && !HEX_SHA256.matcher(pinned).matches()) {
            CleanroomLog.get().warn("Cache metadata {} pins a malformed hash, discarding it", metaFile(file));
            return new Properties();
        }
        return meta;
    }

    // The metadata is replaced whole, so an interruption leaves the previous one
    private void writeMeta(Path file, Properties meta) throws IOException {
        var versions = versions(file);
        cache.checkLinks(versions);
        Files.createDirectories(versions);
        var temp = Files.createTempFile(versions, null, ".part");
        try {
            try (var channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                meta.store(Channels.newWriter(channel, UTF_8), null);
                channel.force(true);
            }
            move(temp, metaFile(file));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static Path metaFile(Path file) {
        return file.resolveSibling(file.getFileName() + Cache.META);
    }

    private static Path versions(Path file) {
        return file.resolveSibling(file.getFileName() + Cache.VERSIONS);
    }

    // A hash prefix keeps paths short, the full hash stays in the metadata
    private static String directory(String hash) {
        return hash.substring(0, 16);
    }

    private static Path stored(Path file, String hash) {
        return versions(file).resolve(directory(hash)).resolve(file.getFileName());
    }

    private static void set(Properties meta, String key, @Nullable String value, boolean clear) {
        if (value != null) {
            meta.setProperty(key, value);
        } else if (clear) {
            meta.remove(key);
        }
    }

    // Runs once per entry per JVM. Superseded versions and temporary files get a grace period, as other processes may still use them
    private static void sweep(Path file, @Nullable String pinned) {
        var current = pinned == null ? null : directory(pinned);
        var expired = Instant.now().minus(GRACE);
        try (var entries = Files.newDirectoryStream(versions(file))) {
            for (var entry : entries) {
                try {
                    if (entry.getFileName().toString().equals(current) || !Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toInstant().isBefore(expired)) {
                        continue;
                    }
                    if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                        Files.deleteIfExists(entry.resolve(file.getFileName()));
                    }
                    Files.deleteIfExists(entry);
                } catch (IOException e) {
                    // Windows cannot delete open files, a later launch retries
                    CleanroomLog.get().debug("Cannot delete superseded cache file {}", entry, e);
                }
            }
        } catch (NoSuchFileException _) {
            // Nothing was ever stored
        } catch (IOException e) {
            CleanroomLog.get().warn("Cannot sweep superseded versions of cache entry {}", file, e);
        }
    }

    private static void touch(Path path) {
        try {
            Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setTimes(FileTime.from(Instant.now()), null, null);
        } catch (IOException e) {
            CleanroomLog.get().debug("Cannot touch superseded cache version {}", path, e);
        }
    }

    private static void await(CompletableFuture<Void> running) throws IOException {
        try {
            running.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted waiting for a concurrent cache ingestion");
        } catch (ExecutionException e) {
            switch (e.getCause()) {
                case IOException cause -> throw cause;
                case RuntimeException cause -> throw cause;
                case Error cause -> throw cause;
                default -> throw new IOException(e.getCause());
            }
        }
    }

    private InputStream open() throws IOException {
        return switch (source) {
            case Resource(var loader, var name) -> {
                var stream = loader.getResourceAsStream(name);
                if (stream == null) {
                    throw new NoSuchFileException(name);
                }
                yield stream;
            }
            case Local(var file) -> Files.newInputStream(file);
            case Bytes(var bytes) -> new ByteArrayInputStream(bytes);
            case Remote _ -> throw new IllegalStateException();
        };
    }

    private boolean outdated(Properties meta) {
        var cached = meta.getProperty(VERSION);
        return version != null && (cached == null || new ComparableVersion(version).compareTo(new ComparableVersion(cached)) > 0);
    }

    private boolean isStale(Properties meta) {
        if (!(source instanceof Remote) || maxAge == null) {
            return false;
        }
        try {
            return System.currentTimeMillis() - Long.parseLong(meta.getProperty(CHECKED, "0")) >= maxAge.toMillis();
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean holds(Path path, String hash) throws IOException {
        try {
            return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && hash.equals(hash(path));
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    private static String write(InputStream input, Path target) throws IOException {
        var digest = sha256();
        try (var in = new DigestInputStream(input, digest);
             var channel = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            in.transferTo(Channels.newOutputStream(channel));
            channel.force(true);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String hash(Path file) throws IOException {
        var digest = sha256();
        try (var in = new DigestInputStream(Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS), digest)) {
            in.transferTo(OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static CompletableFuture<Path> async(Callable<Path> task) {
        var future = new CompletableFuture<Path>();
        Thread.ofVirtual().name("cleanroom-cache").start(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    // Holder, so the client and its threads only exist once something is fetched
    private static final class Http {

        static final Duration TIMEOUT = Duration.ofSeconds(Long.getLong("cleanroom.cache.timeout", 30));

        static final HttpClient CLIENT = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        static final ScheduledThreadPoolExecutor WATCHDOG = new ScheduledThreadPoolExecutor(1, Thread.ofVirtual().name("cleanroom-cache-watchdog").factory());

        static {
            WATCHDOG.setRemoveOnCancelPolicy(true);
        }

    }

}
