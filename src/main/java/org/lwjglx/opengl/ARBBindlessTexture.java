package org.lwjglx.opengl;

public class ARBBindlessTexture {

    public static final int GL_UNSIGNED_INT64_ARB = (int) 5135;

    public static long glGetImageHandleARB(int texture, int level, boolean layered, int layer, int format) {
        return org.lwjgl.opengl.ARBBindlessTexture.glGetImageHandleARB(texture, level, layered, layer, format);
    }

    public static long glGetTextureHandleARB(int texture) {
        return org.lwjgl.opengl.ARBBindlessTexture.glGetTextureHandleARB(texture);
    }

    public static long glGetTextureSamplerHandleARB(int texture, int sampler) {
        return org.lwjgl.opengl.ARBBindlessTexture.glGetTextureSamplerHandleARB(texture, sampler);
    }

    public static void glGetVertexAttribLuARB(int index, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.ARBBindlessTexture.glGetVertexAttribLui64vARB(index, pname, params);
    }

    public static boolean glIsImageHandleResidentARB(long handle) {
        return org.lwjgl.opengl.ARBBindlessTexture.glIsImageHandleResidentARB(handle);
    }

    public static boolean glIsTextureHandleResidentARB(long handle) {
        return org.lwjgl.opengl.ARBBindlessTexture.glIsTextureHandleResidentARB(handle);
    }

    public static void glMakeImageHandleNonResidentARB(long handle) {
        org.lwjgl.opengl.ARBBindlessTexture.glMakeImageHandleNonResidentARB(handle);
    }

    public static void glMakeImageHandleResidentARB(long handle, int access) {
        org.lwjgl.opengl.ARBBindlessTexture.glMakeImageHandleResidentARB(handle, access);
    }

    public static void glMakeTextureHandleNonResidentARB(long handle) {
        org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleNonResidentARB(handle);
    }

    public static void glMakeTextureHandleResidentARB(long handle) {
        org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleResidentARB(handle);
    }

    public static void glProgramUniformHandleuARB(int program, int location, java.nio.LongBuffer values) {
        org.lwjgl.opengl.ARBBindlessTexture.glProgramUniformHandleui64vARB(program, location, values);
    }

    public static void glProgramUniformHandleui64ARB(int program, int location, long value) {
        org.lwjgl.opengl.ARBBindlessTexture.glProgramUniformHandleui64ARB(program, location, value);
    }

    public static void glUniformHandleuARB(int location, java.nio.LongBuffer value) {
        org.lwjgl.opengl.ARBBindlessTexture.glUniformHandleui64vARB(location, value);
    }

    public static void glUniformHandleui64ARB(int location, long value) {
        org.lwjgl.opengl.ARBBindlessTexture.glUniformHandleui64ARB(location, value);
    }

    public static void glVertexAttribL1uARB(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.ARBBindlessTexture.glVertexAttribL1ui64vARB(index, v);
    }

    public static void glVertexAttribL1ui64ARB(int index, long x) {
        org.lwjgl.opengl.ARBBindlessTexture.glVertexAttribL1ui64ARB(index, x);
    }
}
