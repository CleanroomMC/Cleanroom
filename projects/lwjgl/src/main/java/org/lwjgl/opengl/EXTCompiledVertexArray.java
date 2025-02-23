package org.lwjgl.opengl;

public class EXTCompiledVertexArray {

    public static final int GL_ARRAY_ELEMENT_LOCK_COUNT_EXT = (int) 33193;
    public static final int GL_ARRAY_ELEMENT_LOCK_FIRST_EXT = (int) 33192;

    public static void glLockArraysEXT(int first, int count) {
        org.lwjgl3.opengl.EXTCompiledVertexArray.glLockArraysEXT(first, count);
    }

    public static void glUnlockArraysEXT() {
        org.lwjgl3.opengl.EXTCompiledVertexArray.glUnlockArraysEXT();
    }
}
