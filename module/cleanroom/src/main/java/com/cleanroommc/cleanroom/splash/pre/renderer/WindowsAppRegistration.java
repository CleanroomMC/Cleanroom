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

import com.google.common.base.Preconditions;
import org.lwjgl.sdl.SDL;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.windows.User32;

import java.util.UUID;

public final class WindowsAppRegistration implements AutoCloseable {

    private long unregister;

    private WindowsAppRegistration(long unregister) {
        this.unregister = unregister;
    }

    public static WindowsAppRegistration register() {
        // LWJGL 3.4.3's SDLBinding.kt adds SDL_ automatically, generating nonexistent SDL_SDL_* exports

        long register = APIUtil.apiGetFunctionAddress(SDL.getLibrary(), "SDL_RegisterApp");
        long unregister = APIUtil.apiGetFunctionAddress(SDL.getLibrary(), "SDL_UnregisterApp");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // pre splash and game otherwise register SDL_app for the same process HINSTANCE
            long name = MemoryUtil.memAddress(stack.UTF8("Cleanroom_PreSplash_" + UUID.randomUUID()));

            Preconditions.checkState(JNI.invokePPZ(name, User32.CS_BYTEALIGNCLIENT | User32.CS_OWNDC, 0L, register),
                    "SDL_RegisterApp: %s", SDLError.SDL_GetError());
        }

        return new WindowsAppRegistration(unregister);
    }

    @Override
    public void close() {
        if (unregister != 0L) {
            JNI.invokeV(unregister);
            unregister = 0L;
        }
    }
}
