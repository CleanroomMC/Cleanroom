package org.lwjgl.opengl;

public class NVBindlessTexture {

    public static long glGetImageHandleNV(int texture, int level, boolean layered, int layer, int format) {
        return org.lwjgl3.opengl.NVBindlessTexture.glGetImageHandleNV(texture, level, layered, layer, format);
    }

    public static long glGetTextureHandleNV(int texture) {
        return org.lwjgl3.opengl.NVBindlessTexture.glGetTextureHandleNV(texture);
    }

    public static long glGetTextureSamplerHandleNV(int texture, int sampler) {
        return org.lwjgl3.opengl.NVBindlessTexture.glGetTextureSamplerHandleNV(texture, sampler);
    }

    public static boolean glIsImageHandleResidentNV(long handle) {
        return org.lwjgl3.opengl.NVBindlessTexture.glIsImageHandleResidentNV(handle);
    }

    public static boolean glIsTextureHandleResidentNV(long handle) {
        return org.lwjgl3.opengl.NVBindlessTexture.glIsTextureHandleResidentNV(handle);
    }

    public static void glMakeImageHandleNonResidentNV(long handle) {
        org.lwjgl3.opengl.NVBindlessTexture.glMakeImageHandleNonResidentNV(handle);
    }

    public static void glMakeImageHandleResidentNV(long handle, int access) {
        org.lwjgl3.opengl.NVBindlessTexture.glMakeImageHandleResidentNV(handle, access);
    }

    public static void glMakeTextureHandleNonResidentNV(long handle) {
        org.lwjgl3.opengl.NVBindlessTexture.glMakeTextureHandleNonResidentNV(handle);
    }

    public static void glMakeTextureHandleResidentNV(long handle) {
        org.lwjgl3.opengl.NVBindlessTexture.glMakeTextureHandleResidentNV(handle);
    }

    public static void glProgramUniformHandleuNV(int program, int location, java.nio.LongBuffer values) {
        org.lwjgl3.opengl.NVBindlessTexture.glProgramUniformHandleui64vNV(program, location, values);
    }

    public static void glProgramUniformHandleui64NV(int program, int location, long value) {
        org.lwjgl3.opengl.NVBindlessTexture.glProgramUniformHandleui64NV(program, location, value);
    }

    public static void glUniformHandleuNV(int location, java.nio.LongBuffer value) {
        org.lwjgl3.opengl.NVBindlessTexture.glUniformHandleui64vNV(location, value);
    }

    public static void glUniformHandleui64NV(int location, long value) {
        org.lwjgl3.opengl.NVBindlessTexture.glUniformHandleui64NV(location, value);
    }
}
