/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.cache;

import com.cleanroommc.cleanroom.cache.Cache;
import com.cleanroommc.cleanroom.cache.CacheException;
import com.cleanroommc.cleanroom.cache.CacheRequest;

import com.sun.net.httpserver.HttpServer;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.ThrowableAssertAlternative;
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
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class CacheTest {

    static {
        System.setProperty("cleanroom.cache.timeout", "2");
    }

    private static final String HELLO_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

    @TempDir Path root;

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
        for (long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);; Thread.sleep(10)) {
            try (Stream<Path> files = Files.walk(root)) {
                if (files.noneMatch(file -> file.toString().endsWith(".part"))) {
                    break;
                }
            } catch (UncheckedIOException ignored) {
                // A file vanished mid-walk
            }
            assertThat(System.nanoTime()).as("Background cache work did not finish").isLessThan(deadline);
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
        Path file = cache.fromString("one", UTF_8).get("a/b.txt");
        assertThat(file).startsWithRaw(root.resolve("test/a"));
        assertThat(file).hasFileName("b.txt");
        assertThat(read(cache.fromString("two", UTF_8).get("a/b.txt"))).isEqualTo("one");
        Path replaced = cache.fromString("two", UTF_8).put("a/b.txt");
        assertThat(read(replaced)).isEqualTo("two");
        assertThat(replaced).isNotEqualTo(file);
        assertThat(read(file)).isEqualTo("one");
    }

    @Test
    void supersededVersionsAreSwept() throws IOException {
        Path versions = Files.createDirectories(cache.root().resolve("s.versions"));
        FileTime expired = FileTime.from(Instant.now().minus(Duration.ofDays(2)));
        Path superseded = Files.createDirectories(versions.resolve("old")).resolve("s");
        Files.writeString(superseded, "old");
        Files.setLastModifiedTime(superseded.getParent(), expired);
        Path recent = Files.createDirectories(versions.resolve("recent"));
        Path abandoned = Files.createFile(versions.resolve("abandoned.part"));
        Files.setLastModifiedTime(abandoned, expired);
        Path running = Files.createFile(versions.resolve("running.part"));
        Path current = cache.fromString("new", UTF_8).get("s");
        assertThat(superseded.getParent()).doesNotExist();
        assertThat(abandoned).doesNotExist();
        assertThat(recent).exists();
        assertThat(running).exists();

        cache.fromString("newer", UTF_8).put("s");
        assertThat(read(current)).isEqualTo("new");
        Files.delete(running);
    }

    @Test
    void expectedHashIsEnforced() throws IOException {
        assertThat(read(cache.fromString("hello", UTF_8).expectSha256(HELLO_SHA256.toUpperCase()).get("ok")
        )).isEqualTo("hello");
        assertFails(CacheException.Kind.HASH_MISMATCH,
            () -> cache.fromString("other", UTF_8).expectSha256(HELLO_SHA256).get("bad")
        );
        assertThat(cache.root().resolve("bad")).doesNotExist();
    }

    @Test
    void corruptionIsRestoredFromPinnedSource() throws IOException {
        Path source = Files.writeString(root.resolve("source.txt"), "original");
        Path file = cache.fromFile(source).get("copy");
        Files.writeString(file, "corrupt");
        assertThat(read(cache.fromFile(source).get("copy"))).isEqualTo("original");

        Files.writeString(file, "corrupt");
        Files.writeString(source, "changed");
        assertFails(CacheException.Kind.HASH_MISMATCH, () -> cache.fromFile(source).get("copy"));
    }

    @Test
    void replacedContentDropsItsMetadata() throws Exception {
        cache.fromString("a", UTF_8).version("2.0").get("v");
        cache.fromString("b", UTF_8).put("v");
        assertThat(read(cache.fromString("c", UTF_8).version("1.0").get("v"))).isEqualTo("c");

        cache.fromUrl(url("/hello")).get("e");
        assertThat(served.tryAcquire(10, TimeUnit.SECONDS)).isTrue();
        cache.fromString("other", UTF_8).put("e");
        assertThat(read(cache.fromUrl(url("/hello")).revalidateIf(Duration.ZERO).get("e"))).isEqualTo("other");
        assertThat(served.tryAcquire(10, TimeUnit.SECONDS)).isTrue();
        assertThat(notModified.get()).isEqualTo(0);
    }

    @Test
    void failedRevalidationWaitsForTheNextPeriod() throws Exception {
        cache.fromUrl(url("/hello")).get("r");
        Path meta = cache.root().resolve("r.meta");
        Files.writeString(meta, Files.readString(meta).replaceAll("checked=\\d+", "checked=0"));
        for (int i = 0; i < 5; i++) {
            assertThat(read(cache.fromUrl(url("/fail")).revalidateIf(Duration.ofMinutes(1)).get("r"))).isEqualTo("hello"
            );
        }
        Thread.sleep(500);
        assertThat(failures.get()).isEqualTo(1);
    }

    @Test
    void malformedMetadataIsDiscarded() throws IOException {
        cache.fromString("x", UTF_8).get("m");
        Files.writeString(cache.root().resolve("m.meta"), "sha256=../../../escape\nchecked=soon\n");
        assertThat(read(cache.fromString("y", UTF_8).get("m"))).isEqualTo("y");
    }

    @Test
    void stalledBodyFailsOffline() {
        assertFails(CacheException.Kind.OFFLINE, () -> cache.fromUrl(url("/stall")).get("stall"));
    }

    @Test
    void bytesAreCopied() throws IOException {
        byte[] bytes = "hello".getBytes(UTF_8);
        CacheRequest request = cache.fromBytes(bytes);
        bytes[0] = 'j';
        assertThat(read(request.get("copied"))).isEqualTo("hello");
    }

    @Test
    void lazyContentIsProducedOnlyOnMiss() throws IOException {
        AtomicInteger calls = new AtomicInteger();
        CacheRequest request = cache.fromBytes(() -> ("build" + calls.incrementAndGet()).getBytes(UTF_8));
        Path file = request.get("lazy");
        assertThat(read(request.get("lazy"))).isEqualTo("build1");
        assertThat(calls.get()).isEqualTo(1);
        Files.writeString(file, "corrupt");
        assertThat(read(request.get("lazy"))).isEqualTo("build2");
        assertThat(read(cache.fromBuffer(() -> UTF_8.encode("hello")).get("lazyBuffer"))).isEqualTo("hello");
    }

    @Test
    void greaterVersionReingests() throws IOException {
        cache.fromString("1", UTF_8).version("1.0").get("v");
        assertThat(read(cache.fromString("2", UTF_8).version("1.10").get("v"))).isEqualTo("2");
        assertThat(read(cache.fromString("3", UTF_8).version("1.9").get("v"))).isEqualTo("2");
    }

    @Test
    void resourcesAndBuffersIngest() throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        Path file = cache.fromResource(loader, "com/cleanroommc/test/cache/CacheTest.class").get("self.class");
        assertThat(file).isNotEmptyFile();
        assertThatExceptionOfType(java.nio.file.NoSuchFileException.class).isThrownBy(() -> cache.fromResource(loader,
            "nope"
        )
            .get("nope"));
        assertThat(read(cache.fromBuffer(UTF_8.encode("hello")).get("buffer"))).isEqualTo("hello");
    }

    @Test
    void pathsStayConfined() throws IOException {
        for (String name : List.of("../x", "a/../../x", "/abs", "C:/x", "\\\\server\\share", "a\0b", ".", "x.meta",
            "x.lock", "x.versions", "a.versions/x"
        )) {
            assertThatIllegalArgumentException().as(name).isThrownBy(() -> cache.fromString("x", UTF_8).get(name));
        }
        assertThatIllegalArgumentException().isThrownBy(() -> cache.namespace(".."));
        assertThatIllegalArgumentException().isThrownBy(() -> cache.namespace("a.versions"));
        assertThat(cache.fromString("x", UTF_8).get("a/../b")).isEqualTo(cache.fromString("x", UTF_8).get("b"));
    }

    @Test
    void symbolicLinksAreRejected() throws IOException {
        Files.createDirectories(cache.root());
        try {
            Files.createSymbolicLink(cache.root().resolve("link"), Files.createDirectories(root.resolve("elsewhere")));
        } catch (UnsupportedOperationException | IOException e) {
            org.junit.jupiter.api.Assumptions.abort("Symbolic links unavailable: " + e);
        }
        assertFails(CacheException.Kind.UNSAFE_PATH, () -> cache.fromString("x", UTF_8).get("link/x"));
    }

    @Test
    void concurrentGetsFetchOnce() {
        List<CompletableFuture<Path>> futures = IntStream.range(0, 16)
            .mapToObj(_ -> cache.fromUrl(url("/hello")).getAsync("hello"))
            .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        futures.forEach(future -> assertThat(read(future.join())).isEqualTo("hello"));
        assertThat(requests.get()).isEqualTo(1);
    }

    @Test
    void putAsyncAlwaysIngests() {
        cache.fromUrl(url("/hello")).putAsync("hello").join();
        assertThat(read(cache.fromUrl(url("/hello")).putAsync("hello").join())).isEqualTo("hello");
        assertThat(requests.get()).isEqualTo(2);
    }

    @Test
    void asyncFailuresCompleteExceptionally() {
        CompletableFuture<Path> future = cache.fromUrl(url("/stall")).getAsync("stall");
        assertThat(future.isDone()).isFalse();
        assertThatExceptionOfType(CompletionException.class)
            .isThrownBy(future::join)
            .havingCause()
            .isInstanceOfSatisfying(CacheException.class,
                e -> assertThat(e.kind()).isEqualTo(CacheException.Kind.OFFLINE)
            );
    }

    @Test
    void asyncRunsOnAVirtualThread() {
        AtomicBoolean virtual = new AtomicBoolean();
        cache.fromBytes(() -> {
            virtual.set(Thread.currentThread().isVirtual());
            return "hello".getBytes(UTF_8);
        }).getAsync("virtual").join();
        assertThat(virtual.get()).isTrue();
    }

    @Test
    void staleEntriesRevalidateInTheBackground() throws Exception {
        cache.fromUrl(url("/hello")).get("hello");
        cache.fromUrl(url("/hello")).revalidateIf(Duration.ofHours(1)).get("hello");
        assertThat(requests.get()).isEqualTo(1);

        assertThat(read(cache.fromUrl(url("/hello")).revalidateIf(Duration.ZERO).get("hello"))).isEqualTo("hello");
        assertThat(revalidated.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(notModified.get()).isEqualTo(1);
    }

    @Test
    void revalidationDoesNotBlockReaders() throws Exception {
        cache.fromUrl(url("/slow")).get("slow");
        try {
            cache.fromUrl(url("/slow")).revalidateIf(Duration.ZERO).get("slow");
            assertThat(slowRequested.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(cache.fromUrl(url("/slow")).getAsync("slow"))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(file -> assertThat(read(file)).isEqualTo("hello"));
        } finally {
            released.countDown();
        }
    }

    @Test
    void offlineServesWarmAndFailsCold() throws IOException {
        cache.fromUrl(url("/hello")).get("hello");
        URI dead;
        try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            dead = URI.create("http://127.0.0.1:" + socket.getLocalPort() + "/hello");
        }
        assertThat(read(cache.fromUrl(dead).get("hello"))).isEqualTo("hello");
        assertFails(CacheException.Kind.OFFLINE, () -> cache.fromUrl(dead).get("cold"));
        cache.fromUrl(url("/hello")).version("1.0").get("versioned");
        assertFails(CacheException.Kind.OFFLINE, () -> cache.fromUrl(dead).version("2.0").get("versioned")
        ).withMessageContaining("cached at version 1.0 but 2.0 is required");
        assertFails(CacheException.Kind.OFFLINE, () -> cache.fromUrl(url("/drop")).get("drop"));
        assertFails(CacheException.Kind.HTTP, () -> cache.fromUrl(url("/missing")).get("missing"));
    }

    private static ThrowableAssertAlternative<CacheException> assertFails(CacheException.Kind kind,
        ThrowingCallable call
    ) {
        return assertThatExceptionOfType(CacheException.class)
            .isThrownBy(call)
            .satisfies(e -> assertThat(e.kind()).isEqualTo(kind));
    }

    private String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
