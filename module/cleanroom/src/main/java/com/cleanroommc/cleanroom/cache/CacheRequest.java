package com.cleanroommc.cleanroom.cache;

import com.cleanroommc.util.CleanroomLog;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.MoreFiles;
import org.jspecify.annotations.Nullable;

import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Describes where an entry comes from and how it is trusted, then fetches it with {@link #get} or {@link #put}.
 * <p>
 * A request is a builder for a single caller. The cache behind it is safe across threads and processes.
 */
public final class CacheRequest {

    private static final String SHA256 = "sha256",
        VERSION = "version",
        CHECKED = "checked",
        URL = "url",
        ETAG = "etag",
        MODIFIED = "modified";
    private static final Duration GRACE = Duration.ofDays(1);

    // Serializes threads of this JVM, file locks only exclude other processes
    private static final ConcurrentHashMap<Path, ReentrantLock> LOCKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<
        Path, CompletableFuture<@Nullable Void>> INGESTING = new ConcurrentHashMap<>();
    private static final Set<Path> SWEPT = ConcurrentHashMap.newKeySet();

    private final Cache cache;
    private final Source source;

    private @Nullable String sha256;
    private @Nullable String version;
    private @Nullable Duration maxAge;

    CacheRequest(Cache cache, Source source) {
        this.cache = cache;
        this.source = source;
    }

    // Read failures, including a body stalled past the timeout, mean the source was lost
    private static InputStream guard(InputStream body, URI uri) {
        return new FilterInputStream(body) {

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                // Whoever takes the reader first decides whether the interrupt happens
                AtomicReference<@Nullable Thread> reader = new AtomicReference<>(Thread.currentThread());
                ScheduledFuture<?> watchdog = Http.WATCHDOG.schedule(() -> {
                        synchronized (reader) {
                            Thread thread = reader.getAndSet(null);
                            if (thread != null) {
                                thread.interrupt();
                            }
                        }
                    },
                    Http.TIMEOUT.toMillis(), TimeUnit.MILLISECONDS
                );
                try {
                    return super.read(b, off, len);
                } catch (IOException e) {
                    throw new CacheException(CacheException.Kind.OFFLINE, "Lost connection fetching " + uri, e);
                } finally {
                    watchdog.cancel(false);
                    // An interrupt that fired as the read returned must not reach the file write
                    synchronized (reader) {
                        if (reader.getAndSet(null) == null) {
                            Thread.interrupted();
                        }
                    }
                }
            }

        };
    }

    private static Properties readMeta(Path file) throws IOException {
        Properties meta = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(metaFile(file), StandardCharsets.UTF_8)) {
            meta.load(reader);
        } catch (NoSuchFileException _) {
            return meta;
        } catch (IllegalArgumentException | CharacterCodingException e) {
            CleanroomLog.get().warn("Cache metadata {} is unreadable, discarding it", metaFile(file), e);
            return new Properties();
        }
        String pinned = meta.getProperty(SHA256);
        if (pinned != null && !isSha256(pinned)) {
            CleanroomLog.get().warn("Cache metadata {} pins a malformed hash, discarding it", metaFile(file));
            return new Properties();
        }
        return meta;
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
        String current = pinned == null ? null : directory(pinned);
        Instant expired = Instant.now().minus(GRACE);
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(versions(file))) {
            for (Path entry : entries) {
                try {
                    if (entry.getFileName().toString().equals(current) ||
                        !Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toInstant().isBefore(expired)) {
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
            Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS)
                .setTimes(FileTime.from(Instant.now()), null, null);
        } catch (IOException e) {
            CleanroomLog.get().debug("Cannot touch superseded cache version {}", path, e);
        }
    }

    private static void await(CompletableFuture<@Nullable Void> running) throws IOException {
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

    private static boolean isSha256(String hex) {
        if (hex.length() != 64) {
            return false;
        }
        for (int i = 0; i < 64; i++) {
            char c = hex.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
                return false;
            }
        }
        return true;
    }

    private static boolean holds(Path path, String hash) throws IOException {
        try {
            return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && hash.equals(hash(path));
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    private static String write(InputStream input, Path target) throws IOException {
        try (HashingInputStream in = new HashingInputStream(Hashing.sha256(), input
        ); FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
        )) {
            in.transferTo(Channels.newOutputStream(channel));
            channel.force(true);
            return in.hash().toString();
        }
    }

    private static String hash(Path file) throws IOException {
        return MoreFiles.asByteSource(file, LinkOption.NOFOLLOW_LINKS).hash(Hashing.sha256()).toString();
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static CompletableFuture<Path> async(Callable<Path> task) {
        CompletableFuture<Path> future = new CompletableFuture<>();
        Thread.ofVirtual().name("cleanroom-cache").start(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Content must match this SHA-256, otherwise it is pinned on first ingestion and verified from then on.
     */
    public CacheRequest expectSha256(String hex) {
        hex = hex.toLowerCase(Locale.ROOT);
        Preconditions.checkArgument(isSha256(hex), "SHA-256 string is malformed: %s", hex);

        this.sha256 = hex;
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
     * Ingests the entry only when it is missing, corrupt, outdated or expected with another hash.
     *
     * @param name the entry's path relative to the cache root
     * @return the verified content, at a path that changes with the content
     * @throws CacheException if the source is unreachable or untrusted, see {@link CacheException.Kind}
     */
    public Path get(String name) throws IOException {
        return fetch(name, Mode.GET);
    }

    /**
     * Always ingests the entry from the source.
     *
     * @param name the entry's path relative to the cache root
     * @return the verified content, at a path that changes with the content
     * @throws CacheException if the source is unreachable or untrusted, see {@link CacheException.Kind}
     */
    public Path put(String name) throws IOException {
        return fetch(name, Mode.PUT);
    }

    /**
     * {@link #get} on a virtual thread, failures complete the future exceptionally.
     */
    public CompletableFuture<Path> getAsync(String name) {
        return async(() -> get(name));
    }

    /**
     * {@link #put} on a virtual thread, failures complete the future exceptionally.
     */
    public CompletableFuture<Path> putAsync(String name) {
        return async(() -> put(name));
    }

    private Path fetch(String name, Mode mode) throws IOException {
        Path file = cache.entry(name);
        if (mode == Mode.PUT) {
            return ingest(file, new Properties(), sha256, false);
        }
        Lookup lookup = lookup(file);
        if (mode == Mode.REVALIDATE) {
            return ingest(file, lookup.meta(), lookup.expected(), lookup.path() != null);
        }
        if (lookup.path() != null) {
            if (lookup.stale() && claimRevalidation(file, lookup.meta().getProperty(SHA256))) {
                async(() -> fetch(name, Mode.REVALIDATE)).whenComplete((_, t) -> {
                    if (t != null) {
                        CleanroomLog.get().warn("Cannot revalidate cache entry {}, keeping it", file, t);
                    }
                });
            }
            return lookup.path();
        }
        // Concurrent gets of one entry share a single ingestion, then look again
        CompletableFuture<@Nullable Void> ingesting = new CompletableFuture<>();
        CompletableFuture<@Nullable Void> running = INGESTING.putIfAbsent(file, ingesting);
        if (running != null) {
            await(running);
            return fetch(name, mode);
        }
        try {
            lookup = lookup(file);
            Path path = lookup.path() != null ? lookup.path() : ingest(file, lookup.meta(), lookup.expected(), false);
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
        Properties meta = locked(file, current -> {
            if (SWEPT.add(file)) {
                sweep(file, current.getProperty(SHA256));
            }
            return current;
        });
        String pinned = meta.getProperty(SHA256);
        if (pinned == null || outdated(meta) || (sha256 != null && !sha256.equals(pinned))) {
            return new Lookup(meta, null, false, sha256);
        }
        // Stored versions never change, so they are verified unlocked
        Path path = stored(file, pinned);
        cache.checkLinks(path);
        if (holds(path, pinned)) {
            return new Lookup(meta, path, isStale(meta), sha256);
        }
        CleanroomLog.get().warn("Cache entry {} is missing or corrupt, restoring it", path);
        return new Lookup(meta, null, false, source instanceof Lazy ? sha256 : pinned);
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
        Path versions = versions(file);
        cache.checkLinks(versions);
        Files.createDirectories(versions);
        Path temp = Files.createTempFile(versions, null, ".part");
        try {
            Fetched fetched;
            if (source instanceof Remote(URI uri)) {
                fetched = download(uri, file, seen, conditional, temp);
            } else {
                try (InputStream input = open()) {
                    fetched = new Fetched(write(input, temp), null, null);
                }
            }
            if (fetched.hash() != null && expected != null && !expected.equals(fetched.hash())) {
                throw new CacheException(CacheException.Kind.HASH_MISMATCH,
                    "Expected SHA-256 " + expected + " for " + file + ", got " + fetched.hash()
                );
            }
            return commit(file, seen, temp, fetched);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private Path commit(Path file, Properties seen, Path temp, Fetched fetched) throws IOException {
        return locked(file, meta -> {
            String previous = meta.getProperty(SHA256);
            String hash = fetched.hash();
            String current;
            if (hash == null) {
                // A 304 only vouches for the content it was asked about
                current = Objects.requireNonNull(seen.getProperty(SHA256));
                if (!current.equals(previous)) {
                    return stored(file, current);
                }
            } else {
                current = hash;
                Path target = stored(file, hash);
                cache.checkLinks(target);
                // Identical content is kept, it may be open elsewhere
                if (!holds(target, hash)) {
                    Files.createDirectories(target.getParent());
                    move(temp, target);
                }
                meta.setProperty(SHA256, hash);
            }
            // Descriptions of replaced content are dropped, unchanged content keeps what this fetch does not restate
            boolean changed = hash != null && !hash.equals(previous);
            if (hash != null) {
                set(meta, VERSION, version, changed);
                set(meta, URL, source instanceof Remote(URI uri) ? uri.toString() : null, changed);
            }
            set(meta, ETAG, fetched.etag(), changed);
            set(meta, MODIFIED, fetched.modified(), changed);
            meta.setProperty(CHECKED, Long.toString(System.currentTimeMillis()));
            writeMeta(file, meta);
            if (changed && previous != null) {
                // Starts the superseded version's grace period
                touch(versions(file).resolve(directory(previous)));
            }
            return stored(file, current);
        });
    }

    private Fetched download(URI uri, Path file, Properties seen, boolean conditional, Path temp) throws IOException {
        if (Boolean.getBoolean("cleanroom.cache.offline")) {
            throw offline(file, seen, "Offline, cannot fetch " + uri, null);
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(Http.TIMEOUT);
        // Validators only describe what the URL they came from served
        boolean validate = conditional && uri.toString().equals(seen.getProperty(URL));
        if (validate) {
            String etag = seen.getProperty(ETAG);
            if (etag != null) {
                request.header("If-None-Match", etag);
            }
            String modified = seen.getProperty(MODIFIED);
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
        try (InputStream body = response.body()) {
            String hash = null;
            if (response.statusCode() == 200) {
                hash = write(guard(body, uri), temp);
            } else if (response.statusCode() != 304 || !validate) {
                throw new CacheException(CacheException.Kind.HTTP, "HTTP " + response.statusCode() + " fetching " + uri
                );
            }
            return new Fetched(hash, response.headers().firstValue("ETag").orElse(null),
                response.headers().firstValue("Last-Modified").orElse(null)
            );
        }
    }

    private CacheException offline(Path file, Properties seen, String message, @Nullable Throwable cause) {
        if (seen.getProperty(SHA256) != null && outdated(seen)) {
            message += ", " + file + " is cached at version " + seen.getProperty(VERSION, "unknown") + " but " +
                version + " is required";
        }
        return new CacheException(CacheException.Kind.OFFLINE, message, cause);
    }

    private <T> T locked(Path file, Locked<T> action) throws IOException {
        cache.checkLinks(metaFile(file));
        Files.createDirectories(file.getParent());
        Path lockFile = file.resolveSibling(file.getFileName() + Cache.LOCK);
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
            LinkOption.NOFOLLOW_LINKS
        )) {
            ReentrantLock lock = LOCKS.computeIfAbsent(lockFile.toRealPath(), _ -> new ReentrantLock());
            lock.lock();
            try (FileLock _ = channel.lock()) {
                return action.run(readMeta(file));
            } finally {
                lock.unlock();
            }
        }
    }

    // The metadata is replaced whole, so an interruption leaves the previous one
    private void writeMeta(Path file, Properties meta) throws IOException {
        Path versions = versions(file);
        cache.checkLinks(versions);
        Files.createDirectories(versions);
        Path temp = Files.createTempFile(versions, null, ".part");
        try {
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                meta.store(Channels.newWriter(channel, StandardCharsets.UTF_8), null);
                channel.force(true);
            }
            move(temp, metaFile(file));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private InputStream open() throws IOException {
        return switch (source) {
            case Resource(ClassLoader loader, String name) -> {
                InputStream stream = loader.getResourceAsStream(name);
                if (stream == null) {
                    throw new NoSuchFileException(name);
                }
                yield stream;
            }
            case Local(Path file) -> Files.newInputStream(file);
            case Bytes(byte[] bytes) -> new ByteArrayInputStream(bytes);
            case Lazy(Callable<byte[]> content) -> {
                try {
                    yield new ByteArrayInputStream(content.call());
                } catch (IOException | RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
            case Remote _ -> throw new IllegalStateException();
        };
    }

    private boolean outdated(Properties meta) {
        String cached = meta.getProperty(VERSION);
        return version != null &&
            (cached == null || new ComparableVersion(version).compareTo(new ComparableVersion(cached)) > 0);
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

    private enum Mode {

        GET,
        PUT,
        REVALIDATE

    }

    sealed interface Source { }

    @FunctionalInterface
    private interface Locked<T> {

        T run(Properties meta) throws IOException;

    }

    record Remote(URI uri) implements Source { }

    record Resource(ClassLoader loader, String name) implements Source { }

    record Local(Path file) implements Source { }

    record Bytes(byte[] bytes) implements Source { }

    record Lazy(Callable<byte[]> content) implements Source { }

    /**
     * @param path     the verified content, null when it has to be ingested
     * @param expected the hash ingested content must match
     */
    private record Lookup(Properties meta, @Nullable Path path, boolean stale, @Nullable String expected) { }

    /**
     * @param hash null when the server confirmed the cached content is current
     */
    private record Fetched(@Nullable String hash, @Nullable String etag, @Nullable String modified) { }

    // Holder, so the client and its threads only exist once something is fetched
    private static final class Http {

        static final Duration TIMEOUT = Duration.ofSeconds(Long.getLong("cleanroom.cache.timeout", 30));

        static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        static final ScheduledThreadPoolExecutor WATCHDOG = new ScheduledThreadPoolExecutor(1,
            Thread.ofVirtual().name("cleanroom-cache-watchdog").factory()
        );

        static {
            WATCHDOG.setRemoveOnCancelPolicy(true);
        }

    }

}
