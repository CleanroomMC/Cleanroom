package org.lwjglx.opengl;

public class ARBBufferStorage {

    public static final int GL_BUFFER_IMMUTABLE_STORAGE = (int) 33311;
    public static final int GL_BUFFER_STORAGE_FLAGS = (int) 33312;
    public static final int GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT = (int) 16384;
    public static final int GL_CLIENT_STORAGE_BIT = (int) 512;
    public static final int GL_DYNAMIC_STORAGE_BIT = (int) 256;
    public static final int GL_MAP_COHERENT_BIT = (int) 128;
    public static final int GL_MAP_PERSISTENT_BIT = (int) 64;

    public static void glBufferStorage(int target, long size, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glBufferStorage(target, size, flags);
    }

    public static void glBufferStorage(int target, java.nio.ByteBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.DoubleBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.FloatBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.IntBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.ShortBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glBufferStorage(target, data, flags);
    }

    public static void glNamedBufferStorageEXT(int buffer, long size, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glNamedBufferStorageEXT(buffer, size, flags);
    }

    public static void glNamedBufferStorageEXT(int buffer, java.nio.ByteBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glNamedBufferStorageEXT(buffer, data, flags);
    }

    public static void glNamedBufferStorageEXT(int buffer, java.nio.DoubleBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glNamedBufferStorageEXT(buffer, data, flags);
    }

    public static void glNamedBufferStorageEXT(int buffer, java.nio.FloatBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glNamedBufferStorageEXT(buffer, data, flags);
    }

    public static void glNamedBufferStorageEXT(int buffer, java.nio.IntBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glNamedBufferStorageEXT(buffer, data, flags);
    }

    public static void glNamedBufferStorageEXT(int buffer, java.nio.ShortBuffer data, int flags) {
        org.lwjgl.opengl.ARBBufferStorage.glNamedBufferStorageEXT(buffer, data, flags);
    }
}
