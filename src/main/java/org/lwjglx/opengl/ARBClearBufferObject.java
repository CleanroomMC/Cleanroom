package org.lwjglx.opengl;

public class ARBClearBufferObject {

    public static void glClearBufferData(int target, int internalformat, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBClearBufferObject.glClearBufferData(target, internalformat, format, type, data);
    }

    public static void glClearBufferSubData(int target, int internalformat, long offset, long size, int format,
            int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBClearBufferObject
                .glClearBufferSubData(target, internalformat, offset, size, format, type, data);
    }

    public static void glClearNamedBufferDataEXT(int buffer, int internalformat, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBClearBufferObject.glClearNamedBufferDataEXT(buffer, internalformat, format, type, data);
    }

    public static void glClearNamedBufferSubDataEXT(int buffer, int internalformat, long offset, long size, int format,
            int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBClearBufferObject
                .glClearNamedBufferSubDataEXT(buffer, internalformat, offset, size, format, type, data);
    }
}
