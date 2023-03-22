package org.lwjgl.opengl;

import java.nio.ByteBuffer;

import org.lwjgl3.BufferUtils;
import org.lwjgl3.PointerBuffer;
import org.lwjgl3.opengl.GL15;

public class GL15x {

    public static ByteBuffer glGetBufferPointer(int target, int pname) {
        int size = GL15.glGetBufferParameteri(target, GL15.GL_BUFFER_SIZE);

        PointerBuffer pb = BufferUtils.createPointerBuffer(1);
        GL15.glGetBufferPointerv(target, pname, pb);

        return pb.getByteBuffer(0, size);
    }
}
