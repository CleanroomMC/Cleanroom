package org.lwjglx.openal;

import net.minecraftforge.common.ForgeEarlyConfig;

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
        create(null, 44100, 60, false);
    }

    public static void create(String deviceArguments, int contextFrequency, int contextRefresh,
                              boolean contextSynchronized) throws LWJGLException {
        create(deviceArguments, contextFrequency, contextRefresh, contextSynchronized, true);
    }

    public static void create(String deviceArguments, int contextFrequency, int contextRefresh,
                              boolean contextSynchronized, boolean openDevice) throws LWJGLException {
        IntBuffer attribs = BufferUtils.createIntBuffer(16);

        attribs.put(org.lwjgl.openal.ALC10.ALC_FREQUENCY);
        attribs.put(contextFrequency);

        attribs.put(org.lwjgl.openal.ALC10.ALC_REFRESH);
        attribs.put(contextRefresh);

        attribs.put(org.lwjgl.openal.ALC10.ALC_SYNC);
        attribs.put(contextSynchronized ? org.lwjgl.openal.ALC10.ALC_TRUE : org.lwjgl.openal.ALC10.ALC_FALSE);

        /////////////////////////////////////////////
        // HRTF
        if (!ForgeEarlyConfig.OPENAL_CONTEXT.ENABLE_HRTF) {
            attribs.put(org.lwjgl.openal.SOFTHRTF.ALC_HRTF_SOFT);
            attribs.put(org.lwjgl.openal.ALC10.ALC_FALSE);
            attribs.put(org.lwjgl.openal.SOFTHRTF.ALC_HRTF_ID_SOFT);
            attribs.put(0);
        }
        /////////////////////////////////////////////
        
        attribs.put(org.lwjgl.openal.EXTEfx.ALC_MAX_AUXILIARY_SENDS);
        attribs.put(4);

        attribs.put(0);
        attribs.flip();

        String defaultDevice = org.lwjgl.openal.ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);

        long deviceHandle = org.lwjgl.openal.ALC10.alcOpenDevice(defaultDevice);

        if (deviceHandle == 0) throw new LWJGLException("Could not open ALC device");

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
        if (alcContext != null) {
            org.lwjgl.openal.ALC10.alcDestroyContext(alcContext.context);
            alcContext = null;
        }
        if (alcDevice != null) {
            org.lwjgl.openal.ALC10.alcCloseDevice(alcDevice.device);
            alcDevice = null;
        }
        created = false;
    }

    public static ALCcontext getContext() {
        return alcContext;
    }

    public static ALCdevice getDevice() {
        return alcDevice;
    }
}
