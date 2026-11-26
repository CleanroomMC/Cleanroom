package com.cleanroommc.cleanroom.splash.pre.renderer;

import com.cleanroommc.kirino.ui.font.McTtfFontData;
import com.cleanroommc.mcttf.extract.AssetSource;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.cleanroommc.cleanroom.splash.pre.renderer.PreSplashLogger.LOGGER;

/**
 * FreeType & GL agnostic async Minecraft font loader.
 *
 * <p>Note: Only used by {@link SdlSplashRenderer}.</p>
 */
final class AsyncFontLoader implements AutoCloseable {

    private static final class Assets implements AssetSource {

        private static final Pattern HASH = Pattern.compile("[0-9a-f]{40}");

        private final Path directory;
        private final @Nullable Map<String, Path> index;

        private Assets(Path directory, @Nullable String assetIndex) {
            this.directory = directory;
            if (assetIndex == null) {
                index = null;
                return;
            }

            index = new HashMap<>();
            Path file = directory.resolve("indexes").resolve(assetIndex + ".json");
            try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject objects = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("objects");
                if (objects != null) {
                    for (var entry : objects.entrySet()) {
                        String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
                        if (!HASH.matcher(hash).matches()) {
                            throw new IOException("Invalid asset hash: " + hash);
                        }

                        index.put(entry.getKey(), directory.resolve("objects").resolve(hash.substring(0, 2)).resolve(hash));
                    }
                }
            } catch (Throwable failure) {
                index.clear();
                LOGGER.warn("Could not read pre splash asset index {}.", file, failure);
            }
        }

        @Nullable
        private Path find(String path) {
            Preconditions.checkArgument(path.startsWith("assets/"), "Invalid asset path: %s", path);

            String name = path.substring("assets/".length());
            Path file = index == null ? directory.resolve(name) : index.get(name);
            return file != null && Files.isRegularFile(file) ? file : null;
        }

        @Override
        public boolean exists(String path) {
            return find(path) != null || SdlSplashRenderer.class.getResource("/" + path) != null;
        }

        @Override
        public InputStream open(String path) throws IOException {
            Path file = find(path);
            if (file != null) {
                return Files.newInputStream(file);
            }
            InputStream input = SdlSplashRenderer.class.getResourceAsStream("/" + path);
            if (input == null) {
                throw new FileNotFoundException(path);
            }
            return input;
        }

        @Override
        public void close() {
            // NO OP
        }
    }

    private static final int LOAD_TIMEOUT_SECOND = 30;
    private static final int THREAD_STOP_TIMEOUT_SECOND = 2;

    final Thread thread;
    private final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(LOAD_TIMEOUT_SECOND);
    private byte @Nullable [] data;
    private boolean finished;
    private boolean closed;

    AsyncFontLoader(File assetsDirectory, @Nullable String assetIndex) {
        thread = new Thread(() -> load(assetsDirectory, assetIndex), "Pre Splash Font Loader");
        thread.setDaemon(true);
    }

    synchronized boolean isFinished() {
        if (!finished && deadline - System.nanoTime() <= 0) {
            LOGGER.warn("Pre splash Minecraft font loading timed out after {} seconds.", LOAD_TIMEOUT_SECOND);
            cancel();
        }

        return finished;
    }

    @SuppressWarnings("DataFlowIssue")
    synchronized byte @Nullable [] take() {
        byte[] data = this.data;
        this.data = null;
        return data;
    }

    @SuppressWarnings("DataFlowIssue")
    private void load(File assetsDirectory, @Nullable String assetIndex) {
        StopWatch stopWatch = StopWatch.createStarted();
        byte[] data = null;

        try {
            synchronized (this) {
                if (closed || isFinished()) {
                    return;
                }
            }

            Assets source = new Assets(assetsDirectory.toPath(), assetIndex);

            synchronized (this) {
                if (closed || isFinished()) {
                    source.close();
                    return;
                }
            }

            data = McTtfFontData.loadDefault(source, "pre_splash");
        } catch (Throwable failure) {
            LOGGER.warn("Could not prepare the pre splash Minecraft font.", failure);
        } finally {
            stopWatch.stop();
            synchronized (this) {
                if (!closed && !isFinished()) {
                    this.data = data;
                    if (data != null) {
                        LOGGER.info("Pre splash Minecraft font loaded in {} ms.", stopWatch.getTime(TimeUnit.MILLISECONDS));
                    }
                }
                finished = true;
            }
        }
    }

    private void cancel() {
        synchronized (this) {
            closed = true;
            finished = true;
            data = null;
        }

        thread.interrupt();
    }

    @Override
    public void close() {
        cancel();
        if (Thread.currentThread() == thread) {
            return;
        }

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(THREAD_STOP_TIMEOUT_SECOND);
        boolean interrupted = false;
        while (thread.isAlive()) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                LOGGER.warn("Pre splash font loader did not stop within {} seconds.", THREAD_STOP_TIMEOUT_SECOND);
                break;
            }
            try {
                TimeUnit.NANOSECONDS.timedJoin(thread, remaining);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
