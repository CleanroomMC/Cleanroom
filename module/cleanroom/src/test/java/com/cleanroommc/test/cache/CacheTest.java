package com.cleanroommc.test.cache;

import com.cleanroommc.cleanroom.cache.Cache;
import com.cleanroommc.cleanroom.cache.CacheException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class CacheTest {

    static {
        System.setProperty("cleanroom.cache.timeout", "2");
    }

    private static final String HELLO_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

    @TempDir
    Path root;

    private Cache cache;
    private HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();
    private final AtomicInteger notModified = new AtomicInteger();
    private final CountDownLatch revalidated = new CountDownLatch(1);
    private final AtomicInteger failures = new AtomicInteger();
    private final Semaphore served = new Semaphore(0);
    private final CountDownLatch slowRequested = new CountDownLatch(1);
    private final CountDownLatch released = new CountDownLatch(1);

    @BeforeEach
    void setUp() throws IOException {
        System.setProperty("cleanroom.cache.root.instance", root.toString());
        cache = Cache.instance().namespace("test");
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/hello", exchange -> {
            requests.incrementAndGet();
            if ("\"v1\"".equals(exchange.getRequestHeaders().getFirst("If-None-Match"))) {
                notModified.incrementAndGet();
                exchange.sendResponseHeaders(304, -1);
                revalidated.countDown();
            } else {
                exchange.getResponseHeaders().add("ETag", "\"v1\"");
                exchange.sendResponseHeaders(200, 5);
                exchange.getResponseBody().write("hello".getBytes(UTF_8));
            }
            exchange.close();
            served.release();
        });
        server.createContext("/slow", exchange -> {
            if (exchange.getRequestHeaders().containsKey("If-None-Match")) {
                slowRequested.countDown();
                awaitRelease();
                exchange.sendResponseHeaders(304, -1);
            } else {
                exchange.getResponseHeaders().add("ETag", "\"v1\"");
                exchange.sendResponseHeaders(200, 5);
                exchange.getResponseBody().write("hello".getBytes(UTF_8));
            }
            exchange.close();
        });
        server.createContext("/drop", exchange -> {
            exchange.sendResponseHeaders(200, 10);
            exchange.getResponseBody().write("hello".getBytes(UTF_8));
            exchange.close();
        });
        server.createContext("/stall", exchange -> {
            exchange.sendResponseHeaders(200, 10);
            exchange.getResponseBody().write("hello".getBytes(UTF_8));
            exchange.getResponseBody().flush();
            awaitRelease();
            exchange.close();
        });
        server.createContext("/fail", exchange -> {
            failures.incrementAndGet();
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.createContext("/missing", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        released.countDown();
        // Background revalidations hold a temporary file until they commit
        for (var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); ; Thread.sleep(10)) {
            try (var files = Files.walk(root)) {
                if (files.noneMatch(file -> file.toString().endsWith(".part"))) {
                    break;
                }
            } catch (UncheckedIOException ignored) {
                // A file vanished mid-walk
            }
            assertTrue(System.nanoTime() < deadline, "Background cache work did not finish");
        }
        server.stop(0);
    }

    private void awaitRelease() {
        try {
            released.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void clearRoot() {
        System.clearProperty("cleanroom.cache.root.instance");
    }

    private URI url(String path) {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    @Test
    void getKeepsAndPutReplaces() throws IOException {
        var file = cache.fromString("one", UTF_8).get("a/b.txt");
        assertTrue(file.startsWith(root.resolve("test/a")));
        assertEquals("b.txt", file.getFileName().toString());
        assertEquals("one", read(cache.fromString("two", UTF_8).get("a/b.txt")));
        var replaced = cache.fromString("two", UTF_8).put("a/b.txt");
        assertEquals("two", read(replaced));
        assertNotEquals(file, replaced);
        assertEquals("one", read(file));
    }

    @Test
    void supersededVersionsAreSwept() throws IOException {
        var versions = Files.createDirectories(cache.root().resolve("s.versions"));
        var expired = FileTime.from(Instant.now().minus(Duration.ofDays(2)));
        var superseded = Files.createDirectories(versions.resolve("old")).resolve("s");
        Files.writeString(superseded, "old");
        Files.setLastModifiedTime(superseded.getParent(), expired);
        var recent = Files.createDirectories(versions.resolve("recent"));
        var abandoned = Files.createFile(versions.resolve("abandoned.part"));
        Files.setLastModifiedTime(abandoned, expired);
        var running = Files.createFile(versions.resolve("running.part"));
        var current = cache.fromString("new", UTF_8).get("s");
        assertFalse(Files.exists(superseded.getParent()));
        assertFalse(Files.exists(abandoned));
        assertTrue(Files.exists(recent));
        assertTrue(Files.exists(running));

        cache.fromString("newer", UTF_8).put("s");
        assertEquals("new", read(current));
        Files.delete(running);
    }

    @Test
    void expectedHashIsEnforced() throws IOException {
        assertEquals("hello", read(cache.fromString("hello", UTF_8).expectSha256(HELLO_SHA256.toUpperCase()).get("ok")));
        var e = assertThrows(CacheException.class, () -> cache.fromString("other", UTF_8).expectSha256(HELLO_SHA256).get("bad"));
        assertEquals(CacheException.Kind.HASH_MISMATCH, e.kind());
        assertFalse(Files.exists(cache.root().resolve("bad")));
    }

    @Test
    void corruptionIsRestoredFromPinnedSource() throws IOException {
        var source = Files.writeString(root.resolve("source.txt"), "original");
        var file = cache.fromFile(source).get("copy");
        Files.writeString(file, "corrupt");
        assertEquals("original", read(cache.fromFile(source).get("copy")));

        Files.writeString(file, "corrupt");
        Files.writeString(source, "changed");
        var e = assertThrows(CacheException.class, () -> cache.fromFile(source).get("copy"));
        assertEquals(CacheException.Kind.HASH_MISMATCH, e.kind());
    }

    @Test
    void replacedContentDropsItsMetadata() throws Exception {
        cache.fromString("a", UTF_8).version("2.0").get("v");
        cache.fromString("b", UTF_8).put("v");
        assertEquals("c", read(cache.fromString("c", UTF_8).version("1.0").get("v")));

        cache.fromUrl(url("/hello")).get("e");
        assertTrue(served.tryAcquire(10, TimeUnit.SECONDS));
        cache.fromString("other", UTF_8).put("e");
        assertEquals("other", read(cache.fromUrl(url("/hello")).revalidateIf(Duration.ZERO).get("e")));
        assertTrue(served.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals(0, notModified.get());
    }

    @Test
    void failedRevalidationWaitsForTheNextPeriod() throws Exception {
        cache.fromUrl(url("/hello")).get("r");
        var meta = cache.root().resolve("r.meta");
        Files.writeString(meta, Files.readString(meta).replaceAll("checked=\\d+", "checked=0"));
        for (int i = 0; i < 5; i++) {
            assertEquals("hello", read(cache.fromUrl(url("/fail")).revalidateIf(Duration.ofMinutes(1)).get("r")));
        }
        Thread.sleep(500);
        assertEquals(1, failures.get());
    }

    @Test
    void malformedMetadataIsDiscarded() throws IOException {
        cache.fromString("x", UTF_8).get("m");
        Files.writeString(cache.root().resolve("m.meta"), "sha256=../../../escape\nchecked=soon\n");
        assertEquals("y", read(cache.fromString("y", UTF_8).get("m")));
    }

    @Test
    void stalledBodyFailsOffline() {
        var e = assertThrows(CacheException.class, () -> cache.fromUrl(url("/stall")).get("stall"));
        assertEquals(CacheException.Kind.OFFLINE, e.kind());
    }

    @Test
    void bytesAreCopied() throws IOException {
        var bytes = "hello".getBytes(UTF_8);
        var request = cache.fromBytes(bytes);
        bytes[0] = 'j';
        assertEquals("hello", read(request.get("copied")));
    }

    @Test
    void greaterVersionReingests() throws IOException {
        cache.fromString("1", UTF_8).version("1.0").get("v");
        assertEquals("2", read(cache.fromString("2", UTF_8).version("1.10").get("v")));
        assertEquals("2", read(cache.fromString("3", UTF_8).version("1.9").get("v")));
    }

    @Test
    void resourcesAndBuffersIngest() throws IOException {
        var loader = getClass().getClassLoader();
        var file = cache.fromResource(loader, "com/cleanroommc/test/cache/CacheTest.class").get("self.class");
        assertTrue(Files.size(file) > 0);
        var e = assertThrows(IOException.class, () -> cache.fromResource(loader, "nope").get("nope"));
        assertInstanceOf(java.nio.file.NoSuchFileException.class, e);
        assertEquals("hello", read(cache.fromBuffer(UTF_8.encode("hello")).get("buffer")));
    }

    @Test
    void pathsStayConfined() throws IOException {
        for (var name : List.of("../x", "a/../../x", "/abs", "C:/x", "\\\\server\\share", "a\0b", ".", "x.meta", "x.lock", "x.versions", "a.versions/x")) {
            assertThrows(IllegalArgumentException.class, () -> cache.fromString("x", UTF_8).get(name), name);
        }
        assertThrows(IllegalArgumentException.class, () -> cache.namespace(".."));
        assertThrows(IllegalArgumentException.class, () -> cache.namespace("a.versions"));
        assertEquals(cache.fromString("x", UTF_8).get("b"), cache.fromString("x", UTF_8).get("a/../b"));
    }

    @Test
    void symbolicLinksAreRejected() throws IOException {
        Files.createDirectories(cache.root());
        try {
            Files.createSymbolicLink(cache.root().resolve("link"), Files.createDirectories(root.resolve("elsewhere")));
        } catch (UnsupportedOperationException | IOException e) {
            org.junit.jupiter.api.Assumptions.abort("Symbolic links unavailable: " + e);
        }
        var e = assertThrows(CacheException.class, () -> cache.fromString("x", UTF_8).get("link/x"));
        assertEquals(CacheException.Kind.UNSAFE_PATH, e.kind());
    }

    @Test
    void concurrentGetsFetchOnce() {
        var futures = IntStream.range(0, 16).mapToObj(_ -> cache.fromUrl(url("/hello")).getAsync("hello")).toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        futures.forEach(future -> assertEquals("hello", read(future.join())));
        assertEquals(1, requests.get());
    }

    @Test
    void staleEntriesRevalidateInTheBackground() throws Exception {
        cache.fromUrl(url("/hello")).get("hello");
        cache.fromUrl(url("/hello")).revalidateIf(Duration.ofHours(1)).get("hello");
        assertEquals(1, requests.get());

        assertEquals("hello", read(cache.fromUrl(url("/hello")).revalidateIf(Duration.ZERO).get("hello")));
        assertTrue(revalidated.await(10, TimeUnit.SECONDS));
        assertEquals(1, notModified.get());
    }

    @Test
    void revalidationDoesNotBlockReaders() throws Exception {
        cache.fromUrl(url("/slow")).get("slow");
        try {
            cache.fromUrl(url("/slow")).revalidateIf(Duration.ZERO).get("slow");
            assertTrue(slowRequested.await(10, TimeUnit.SECONDS));
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertEquals("hello", read(cache.fromUrl(url("/slow")).get("slow"))));
        } finally {
            released.countDown();
        }
    }

    @Test
    void offlineServesWarmAndFailsCold() throws IOException {
        cache.fromUrl(url("/hello")).get("hello");
        URI dead;
        try (var socket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            dead = URI.create("http://127.0.0.1:" + socket.getLocalPort() + "/hello");
        }
        assertEquals("hello", read(cache.fromUrl(dead).get("hello")));
        var e = assertThrows(CacheException.class, () -> cache.fromUrl(dead).get("cold"));
        assertEquals(CacheException.Kind.OFFLINE, e.kind());
        cache.fromUrl(url("/hello")).version("1.0").get("versioned");
        e = assertThrows(CacheException.class, () -> cache.fromUrl(dead).version("2.0").get("versioned"));
        assertEquals(CacheException.Kind.OFFLINE, e.kind());
        assertTrue(e.getMessage().contains("cached at version 1.0 but 2.0 is required"), e.getMessage());
        e = assertThrows(CacheException.class, () -> cache.fromUrl(url("/drop")).get("drop"));
        assertEquals(CacheException.Kind.OFFLINE, e.kind());
        e = assertThrows(CacheException.class, () -> cache.fromUrl(url("/missing")).get("missing"));
        assertEquals(CacheException.Kind.HTTP, e.kind());
    }

    private String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
