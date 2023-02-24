package org.lwjglx.opengl;

public class EXTDrawBuffers2 {

    public static void glColorMaskIndexedEXT(int buf, boolean r, boolean g, boolean b, boolean a) {
        org.lwjgl.opengl.EXTDrawBuffers2.glColorMaskIndexedEXT(buf, r, g, b, a);
    }

    public static void glDisableIndexedEXT(int target, int index) {
        org.lwjgl.opengl.EXTDrawBuffers2.glDisableIndexedEXT(target, index);
    }

    public static void glEnableIndexedEXT(int target, int index) {
        org.lwjgl.opengl.EXTDrawBuffers2.glEnableIndexedEXT(target, index);
    }

    public static boolean glGetBooleanIndexedEXT(int value, int index) {
        return org.lwjgl.opengl.EXTDrawBuffers2.glGetBooleanIndexedEXT(value, index);
    }

    public static void glGetBooleanIndexedEXT(int value, int index, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDrawBuffers2.glGetBooleanIndexedvEXT(value, index, data);
    }

    public static int glGetIntegerIndexedEXT(int value, int index) {
        return org.lwjgl.opengl.EXTDrawBuffers2.glGetIntegerIndexedEXT(value, index);
    }

    public static void glGetIntegerIndexedEXT(int value, int index, java.nio.IntBuffer data) {
        org.lwjgl.opengl.EXTDrawBuffers2.glGetIntegerIndexedvEXT(value, index, data);
    }

    public static boolean glIsEnabledIndexedEXT(int target, int index) {
        return org.lwjgl.opengl.EXTDrawBuffers2.glIsEnabledIndexedEXT(target, index);
    }
}
