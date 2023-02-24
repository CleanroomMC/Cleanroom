package org.lwjglx.openal;

import java.nio.IntBuffer;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjglx.BufferUtils;
import org.lwjglx.LWJGLException;
import org.lwjglx.Sys;

public class AL {

    static ALCdevice alcDevice;
    static ALCcontext alcContext;

    private static boolean created = false;

    static {
        Sys.initialize(); // init using dummy sys method
    }

    public static void create() throws LWJGLException {
        IntBuffer attribs = BufferUtils.createIntBuffer(16);

        attribs.put(org.lwjgl.openal.ALC10.ALC_FREQUENCY);
        attribs.put(44100);

        attribs.put(org.lwjgl.openal.ALC10.ALC_REFRESH);
        attribs.put(60);

        attribs.put(org.lwjgl.openal.ALC10.ALC_SYNC);
        attribs.put(org.lwjgl.openal.ALC10.ALC_FALSE);

        attribs.put(0);
        attribs.flip();

        String defaultDevice = org.lwjgl.openal.ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);

        long deviceHandle = org.lwjgl.openal.ALC10.alcOpenDevice(defaultDevice);

        alcDevice = new ALCdevice(deviceHandle);

        final ALCCapabilities deviceCaps = org.lwjgl.openal.ALC.createCapabilities(deviceHandle);

        long contextHandle = org.lwjgl.openal.ALC10.alcCreateContext(AL.getDevice().device, attribs);
        alcContext = new ALCcontext(contextHandle);
        org.lwjgl.openal.ALC10.alcMakeContextCurrent(contextHandle);
        org.lwjgl.openal.AL.createCapabilities(deviceCaps);

        created = true;
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        org.lwjgl.openal.ALC10.alcDestroyContext(alcContext.context);
        org.lwjgl.openal.ALC10.alcCloseDevice(alcDevice.device);
        alcContext = null;
        alcDevice = null;
        created = false;
    }

    public static ALCdevice getDevice() {
        return alcDevice;
    }
}
