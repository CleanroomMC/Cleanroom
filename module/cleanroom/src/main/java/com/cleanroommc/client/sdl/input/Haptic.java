package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.SDLException;
import org.lwjgl.sdl.SDLHaptic;
import org.lwjgl.sdl.SDL_HapticEffect;
import org.lwjgl.system.MemoryStack;

/**
 * One force-feedback device. {@link Gamepad#rumble} is the easy path.
 * This is the rest of the effect set.
 */
public final class Haptic implements AutoCloseable {

    static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private final int id;
    private final long handle;

    private int leftRightEffect = -1;
    private boolean rumbleReady;
    private boolean closed;

    Haptic(int id, long handle) {
        this.id = id;
        this.handle = handle;
    }

    public int id() {
        return id;
    }

    public String name() {
        String name = SDLHaptic.SDL_GetHapticName(handle);
        return name == null ? "" : name;
    }

    public int axes() {
        return Math.max(0, SDLHaptic.SDL_GetNumHapticAxes(handle));
    }

    public int maxEffects() {
        return Math.max(0, SDLHaptic.SDL_GetMaxHapticEffects(handle));
    }

    public boolean has(HapticFeature feature) {
        return feature != null && (SDLHaptic.SDL_GetHapticFeatures(handle) & feature.value()) != 0;
    }

    public boolean rumbleSupported() {
        return SDLHaptic.SDL_HapticRumbleSupported(handle);
    }

    /**
     * Simple rumble. Strength is {@code 0..1}.
     */
    public Haptic rumble(float strength, int durationMs) {
        ensureOpen();
        if (!rumbleReady) {
            SDL.check(SDLHaptic.SDL_InitHapticRumble(handle), "SDL_InitHapticRumble");
            rumbleReady = true;
        }
        SDL.check(SDLHaptic.SDL_PlayHapticRumble(handle, clamp01(strength), durationMs), "SDL_PlayHapticRumble");
        return this;
    }

    public Haptic stopRumble() {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_StopHapticRumble(handle), "SDL_StopHapticRumble");
        return this;
    }

    /**
     * Dual-motor left/right effect. Strengths are {@code 0..1}.
     *
     * <p>The effect is uploaded once and updated in place afterwards.
     * since a device holds only {@link #maxEffects()} of them at a time.
     *
     * @return the SDL effect id, which stays the same across calls until {@link #destroy(int)}
     */
    public int leftRight(float large, float small, int durationMs) {
        ensureOpen();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_HapticEffect effect = SDL_HapticEffect.calloc(stack);
            effect.type((short) SDLHaptic.SDL_HAPTIC_LEFTRIGHT);
            effect.leftright()
                    .type((short) SDLHaptic.SDL_HAPTIC_LEFTRIGHT)
                    .length(durationMs)
                    .large_magnitude(Gamepad.toStrength(large))
                    .small_magnitude(Gamepad.toStrength(small));
            if (this.leftRightEffect < 0) {
                int id = SDLHaptic.SDL_CreateHapticEffect(handle, effect);
                if (id < 0) {
                    throw new SDLException("SDL_CreateHapticEffect failed: " + org.lwjgl.sdl.SDLError.SDL_GetError());
                }
                this.leftRightEffect = id;
            } else {
                SDL.check(SDLHaptic.SDL_UpdateHapticEffect(handle, this.leftRightEffect, effect), "SDL_UpdateHapticEffect");
            }
            SDL.check(SDLHaptic.SDL_RunHapticEffect(handle, this.leftRightEffect, 1), "SDL_RunHapticEffect");
            return this.leftRightEffect;
        }
    }

    public Haptic stop(int effectId) {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_StopHapticEffect(handle, effectId), "SDL_StopHapticEffect");
        return this;
    }

    public Haptic destroy(int effectId) {
        ensureOpen();
        SDLHaptic.SDL_DestroyHapticEffect(handle, effectId);
        if (this.leftRightEffect == effectId) {
            this.leftRightEffect = -1;
        }
        return this;
    }

    public Haptic gain(int percent) {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_SetHapticGain(handle, clampPercent(percent)), "SDL_SetHapticGain");
        return this;
    }

    public Haptic autocenter(int percent) {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_SetHapticAutocenter(handle, clampPercent(percent)), "SDL_SetHapticAutocenter");
        return this;
    }

    public Haptic pause() {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_PauseHaptic(handle), "SDL_PauseHaptic");
        return this;
    }

    public Haptic resume() {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_ResumeHaptic(handle), "SDL_ResumeHaptic");
        return this;
    }

    public Haptic stop() {
        ensureOpen();
        SDL.check(SDLHaptic.SDL_StopHapticEffects(handle), "SDL_StopHapticEffects");
        return this;
    }

    long handle() {
        return handle;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The haptic device is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        this.leftRightEffect = -1;
        Inputs.haptics().forget(this);
        SDLHaptic.SDL_CloseHaptic(handle);
    }

}
