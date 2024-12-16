package org.lwjglx.openal;

public class AL10 {

    public static final int AL_INVALID = org.lwjgl.openal.AL10.AL_INVALID;
    public static final int AL_NONE = org.lwjgl.openal.AL10.AL_NONE;
    public static final int AL_FALSE = org.lwjgl.openal.AL10.AL_FALSE;
    public static final int AL_TRUE = org.lwjgl.openal.AL10.AL_TRUE;
    public static final int AL_SOURCE_TYPE = org.lwjgl.openal.AL10.AL_SOURCE_TYPE;
    public static final int AL_SOURCE_ABSOLUTE = org.lwjgl.openal.AL10.AL_SOURCE_ABSOLUTE;
    public static final int AL_SOURCE_RELATIVE = org.lwjgl.openal.AL10.AL_SOURCE_RELATIVE;
    public static final int AL_CONE_INNER_ANGLE = org.lwjgl.openal.AL10.AL_CONE_INNER_ANGLE;
    public static final int AL_CONE_OUTER_ANGLE = org.lwjgl.openal.AL10.AL_CONE_OUTER_ANGLE;
    public static final int AL_PITCH = org.lwjgl.openal.AL10.AL_PITCH;
    public static final int AL_POSITION = org.lwjgl.openal.AL10.AL_POSITION;
    public static final int AL_DIRECTION = org.lwjgl.openal.AL10.AL_DIRECTION;
    public static final int AL_VELOCITY = org.lwjgl.openal.AL10.AL_VELOCITY;
    public static final int AL_LOOPING = org.lwjgl.openal.AL10.AL_LOOPING;
    public static final int AL_BUFFER = org.lwjgl.openal.AL10.AL_BUFFER;
    public static final int AL_GAIN = org.lwjgl.openal.AL10.AL_GAIN;
    public static final int AL_MIN_GAIN = org.lwjgl.openal.AL10.AL_MIN_GAIN;
    public static final int AL_MAX_GAIN = org.lwjgl.openal.AL10.AL_MAX_GAIN;
    public static final int AL_ORIENTATION = org.lwjgl.openal.AL10.AL_ORIENTATION,
            AL_REFERENCE_DISTANCE = org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;
    public static final int AL_ROLLOFF_FACTOR = org.lwjgl.openal.AL10.AL_ROLLOFF_FACTOR;
    public static final int AL_CONE_OUTER_GAIN = org.lwjgl.openal.AL10.AL_CONE_OUTER_GAIN;
    public static final int AL_MAX_DISTANCE = org.lwjgl.openal.AL10.AL_MAX_DISTANCE;
    public static final int AL_CHANNEL_MASK = 0x3000;
    public static final int AL_SOURCE_STATE = org.lwjgl.openal.AL10.AL_SOURCE_STATE,
            AL_INITIAL = org.lwjgl.openal.AL10.AL_INITIAL,
            AL_PLAYING = org.lwjgl.openal.AL10.AL_PLAYING,
            AL_PAUSED = org.lwjgl.openal.AL10.AL_PAUSED,
            AL_STOPPED = org.lwjgl.openal.AL10.AL_STOPPED;
    public static final int AL_BUFFERS_QUEUED = org.lwjgl.openal.AL10.AL_BUFFERS_QUEUED,
            AL_BUFFERS_PROCESSED = org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED;
    public static final int AL_FORMAT_MONO8 = org.lwjgl.openal.AL10.AL_FORMAT_MONO8,
            AL_FORMAT_MONO16 = org.lwjgl.openal.AL10.AL_FORMAT_MONO16,
            AL_FORMAT_STEREO8 = org.lwjgl.openal.AL10.AL_FORMAT_STEREO8,
            AL_FORMAT_STEREO16 = org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
    public static final int AL_FORMAT_VORBIS_EXT = 0x10003;
    public static final int AL_FREQUENCY = org.lwjgl.openal.AL10.AL_FREQUENCY;
    public static final int AL_BITS = org.lwjgl.openal.AL10.AL_BITS;
    public static final int AL_CHANNELS = org.lwjgl.openal.AL10.AL_CHANNELS;
    public static final int AL_SIZE = org.lwjgl.openal.AL10.AL_SIZE;
    public static final int AL_DATA = 0x2005;
    public static final int AL_UNUSED = org.lwjgl.openal.AL10.AL_UNUSED,
            AL_PENDING = org.lwjgl.openal.AL10.AL_PENDING,
            AL_PROCESSED = org.lwjgl.openal.AL10.AL_PROCESSED;
    public static final int AL_NO_ERROR = 0x0;
    public static final int AL_INVALID_NAME = org.lwjgl.openal.AL10.AL_INVALID_NAME;
    public static final int AL_INVALID_ENUM = org.lwjgl.openal.AL10.AL_INVALID_ENUM;
    public static final int AL_INVALID_VALUE = org.lwjgl.openal.AL10.AL_INVALID_VALUE;
    public static final int AL_INVALID_OPERATION = org.lwjgl.openal.AL10.AL_INVALID_OPERATION;
    public static final int AL_OUT_OF_MEMORY = org.lwjgl.openal.AL10.AL_OUT_OF_MEMORY;
    public static final int AL_VENDOR = org.lwjgl.openal.AL10.AL_VENDOR;
    public static final int AL_VERSION = org.lwjgl.openal.AL10.AL_VERSION;
    public static final int AL_RENDERER = org.lwjgl.openal.AL10.AL_RENDERER;
    public static final int AL_EXTENSIONS = org.lwjgl.openal.AL10.AL_EXTENSIONS;
    public static final int AL_DOPPLER_FACTOR = org.lwjgl.openal.AL10.AL_DOPPLER_FACTOR;
    public static final int AL_DOPPLER_VELOCITY = org.lwjgl.openal.AL10.AL_DOPPLER_FACTOR;
    public static final int AL_DISTANCE_MODEL = org.lwjgl.openal.AL10.AL_DISTANCE_MODEL;
    public static final int AL_INVERSE_DISTANCE = org.lwjgl.openal.AL10.AL_INVERSE_DISTANCE,
            AL_INVERSE_DISTANCE_CLAMPED = org.lwjgl.openal.AL10.AL_INVERSE_DISTANCE_CLAMPED;

    public static void alBufferData(int buffer, int format, java.nio.ByteBuffer data, int freq) {
        org.lwjgl.openal.AL10.alBufferData(buffer, format, data, freq);
    }

    public static void alBufferData(int buffer, int format, java.nio.IntBuffer data, int freq) {
        org.lwjgl.openal.AL10.alBufferData(buffer, format, data, freq);
    }

    public static void alBufferData(int buffer, int format, java.nio.ShortBuffer data, int freq) {
        org.lwjgl.openal.AL10.alBufferData(buffer, format, data, freq);
    }
    public static void alcCloseDevice(ALCdevice alCdevice) {
        org.lwjgl.openal.ALC10.alcCloseDevice(alCdevice.device);
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
