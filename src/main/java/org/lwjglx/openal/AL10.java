package org.lwjglx.openal;

public class AL10 {

    public static final int AL_NO_ERROR = org.lwjgl.openal.AL10.AL_NO_ERROR;
    public static final int AL_BUFFER = org.lwjgl.openal.AL10.AL_BUFFER;
    public static final int AL_SOURCE_STATE = org.lwjgl.openal.AL10.AL_SOURCE_STATE;
    public static final int AL_PLAYING = org.lwjgl.openal.AL10.AL_PLAYING;
    public static final int AL_LOOPING = org.lwjgl.openal.AL10.AL_LOOPING;
    public static final int AL_TRUE = org.lwjgl.openal.AL10.AL_TRUE;
    public static final int AL_REFERENCE_DISTANCE = org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;
    public static final int AL_ROLLOFF_FACTOR = org.lwjgl.openal.AL10.AL_ROLLOFF_FACTOR;
    public static final int AL_POSITION = org.lwjgl.openal.AL10.AL_POSITION;

    public static void alBufferData(int buffer, int format, java.nio.ByteBuffer data, int freq) {
        org.lwjgl.openal.AL10.alBufferData(buffer, format, data, freq);
    }

    public static void alBufferData(int buffer, int format, java.nio.IntBuffer data, int freq) {
        org.lwjgl.openal.AL10.alBufferData(buffer, format, data, freq);
    }

    public static void alBufferData(int buffer, int format, java.nio.ShortBuffer data, int freq) {
        org.lwjgl.openal.AL10.alBufferData(buffer, format, data, freq);
    }

    public static void alDeleteBuffers(int buffer) {
        org.lwjgl.openal.AL10.alDeleteBuffers(buffer);
    }

    public static void alDeleteBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.openal.AL10.alDeleteBuffers(buffers);
    }

    public static void alDeleteSources(int source) {
        org.lwjgl.openal.AL10.alDeleteSources(source);
    }

    public static void alDeleteSources(java.nio.IntBuffer sources) {
        org.lwjgl.openal.AL10.alDeleteSources(sources);
    }

    public static void alDisable(int capability) {
        org.lwjgl.openal.AL10.alDisable(capability);
    }

    public static void alDistanceModel(int value) {
        org.lwjgl.openal.AL10.alDistanceModel(value);
    }

    public static void alDopplerFactor(float value) {
        org.lwjgl.openal.AL10.alDopplerFactor(value);
    }

    public static void alDopplerVelocity(float value) {
        org.lwjgl.openal.AL10.alDopplerVelocity(value);
    }

    public static void alEnable(int capability) {
        org.lwjgl.openal.AL10.alEnable(capability);
    }

    public static int alGenBuffers() {
        return org.lwjgl.openal.AL10.alGenBuffers();
    }

    public static void alGenBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.openal.AL10.alGenBuffers(buffers);
    }

    public static int alGenSources() {
        return org.lwjgl.openal.AL10.alGenSources();
    }

    public static void alGenSources(java.nio.IntBuffer sources) {
        org.lwjgl.openal.AL10.alGenSources(sources);
    }

    public static boolean alGetBoolean(int pname) {
        return org.lwjgl.openal.AL10.alGetBoolean(pname);
    }

    public static float alGetBufferf(int buffer, int pname) {
        return org.lwjgl.openal.AL10.alGetBufferf(buffer, pname);
    }

    public static int alGetBufferi(int buffer, int pname) {
        return org.lwjgl.openal.AL10.alGetBufferi(buffer, pname);
    }

    public static double alGetDouble(int pname) {
        return org.lwjgl.openal.AL10.alGetDouble(pname);
    }

    public static void alGetDouble(int pname, java.nio.DoubleBuffer data) {
        org.lwjgl.openal.AL10.alGetDoublev(pname, data);
    }

    public static int alGetEnumValue(java.lang.String ename) {

        int returnValue = org.lwjgl.openal.AL10.alGetEnumValue(ename);

        return returnValue;
    }

    public static int alGetError() {
        return org.lwjgl.openal.AL10.alGetError();
    }

    public static float alGetFloat(int pname) {
        return org.lwjgl.openal.AL10.alGetFloat(pname);
    }

    public static void alGetFloat(int pname, java.nio.FloatBuffer data) {
        org.lwjgl.openal.AL10.alGetFloatv(pname, data);
    }

    public static int alGetInteger(int pname) {
        return org.lwjgl.openal.AL10.alGetInteger(pname);
    }

    public static void alGetInteger(int pname, java.nio.IntBuffer data) {
        org.lwjgl.openal.AL10.alGetIntegerv(pname, data);
    }

    public static void alGetListener(int pname, java.nio.FloatBuffer floatdata) {
        org.lwjgl.openal.AL10.alGetListenerfv(pname, floatdata);
    }

    public static float alGetListenerf(int pname) {
        return org.lwjgl.openal.AL10.alGetListenerf(pname);
    }

    public static int alGetListeneri(int pname) {
        return org.lwjgl.openal.AL10.alGetListeneri(pname);
    }

    public static void alGetSource(int source, int pname, java.nio.FloatBuffer floatdata) {
        org.lwjgl.openal.AL10.alGetSourcefv(source, pname, floatdata);
    }

    public static float alGetSourcef(int source, int pname) {
        return org.lwjgl.openal.AL10.alGetSourcef(source, pname);
    }

    public static int alGetSourcei(int source, int pname) {
        return org.lwjgl.openal.AL10.alGetSourcei(source, pname);
    }

    public static java.lang.String alGetString(int pname) {
        return org.lwjgl.openal.AL10.alGetString(pname);
    }

    public static boolean alIsBuffer(int buffer) {
        return org.lwjgl.openal.AL10.alIsBuffer(buffer);
    }

    public static boolean alIsEnabled(int capability) {
        return org.lwjgl.openal.AL10.alIsEnabled(capability);
    }

    public static boolean alIsExtensionPresent(java.lang.String fname) {

        boolean returnValue = org.lwjgl.openal.AL10.alIsExtensionPresent(fname);

        return returnValue;
    }

    public static boolean alIsSource(int id) {
        return org.lwjgl.openal.AL10.alIsSource(id);
    }

    public static void alListener3f(int pname, float v1, float v2, float v3) {
        org.lwjgl.openal.AL10.alListener3f(pname, v1, v2, v3);
    }

    public static void alListener(int pname, java.nio.FloatBuffer value) {
        org.lwjgl.openal.AL10.alListenerfv(pname, value);
    }

    public static void alListenerf(int pname, float value) {
        org.lwjgl.openal.AL10.alListenerf(pname, value);
    }

    public static void alListeneri(int pname, int value) {
        org.lwjgl.openal.AL10.alListeneri(pname, value);
    }

    public static void alSource3f(int source, int pname, float v1, float v2, float v3) {
        org.lwjgl.openal.AL10.alSource3f(source, pname, v1, v2, v3);
    }

    public static void alSource(int source, int pname, java.nio.FloatBuffer value) {
        org.lwjgl.openal.AL10.alSourcefv(source, pname, value);
    }

    public static void alSourcePause(int source) {
        org.lwjgl.openal.AL10.alSourcePause(source);
    }

    public static void alSourcePause(java.nio.IntBuffer sources) {
        org.lwjgl.openal.AL10.alSourcePausev(sources);
    }

    public static void alSourcePlay(int source) {
        org.lwjgl.openal.AL10.alSourcePlay(source);
    }

    public static void alSourcePlay(java.nio.IntBuffer sources) {
        org.lwjgl.openal.AL10.alSourcePlayv(sources);
    }

    public static void alSourceQueueBuffers(int source, int buffer) {
        org.lwjgl.openal.AL10.alSourceQueueBuffers(source, buffer);
    }

    public static void alSourceQueueBuffers(int source, java.nio.IntBuffer buffers) {
        org.lwjgl.openal.AL10.alSourceQueueBuffers(source, buffers);
    }

    public static void alSourceRewind(int source) {
        org.lwjgl.openal.AL10.alSourceRewind(source);
    }

    public static void alSourceRewind(java.nio.IntBuffer sources) {
        org.lwjgl.openal.AL10.alSourceRewindv(sources);
    }

    public static void alSourceStop(int source) {
        org.lwjgl.openal.AL10.alSourceStop(source);
    }

    public static void alSourceStop(java.nio.IntBuffer sources) {
        org.lwjgl.openal.AL10.alSourceStopv(sources);
    }

    public static int alSourceUnqueueBuffers(int source) {
        return org.lwjgl.openal.AL10.alSourceUnqueueBuffers(source);
    }

    public static void alSourceUnqueueBuffers(int source, java.nio.IntBuffer buffers) {
        org.lwjgl.openal.AL10.alSourceUnqueueBuffers(source, buffers);
    }

    public static void alSourcef(int source, int pname, float value) {
        org.lwjgl.openal.AL10.alSourcef(source, pname, value);
    }

    public static void alSourcei(int source, int pname, int value) {
        org.lwjgl.openal.AL10.alSourcei(source, pname, value);
    }
}
