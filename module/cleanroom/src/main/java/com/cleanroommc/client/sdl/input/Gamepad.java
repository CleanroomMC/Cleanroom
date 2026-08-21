package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * One mapped gamepad. Query buttons, axes and rumble if the pad supports it.
 */
public final class Gamepad {

    static final float DEFAULT_DEADZONE = 0.2F;
    static final float AXIS_SCALE = 32767.0F;

    private final int instanceId;
    private final long handle;

    Gamepad(int instanceId, long handle) {
        this.instanceId = instanceId;
        this.handle = handle;
    }

    public int id() {
        return instanceId;
    }

    public String name() {
        String name = SDLGamepad.SDL_GetGamepadName(handle);
        return name == null ? "" : name;
    }

    public GamepadType type() {
        return GamepadType.of(SDLGamepad.SDL_GetGamepadType(handle));
    }

    public boolean connected() {
        return SDLGamepad.SDL_GamepadConnected(handle);
    }

    public boolean button(GamepadButton button) {
        return button != null && SDLGamepad.SDL_GetGamepadButton(handle, button.value());
    }

    /**
     * @return {@code [-1, 1]} for sticks, {@code [0, 1]} for triggers, after the default deadzone
     */
    public float axis(GamepadAxis axis) {
        return axis(axis, DEFAULT_DEADZONE);
    }

    public float axis(GamepadAxis axis, float deadzone) {
        if (axis == null) {
            return 0.0F;
        }
        float raw = SDLGamepad.SDL_GetGamepadAxis(handle, axis.value()) / AXIS_SCALE;
        if (axis.trigger()) {
            return raw < deadzone ? 0.0F : Math.min(1.0F, raw);
        }
        float magnitude = Math.abs(raw);
        if (magnitude < deadzone) {
            return 0.0F;
        }
        float scaled = (magnitude - deadzone) / (1.0F - deadzone);
        return Math.copySign(Math.min(1.0F, scaled), raw);
    }

    public GamepadLabel label(GamepadButton button) {
        if (button == null) {
            return GamepadLabel.UNKNOWN;
        }
        return GamepadLabel.of(SDLGamepad.SDL_GetGamepadButtonLabel(handle, button.value()));
    }

    public int player() {
        return SDLGamepad.SDL_GetGamepadPlayerIndex(handle);
    }

    public Gamepad player(int index) {
        SDL.check(SDLGamepad.SDL_SetGamepadPlayerIndex(handle, index), "SDL_SetGamepadPlayerIndex");
        return this;
    }

    /**
     * @param low low-frequency rumble, {@code 0..1}
     * @param high high-frequency rumble, {@code 0..1}
     * @param durationMs how long to rumble
     */
    public Gamepad rumble(float low, float high, int durationMs) {
        SDL.check(SDLGamepad.SDL_RumbleGamepad(handle, toStrength(low), toStrength(high), durationMs), "SDL_RumbleGamepad");
        return this;
    }

    public Gamepad rumbleTriggers(float left, float right, int durationMs) {
        SDL.check(SDLGamepad.SDL_RumbleGamepadTriggers(handle, toStrength(left), toStrength(right), durationMs),
                "SDL_RumbleGamepadTriggers");
        return this;
    }

    public Gamepad led(int red, int green, int blue) {
        SDL.check(SDLGamepad.SDL_SetGamepadLED(handle, (byte) red, (byte) green, (byte) blue), "SDL_SetGamepadLED");
        return this;
    }

    public boolean has(GamepadButton button) {
        return button != null && SDLGamepad.SDL_GamepadHasButton(handle, button.value());
    }

    public boolean has(GamepadAxis axis) {
        return axis != null && SDLGamepad.SDL_GamepadHasAxis(handle, axis.value());
    }

    public boolean has(SensorType sensor) {
        return sensor != null && SDLGamepad.SDL_GamepadHasSensor(handle, sensor.value());
    }

    public Gamepad sensor(SensorType sensor, boolean enabled) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor cannot be null");
        }
        SDL.check(SDLGamepad.SDL_SetGamepadSensorEnabled(handle, sensor.value(), enabled), "SDL_SetGamepadSensorEnabled");
        return this;
    }

    public boolean sensorEnabled(SensorType sensor) {
        return sensor != null && SDLGamepad.SDL_GamepadSensorEnabled(handle, sensor.value());
    }

    /**
     * @return three floats, or an empty array if this pad has no such sensor
     */
    public float[] sensor(SensorType sensor) {
        if (sensor == null || !has(sensor)) {
            return new float[0];
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            if (!SDLGamepad.SDL_GetGamepadSensorData(handle, sensor.value(), buffer)) {
                return new float[0];
            }
            return new float[] {buffer.get(0), buffer.get(1), buffer.get(2)};
        }
    }

    public int touchpads() {
        return Math.max(0, SDLGamepad.SDL_GetNumGamepadTouchpads(handle));
    }

    public int fingers(int touchpad) {
        return Math.max(0, SDLGamepad.SDL_GetNumGamepadTouchpadFingers(handle, touchpad));
    }

    /**
     * @return {@code null} if the finger is not down or the pad has no such touchpad
     */
    public Finger finger(int touchpad, int finger) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer down = stack.malloc(1);
            FloatBuffer x = stack.mallocFloat(1);
            FloatBuffer y = stack.mallocFloat(1);
            FloatBuffer pressure = stack.mallocFloat(1);
            if (!SDLGamepad.SDL_GetGamepadTouchpadFinger(handle, touchpad, finger, down, x, y, pressure)) {
                return null;
            }
            if (down.get(0) == 0) {
                return null;
            }
            return new Finger(finger, x.get(0), y.get(0), pressure.get(0));
        }
    }

    public String serial() {
        String serial = SDLGamepad.SDL_GetGamepadSerial(handle);
        return serial == null ? "" : serial;
    }

    public int firmwareVersion() {
        return Short.toUnsignedInt(SDLGamepad.SDL_GetGamepadFirmwareVersion(handle));
    }

    public String mapping() {
        String mapping = SDLGamepad.SDL_GetGamepadMapping(handle);
        return mapping == null ? "" : mapping;
    }

    long handle() {
        return handle;
    }

    static short toStrength(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return (short) Math.round(clamped * 65535.0F);
    }

}
