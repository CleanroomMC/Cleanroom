/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre.renderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class PreSplashLogger {

    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    @FunctionalInterface
    public interface Sink {

        void log(Level level, String message, @Nullable Throwable failure);
    }

    private static final class Log4j {

        private static final Logger LOGGER = LogManager.getLogger("Pre Splash Renderer");

        private static void log(Level level, String message, @Nullable Throwable failure) {
            LOGGER.log(org.apache.logging.log4j.Level.valueOf(level.name()), message, failure);
        }
    }

    public static final PreSplashLogger LOGGER = new PreSplashLogger();

    private static volatile @Nullable Sink sink;

    private PreSplashLogger() {
    }

    public static void setSink(Sink sink) {
        PreSplashLogger.sink = Objects.requireNonNull(sink);
    }

    public void trace(String message, Object... arguments) {
        log(Level.TRACE, message, arguments);
    }

    public void debug(String message, Object... arguments) {
        log(Level.DEBUG, message, arguments);
    }

    public void info(String message, Object... arguments) {
        log(Level.INFO, message, arguments);
    }

    public void warn(String message, Object... arguments) {
        log(Level.WARN, message, arguments);
    }

    public void error(String message, Object... arguments) {
        log(Level.ERROR, message, arguments);
    }

    /**
     * Supports <code>{}</code> placeholders and an optional Throwable.
     */
    public void log(Level level, String message, Object... arguments) {
        Throwable failure = arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable cause ? cause : null;
        int count = arguments.length - (failure == null ? 0 : 1);

        if (count > 0) {
            StringBuilder formatted = new StringBuilder();
            int offset = 0;
            for (int i = 0; i < count; i++) {
                int placeholder = message.indexOf("{}", offset);
                if (placeholder < 0) {
                    break;
                }
                formatted.append(message, offset, placeholder).append(arguments[i]);
                offset = placeholder + 2;
            }
            message = formatted.append(message, offset, message.length()).toString();
        }

        Sink sink = PreSplashLogger.sink;
        if (sink == null) {
            Log4j.log(level, message, failure);
        } else {
            sink.log(level, message, failure);
        }
    }
}
