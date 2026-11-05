/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.language.scala;

import com.cleanroommc.cleanroom.cache.Cache;
import com.cleanroommc.discovery.CleanroomModDiscoverer;
import com.cleanroommc.discovery.DiscoveredMod;
import com.cleanroommc.language.scala.ScalaUsage.Kind;
import com.cleanroommc.util.CleanroomLog;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Provides Scala to mods without Scala mods of their own. Scala 2.11 lives in {@code scala211}, so mods written in it
 * are rewritten to match, and Scala 3 keeps {@code scala} untouched. Both runtimes and the rewritten jars are cached.
 */
public final class ScalaProvider {

    // Bump when ScalaRemapper's output changes, so cached jars are rewritten again
    private static final String REMAPPER_VERSION = "1";
    private static final String CENTRAL = "https://repo1.maven.org/maven2/";
    private static final Artifact SCALA_211 = new Artifact(
        "https://maven.cleanroommc.com/com/cleanroommc/scala211/1.0.0/scala211-1.0.0.jar",
        "99e99f89d3a36d354e48787d22a2ee57bb6c254ac856f29d54132a9f00685490"
    );
    private static final List<Artifact> SCALA_3 = List.of(
        new Artifact(
            CENTRAL + "org/scala-lang/scala-library/3.10.0/scala-library-3.10.0.jar",
            "b47befb29e02bbc08a10bbd387b35cf80ff9bf04fda8fdf3dda6c8ad6895f325"
        ),
        new Artifact(
            CENTRAL + "org/scala-lang/modules/scala-parser-combinators_3/2.5.0/scala-parser-combinators_3-2.5.0.jar",
            "b0bd92c899104db8ce82b675a6bc54cfd9da80b81a6ce4c15451819dfd5b12cc"
        )
    );
    private static final Artifact CONFIG = new Artifact(
        CENTRAL + "com/typesafe/config/1.2.1/config-1.2.1.jar",
        "c160fbd78f51a0c2375a794e435ce2112524a6871f64d0331895e9e26ee8b9ee"
    );

    private ScalaProvider() { }

    /**
     * Fetches the runtimes the mods need onto the classpath and rewrites the jars that link against Scala 2.11.
     * Jars already on the classpath are left alone, as they cannot be swapped.
     *
     * @param onRewritten receives each rewritten mod as soon as its jar is ready
     */
    public static void provide(Collection<DiscoveredMod> mods, Consumer<DiscoveredMod> onRewritten) {
        List<DiscoveredMod> swappable = new ArrayList<>();
        Set<String> modernMods = new HashSet<>();
        boolean legacy = false;
        boolean modern = false;
        boolean config = false;
        for (DiscoveredMod mod : mods) {
            Kind kind = mod.scala().kind();
            config |= mod.scala().config();
            if (kind == Kind.MODERN) {
                modern = true;
                modernMods.addAll(mod.modIds());
            }
            // Development puts Scala and the mods being developed on the classpath itself
            if (!mod.onClasspath() && !mod.isDirectoryBased()) {
                swappable.add(mod);
                legacy |= kind == Kind.LEGACY;
            }
        }
        modern &= Launch.classLoader.findResource("scala/Predef.class") == null;
        config &= Launch.classLoader.findResource("com/typesafe/config/Config.class") == null;

        Map<DiscoveredMod, Boolean> rewrites = new HashMap<>();
        for (DiscoveredMod mod : swappable) {
            Kind kind = mod.scala().kind();
            // Java addons follow the mods they were written against
            boolean addsOnModern = kind == Kind.LINKED && mod.dependencies().stream().anyMatch(modernMods::contains);
            if (kind == Kind.SHIM || kind == Kind.LEGACY || kind == Kind.LINKED && legacy && !addsOnModern) {
                rewrites.put(mod, kind != Kind.SHIM);
            }
        }
        List<Artifact> runtimes = new ArrayList<>();
        if (legacy) {
            runtimes.add(SCALA_211);
        }
        if (modern) {
            runtimes.addAll(SCALA_3);
        }
        if (config) {
            runtimes.add(CONFIG);
        }
        if (runtimes.isEmpty() && rewrites.isEmpty()) {
            return;
        }

        long start = System.nanoTime();
        Cache cache = Cache.global().namespace("scala");
        int remapped = 0;
        // Rewriting is CPU bound and virtual threads share one carrier per core, so at most one jar runs per core
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<Artifact, Future<File>> fetched = new LinkedHashMap<>();
            runtimes.forEach(runtime -> fetched.put(runtime, runtime.fetch(cache)));
            Map<DiscoveredMod, Future<File>> rewritten = new LinkedHashMap<>();
            rewrites.forEach((mod, toLegacy) -> rewritten.put(
                mod, executor.submit(() -> rewrite(cache, mod.file(), toLegacy))
            ));
            for (Map.Entry<Artifact, Future<File>> entry : fetched.entrySet()) {
                File runtime;
                try {
                    runtime = join(entry.getValue());
                } catch (RuntimeException e) {
                    throw new RuntimeException("Failed to fetch Scala from " + entry.getKey().url(), e);
                }
                Launch.classLoader.addURL(runtime.toURI().toURL());
            }
            for (Map.Entry<DiscoveredMod, Future<File>> entry : rewritten.entrySet()) {
                DiscoveredMod mod = entry.getKey();
                File file;
                try {
                    file = join(entry.getValue());
                } catch (RuntimeException e) {
                    CleanroomLog.get()
                        .error("Failed to rewrite {} for Scala, loading it as is", mod.file().getName(), e);
                    continue;
                }
                onRewritten.accept(mod.withRewrittenFile(file));
                remapped++;
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to add Scala to the classpath", e);
        }
        CleanroomLog.get()
            .info(
                "Provided Scala in {} ms. Scala 2.11: {}, Scala 3: {}, rewritten jars: {}",
                (System.nanoTime() - start) / 1_000_000L, legacy, modern, remapped
            );
    }

    /**
     * Rewritten jars carry no signatures, so mods expecting a fingerprint get the certificates of the original jar.
     */
    public static Certificate[] certificates(Class<?> clazz, File source) {
        Certificate[] certificates = clazz.getProtectionDomain().getCodeSource().getCertificates();
        DiscoveredMod mod = CleanroomModDiscoverer.instance().discoveredMod(source);
        File original = mod == null ? null : mod.scala().original();
        if (certificates != null || original == null) {
            return certificates;
        }
        try (JarFile jar = new JarFile(original, true)) {
            JarEntry entry = jar.getJarEntry(clazz.getName().replace('.', '/') + ".class");
            if (entry == null) {
                return null;
            }
            // Certificates are only known once the entry has been read through
            try (InputStream in = jar.getInputStream(entry)) {
                in.transferTo(OutputStream.nullOutputStream());
            }
            return entry.getCertificates();
        } catch (IOException | SecurityException e) {
            CleanroomLog.get().warn("Failed to read the certificates of {}", original.getName(), e);
            return null;
        }
    }

    private static File rewrite(Cache cache, File jar, boolean legacy) throws IOException {
        String hash = Files.asByteSource(jar).hash(Hashing.sha256()).toString();
        return cache.fromBytes(() -> ScalaRemapper.remap(jar, legacy))
            .version(REMAPPER_VERSION)
            .get((legacy ? "legacy/" : "shim/") + hash + "/" + jar.getName())
            .toFile();
    }

    private static <T> T join(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private record Artifact(String url, String sha256) {

        private CompletableFuture<File> fetch(Cache cache) {
            return cache.fromUrl(URI.create(url))
                .expectSha256(sha256)
                .getAsync(url.substring(url.lastIndexOf('/') + 1))
                .thenApply(Path::toFile);
        }

    }

}
