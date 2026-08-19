package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLHaptic;

/** Bits from {@code SDL_GetHapticFeatures}. */
public enum HapticFeature {

    CONSTANT(SDLHaptic.SDL_HAPTIC_CONSTANT),
    SINE(SDLHaptic.SDL_HAPTIC_SINE),
    SQUARE(SDLHaptic.SDL_HAPTIC_SQUARE),
    TRIANGLE(SDLHaptic.SDL_HAPTIC_TRIANGLE),
    SAWTOOTH_UP(SDLHaptic.SDL_HAPTIC_SAWTOOTHUP),
    SAWTOOTH_DOWN(SDLHaptic.SDL_HAPTIC_SAWTOOTHDOWN),
    RAMP(SDLHaptic.SDL_HAPTIC_RAMP),
    SPRING(SDLHaptic.SDL_HAPTIC_SPRING),
    DAMPER(SDLHaptic.SDL_HAPTIC_DAMPER),
    INERTIA(SDLHaptic.SDL_HAPTIC_INERTIA),
    FRICTION(SDLHaptic.SDL_HAPTIC_FRICTION),
    LEFTRIGHT(SDLHaptic.SDL_HAPTIC_LEFTRIGHT),
    CUSTOM(SDLHaptic.SDL_HAPTIC_CUSTOM),
    GAIN(SDLHaptic.SDL_HAPTIC_GAIN),
    AUTOCENTER(SDLHaptic.SDL_HAPTIC_AUTOCENTER),
    STATUS(SDLHaptic.SDL_HAPTIC_STATUS),
    PAUSE(SDLHaptic.SDL_HAPTIC_PAUSE);

    public static HapticFeature of(int value) {
        return switch (value) {
            case SDLHaptic.SDL_HAPTIC_CONSTANT -> CONSTANT;
            case SDLHaptic.SDL_HAPTIC_SINE -> SINE;
            case SDLHaptic.SDL_HAPTIC_SQUARE -> SQUARE;
            case SDLHaptic.SDL_HAPTIC_TRIANGLE -> TRIANGLE;
            case SDLHaptic.SDL_HAPTIC_SAWTOOTHUP -> SAWTOOTH_UP;
            case SDLHaptic.SDL_HAPTIC_SAWTOOTHDOWN -> SAWTOOTH_DOWN;
            case SDLHaptic.SDL_HAPTIC_RAMP -> RAMP;
            case SDLHaptic.SDL_HAPTIC_SPRING -> SPRING;
            case SDLHaptic.SDL_HAPTIC_DAMPER -> DAMPER;
            case SDLHaptic.SDL_HAPTIC_INERTIA -> INERTIA;
            case SDLHaptic.SDL_HAPTIC_FRICTION -> FRICTION;
            case SDLHaptic.SDL_HAPTIC_LEFTRIGHT -> LEFTRIGHT;
            case SDLHaptic.SDL_HAPTIC_CUSTOM -> CUSTOM;
            case SDLHaptic.SDL_HAPTIC_GAIN -> GAIN;
            case SDLHaptic.SDL_HAPTIC_AUTOCENTER -> AUTOCENTER;
            case SDLHaptic.SDL_HAPTIC_STATUS -> STATUS;
            case SDLHaptic.SDL_HAPTIC_PAUSE -> PAUSE;
            default -> null;
        };
    }

    private final int value;

    HapticFeature(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
