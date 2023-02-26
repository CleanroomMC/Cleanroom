package org.lwjglx.opengl;

public class NVShaderBufferLoad {

    public static final int GL_BUFFER_GPU_ADDRESS_NV = (int) 36637;
    public static final int GL_GPU_ADDRESS_NV = (int) 36660;
    public static final int GL_MAX_SHADER_BUFFER_ADDRESS_NV = (int) 36661;

    public static void glGetBufferParameteruNV(int target, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.NVShaderBufferLoad.glGetBufferParameterui64vNV(target, pname, params);
    }

    public static long glGetBufferParameterui64NV(int target, int pname) {
        return org.lwjgl.opengl.NVShaderBufferLoad.glGetBufferParameterui64NV(target, pname);
    }

    public static void glGetIntegeruNV(int value, java.nio.LongBuffer result) {
        org.lwjgl.opengl.NVShaderBufferLoad.glGetIntegerui64vNV(value, result);
    }

    public static long glGetIntegerui64NV(int value) {
        return org.lwjgl.opengl.NVShaderBufferLoad.glGetIntegerui64NV(value);
    }

    public static void glGetNamedBufferParameteruNV(int buffer, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.NVShaderBufferLoad.glGetNamedBufferParameterui64vNV(buffer, pname, params);
    }

    public static long glGetNamedBufferParameterui64NV(int buffer, int pname) {
        return org.lwjgl.opengl.NVShaderBufferLoad.glGetNamedBufferParameterui64NV(buffer, pname);
    }

    public static boolean glIsBufferResidentNV(int target) {
        return org.lwjgl.opengl.NVShaderBufferLoad.glIsBufferResidentNV(target);
    }

    public static boolean glIsNamedBufferResidentNV(int buffer) {
        return org.lwjgl.opengl.NVShaderBufferLoad.glIsNamedBufferResidentNV(buffer);
    }

    public static void glMakeBufferNonResidentNV(int target) {
        org.lwjgl.opengl.NVShaderBufferLoad.glMakeBufferNonResidentNV(target);
    }

    public static void glMakeBufferResidentNV(int target, int access) {
        org.lwjgl.opengl.NVShaderBufferLoad.glMakeBufferResidentNV(target, access);
    }

    public static void glMakeNamedBufferNonResidentNV(int buffer) {
        org.lwjgl.opengl.NVShaderBufferLoad.glMakeNamedBufferNonResidentNV(buffer);
    }

    public static void glMakeNamedBufferResidentNV(int buffer, int access) {
        org.lwjgl.opengl.NVShaderBufferLoad.glMakeNamedBufferResidentNV(buffer, access);
    }

    public static void glProgramUniformuNV(int program, int location, java.nio.LongBuffer value) {
        org.lwjgl.opengl.NVShaderBufferLoad.glProgramUniformui64vNV(program, location, value);
    }

    public static void glProgramUniformui64NV(int program, int location, long value) {
        org.lwjgl.opengl.NVShaderBufferLoad.glProgramUniformui64NV(program, location, value);
    }

    public static void glUniformuNV(int location, java.nio.LongBuffer value) {
        org.lwjgl.opengl.NVShaderBufferLoad.glUniformui64vNV(location, value);
    }

    public static void glUniformui64NV(int location, long value) {
        org.lwjgl.opengl.NVShaderBufferLoad.glUniformui64NV(location, value);
    }
}
