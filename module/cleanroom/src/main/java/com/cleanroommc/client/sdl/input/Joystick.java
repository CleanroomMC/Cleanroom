package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.Power;
import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDLGUID;
import org.lwjgl.sdl.SDLJoystick;
import org.lwjgl.sdl.SDL_GUID;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * One unmapped joystick. Hats, axes, buttons and trackballs are numbered from zero.
 */
public final class Joystick {

    private final int instanceId;
    private final long handle;

    Joystick(int instanceId, long handle) {
        this.instanceId = instanceId;
        this.handle = handle;
    }

    public int id() {
        return instanceId;
    }

    public String name() {
        String name = SDLJoystick.SDL_GetJoystickName(handle);
        return name == null ? "" : name;
    }

    public String path() {
        String path = SDLJoystick.SDL_GetJoystickPath(handle);
        return path == null ? "" : path;
    }

    public String guid() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_GUID guid = SDL_GUID.malloc(stack);
            SDLJoystick.SDL_GetJoystickGUID(handle, guid);
            ByteBuffer text = stack.calloc(33);
            SDLGUID.SDL_GUIDToString(guid, text);
            String value = MemoryUtil.memUTF8Safe(MemoryUtil.memAddress(text));
            return value == null ? "" : value;
        }
    }

    public String serial() {
        String serial = SDLJoystick.SDL_GetJoystickSerial(handle);
        return serial == null ? "" : serial;
    }

    public JoystickType type() {
        return JoystickType.of(SDLJoystick.SDL_GetJoystickType(handle));
    }

    public boolean connected() {
        return SDLJoystick.SDL_JoystickConnected(handle);
    }

    public boolean virtual() {
        return SDLJoystick.SDL_IsJoystickVirtual(instanceId);
    }

    public int vendor() {
        return Short.toUnsignedInt(SDLJoystick.SDL_GetJoystickVendor(handle));
    }

    public int product() {
        return Short.toUnsignedInt(SDLJoystick.SDL_GetJoystickProduct(handle));
    }

    public int productVersion() {
        return Short.toUnsignedInt(SDLJoystick.SDL_GetJoystickProductVersion(handle));
    }

    public int firmwareVersion() {
        return Short.toUnsignedInt(SDLJoystick.SDL_GetJoystickFirmwareVersion(handle));
    }

    public int player() {
        return SDLJoystick.SDL_GetJoystickPlayerIndex(handle);
    }

    public Joystick player(int index) {
        SDL.check(SDLJoystick.SDL_SetJoystickPlayerIndex(handle, index), "SDL_SetJoystickPlayerIndex");
        return this;
    }

    public int axes() {
        return Math.max(0, SDLJoystick.SDL_GetNumJoystickAxes(handle));
    }

    public int buttons() {
        return Math.max(0, SDLJoystick.SDL_GetNumJoystickButtons(handle));
    }

    public int hats() {
        return Math.max(0, SDLJoystick.SDL_GetNumJoystickHats(handle));
    }

    public int balls() {
        return Math.max(0, SDLJoystick.SDL_GetNumJoystickBalls(handle));
    }

    /**
     * @return {@code [-1, 1]} after the default deadzone
     */
    public float axis(int index) {
        return axis(index, Gamepad.DEFAULT_DEADZONE);
    }

    public float axis(int index, float deadzone) {
        if (index < 0 || index >= axes()) {
            return 0.0F;
        }
        float raw = SDLJoystick.SDL_GetJoystickAxis(handle, index) / Gamepad.AXIS_SCALE;
        float magnitude = Math.abs(raw);
        if (magnitude < deadzone) {
            return 0.0F;
        }
        float scaled = (magnitude - deadzone) / (1.0F - deadzone);
        return Math.copySign(Math.min(1.0F, scaled), raw);
    }

    public boolean button(int index) {
        return index >= 0 && SDLJoystick.SDL_GetJoystickButton(handle, index);
    }

    public JoystickHat hat(int index) {
        if (index < 0 || index >= hats()) {
            return JoystickHat.CENTERED;
        }
        return JoystickHat.of(SDLJoystick.SDL_GetJoystickHat(handle, index) & 0xFF);
    }

    public int[] ball(int index) {
        if (index < 0 || index >= balls()) {
            return new int[] {0, 0};
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer dx = stack.mallocInt(1);
            IntBuffer dy = stack.mallocInt(1);
            if (!SDLJoystick.SDL_GetJoystickBall(handle, index, dx, dy)) {
                return new int[] {0, 0};
            }
            return new int[] {dx.get(0), dy.get(0)};
        }
    }

    public JoystickConnection connection() {
        return JoystickConnection.of(SDLJoystick.SDL_GetJoystickConnectionState(handle));
    }

    public Power power() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer percent = stack.mallocInt(1);
            int state = SDLJoystick.SDL_GetJoystickPowerInfo(handle, percent);
            return new Power(Power.State.of(state), -1, percent.get(0));
        }
    }

    public Joystick rumble(float low, float high, int durationMs) {
        SDL.check(SDLJoystick.SDL_RumbleJoystick(handle, Gamepad.toStrength(low), Gamepad.toStrength(high), durationMs),
                "SDL_RumbleJoystick");
        return this;
    }

    public Joystick rumbleTriggers(float left, float right, int durationMs) {
        SDL.check(SDLJoystick.SDL_RumbleJoystickTriggers(handle, Gamepad.toStrength(left), Gamepad.toStrength(right), durationMs),
                "SDL_RumbleJoystickTriggers");
        return this;
    }

    public Joystick led(int red, int green, int blue) {
        SDL.check(SDLJoystick.SDL_SetJoystickLED(handle, (byte) red, (byte) green, (byte) blue), "SDL_SetJoystickLED");
        return this;
    }

    /**
     * Opens this stick's haptic device, if it has one.
     */
    public Haptic haptic() {
        return InputInternal.haptics().open(this);
    }

    long handle() {
        return handle;
    }

}
