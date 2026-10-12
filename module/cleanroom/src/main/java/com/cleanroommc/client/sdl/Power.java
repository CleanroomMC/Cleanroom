/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLPower;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * Battery/power snapshot from {@code SDL_GetPowerInfo}.
 *
 * @param state how the machine is powered
 * @param seconds estimated seconds remaining, or {@code -1}
 * @param percent estimated charge, {@code 0..100}, or {@code -1}
 */
public record Power(State state, int seconds, int percent) {

    static Power query() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer seconds = stack.mallocInt(1);
            IntBuffer percent = stack.mallocInt(1);
            int state = SDLPower.SDL_GetPowerInfo(seconds, percent);
            return new Power(State.of(state), seconds.get(0), percent.get(0));
        }
    }

    public enum State {

        ERROR(SDLPower.SDL_POWERSTATE_ERROR),
        UNKNOWN(SDLPower.SDL_POWERSTATE_UNKNOWN),
        ON_BATTERY(SDLPower.SDL_POWERSTATE_ON_BATTERY),
        NO_BATTERY(SDLPower.SDL_POWERSTATE_NO_BATTERY),
        CHARGING(SDLPower.SDL_POWERSTATE_CHARGING),
        CHARGED(SDLPower.SDL_POWERSTATE_CHARGED);

        private final int value;

        State(int value) {
            this.value = value;
        }

        public static State of(int value) {
            return switch (value) {
                case SDLPower.SDL_POWERSTATE_ERROR -> ERROR;
                case SDLPower.SDL_POWERSTATE_ON_BATTERY -> ON_BATTERY;
                case SDLPower.SDL_POWERSTATE_NO_BATTERY -> NO_BATTERY;
                case SDLPower.SDL_POWERSTATE_CHARGING -> CHARGING;
                case SDLPower.SDL_POWERSTATE_CHARGED -> CHARGED;
                default -> UNKNOWN;
            };
        }

        public int value() {
            return value;
        }

    }

}
