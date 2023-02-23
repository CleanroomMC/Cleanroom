package org.lwjglx.opengl;

public class ARBCopyBuffer {

    public static final int GL_COPY_READ_BUFFER = (int) 36662;
    public static final int GL_COPY_WRITE_BUFFER = (int) 36663;

    public static void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset,
            long size) {
        org.lwjgl.opengl.ARBCopyBuffer.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    }
}
