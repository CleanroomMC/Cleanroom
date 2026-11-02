/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.hid;

import com.cleanroommc.client.sdl.SDLException;

import org.lwjgl.sdl.SDLHIDAPI;
import org.lwjgl.sdl.SDL_hid_device_info;

import java.nio.ByteBuffer;

/** An open HID handle. */
public final class HidDevice implements AutoCloseable {

    private final long handle;

    private boolean closed;

    HidDevice(long handle) {
        this.handle = handle;
    }

    public HidInfo info() {
        ensureOpen();
        SDL_hid_device_info info = SDLHIDAPI.SDL_hid_get_device_info(handle);
        if (info == null) {
            return null;
        }
        return Hid.fromNative(info);
    }

    public int write(ByteBuffer data) {
        ensureOpen();
        int written = SDLHIDAPI.SDL_hid_write(handle, data);
        if (written < 0) {
            throw new SDLException("SDL_hid_write failed");
        }
        return written;
    }

    public int read(ByteBuffer dest) {
        ensureOpen();
        int read = SDLHIDAPI.SDL_hid_read(handle, dest);
        if (read < 0) {
            throw new SDLException("SDL_hid_read failed");
        }
        return read;
    }

    public int read(ByteBuffer dest, int timeoutMs) {
        ensureOpen();
        int read = SDLHIDAPI.SDL_hid_read_timeout(handle, dest, timeoutMs);
        if (read < 0) {
            throw new SDLException("SDL_hid_read_timeout failed");
        }
        return read;
    }

    public int feature(ByteBuffer dest) {
        ensureOpen();
        int read = SDLHIDAPI.SDL_hid_get_feature_report(handle, dest);
        if (read < 0) {
            throw new SDLException("SDL_hid_get_feature_report failed");
        }
        return read;
    }

    public int sendFeature(ByteBuffer data) {
        ensureOpen();
        int written = SDLHIDAPI.SDL_hid_send_feature_report(handle, data);
        if (written < 0) {
            throw new SDLException("SDL_hid_send_feature_report failed");
        }
        return written;
    }

    public HidDevice nonblocking(boolean enabled) {
        ensureOpen();
        if (SDLHIDAPI.SDL_hid_set_nonblocking(handle, enabled) < 0) {
            throw new SDLException("SDL_hid_set_nonblocking failed");
        }
        return this;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        Hid.forget(this);
        SDLHIDAPI.SDL_hid_close(handle);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The HID device is closed");
        }
    }

}
