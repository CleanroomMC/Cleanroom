package org.lwjglx.opengl;

public class NVBindlessTexture {

    public static long glGetImageHandleNV(int texture, int level, boolean layered, int layer, int format) {
        return org.lwjgl.opengl.NVBindlessTexture.glGetImageHandleNV(texture, level, layered, layer, format);
    }

    public static long glGetTextureHandleNV(int texture) {
        return org.lwjgl.opengl.NVBindlessTexture.glGetTextureHandleNV(texture);
    }

    public static long glGetTextureSamplerHandleNV(int texture, int sampler) {
        return org.lwjgl.opengl.NVBindlessTexture.glGetTextureSamplerHandleNV(texture, sampler);
    }

    public static boolean glIsImageHandleResidentNV(long handle) {
        return org.lwjgl.opengl.NVBindlessTexture.glIsImageHandleResidentNV(handle);
    }

    public static boolean glIsTextureHandleResidentNV(long handle) {
        return org.lwjgl.opengl.NVBindlessTexture.glIsTextureHandleResidentNV(handle);
    }

    public static void glMakeImageHandleNonResidentNV(long handle) {
        org.lwjgl.opengl.NVBindlessTexture.glMakeImageHandleNonResidentNV(handle);
    }

    public static void glMakeImageHandleResidentNV(long handle, int access) {
        org.lwjgl.opengl.NVBindlessTexture.glMakeImageHandleResidentNV(handle, access);
    }

    public static void glMakeTextureHandleNonResidentNV(long handle) {
        org.lwjgl.opengl.NVBindlessTexture.glMakeTextureHandleNonResidentNV(handle);
    }

    public static void glMakeTextureHandleResidentNV(long handle) {
        org.lwjgl.opengl.NVBindlessTexture.glMakeTextureHandleResidentNV(handle);
    }

    public static void glProgramUniformHandleuNV(int program, int location, java.nio.LongBuffer values) {
        org.lwjgl.opengl.NVBindlessTexture.glProgramUniformHandleui64vNV(program, location, values);
    }

    public static void glProgramUniformHandleui64NV(int program, int location, long value) {
        org.lwjgl.opengl.NVBindlessTexture.glProgramUniformHandleui64NV(program, location, value);
    }

    public static void glUniformHandleuNV(int location, java.nio.LongBuffer value) {
        org.lwjgl.opengl.NVBindlessTexture.glUniformHandleui64vNV(location, value);
    }

    public static void glUniformHandleui64NV(int location, long value) {
        org.lwjgl.opengl.NVBindlessTexture.glUniformHandleui64NV(location, value);
    }
}
