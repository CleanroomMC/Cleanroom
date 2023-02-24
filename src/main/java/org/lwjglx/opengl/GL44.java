package org.lwjglx.opengl;

import org.lwjglx.lwjgl3ify.BufferCasts;

public class GL44 {

    public static final int GL_BUFFER_IMMUTABLE_STORAGE = (int) 33311;
    public static final int GL_BUFFER_STORAGE_FLAGS = (int) 33312;
    public static final int GL_CLEAR_TEXTURE = (int) 37733;
    public static final int GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT = (int) 16384;
    public static final int GL_CLIENT_STORAGE_BIT = (int) 512;
    public static final int GL_DYNAMIC_STORAGE_BIT = (int) 256;
    public static final int GL_LOCATION_COMPONENT = (int) 37706;
    public static final int GL_MAP_COHERENT_BIT = (int) 128;
    public static final int GL_MAP_PERSISTENT_BIT = (int) 64;
    public static final int GL_MAX_VERTEX_ATTRIB_STRIDE = (int) 33509;
    public static final int GL_MIRROR_CLAMP_TO_EDGE = (int) 34627;
    public static final int GL_QUERY_BUFFER = (int) 37266;
    public static final int GL_QUERY_BUFFER_BARRIER_BIT = (int) 32768;
    public static final int GL_QUERY_BUFFER_BINDING = (int) 37267;
    public static final int GL_QUERY_RESULT_NO_WAIT = (int) 37268;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_INDEX = (int) 37707;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_STRIDE = (int) 37708;

    public static void glBufferStorage(int target, long size, int flags) {
        org.lwjgl.opengl.GL44.glBufferStorage(target, size, flags);
    }

    public static void glBufferStorage(int target, java.nio.ByteBuffer data, int flags) {
        org.lwjgl.opengl.GL44.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.DoubleBuffer data, int flags) {
        org.lwjgl.opengl.GL44.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.FloatBuffer data, int flags) {
        org.lwjgl.opengl.GL44.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.IntBuffer data, int flags) {
        org.lwjgl.opengl.GL44.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(int target, java.nio.ShortBuffer data, int flags) {
        org.lwjgl.opengl.GL44.glBufferStorage(target, data, flags);
    }

    public static void glClearTexImage(int texture, int level, int format, int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(int texture, int level, int format, int type, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(int texture, int level, int format, int type, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(int texture, int level, int format, int type, java.nio.IntBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(int texture, int level, int format, int type, java.nio.LongBuffer data) {
        final java.nio.ByteBuffer wrappedArg4 = BufferCasts.toByteBuffer(data);

        org.lwjgl.opengl.GL44.glClearTexImage(texture, level, format, type, wrappedArg4);
        BufferCasts.updateBuffer(data, wrappedArg4);
    }

    public static void glClearTexImage(int texture, int level, int format, int type, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                data);
    }

    public static void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                data);
    }

    public static void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                data);
    }

    public static void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.IntBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                data);
    }

    public static void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.LongBuffer data) {
        final java.nio.ByteBuffer wrappedArg10 = BufferCasts.toByteBuffer(data);

        org.lwjgl.opengl.GL44.glClearTexSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                wrappedArg10);
        BufferCasts.updateBuffer(data, wrappedArg10);
    }

    public static void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.GL44.glClearTexSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                data);
    }
}
