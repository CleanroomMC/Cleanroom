package org.lwjglx.opengl;

public class ARBVertexArrayObject {

    public static final int GL_VERTEX_ARRAY_BINDING = (int) 34229;

    public static void glBindVertexArray(int array) {
        org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray(array);
    }

    public static void glDeleteVertexArrays(int array) {
        org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays(array);
    }

    public static void glDeleteVertexArrays(java.nio.IntBuffer arrays) {
        org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays(arrays);
    }

    public static int glGenVertexArrays() {
        return org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays();
    }

    public static void glGenVertexArrays(java.nio.IntBuffer arrays) {
        org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays(arrays);
    }

    public static boolean glIsVertexArray(int array) {
        return org.lwjgl.opengl.ARBVertexArrayObject.glIsVertexArray(array);
    }
}
