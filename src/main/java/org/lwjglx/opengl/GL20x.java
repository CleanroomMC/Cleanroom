package org.lwjglx.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

public class GL20x {

    public static void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride,
            ByteBuffer buffer) {
        int type = unsigned ? GL11.GL_UNSIGNED_BYTE : GL11.GL_BYTE;
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer);
    }

    public static void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride,
            ShortBuffer buffer) {
        int type = unsigned ? GL11.GL_UNSIGNED_SHORT : GL11.GL_SHORT;
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, MemoryUtil.memAddress(buffer));
    }

    public static void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride,
            IntBuffer buffer) {
        int type = unsigned ? GL11.GL_UNSIGNED_INT : GL11.GL_INT;
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, MemoryUtil.memAddress(buffer));
    }

    public static String glGetActiveAttrib(int program, int index, int maxLength, IntBuffer sizeType) {
        // TODO check if correct
        IntBuffer type = BufferUtils.createIntBuffer(1);
        String s = GL20.glGetActiveAttrib(program, index, maxLength, sizeType, type);
        sizeType.put(type.get(0));
        return s;
    }

    public static String glGetActiveUniform(int program, int index, int maxLength, IntBuffer sizeType) {
        // TODO if correct
        IntBuffer type = BufferUtils.createIntBuffer(1);
        String s = GL20.glGetActiveUniform(program, index, maxLength, sizeType, type);
        sizeType.put(type.get(0));
        return s;
    }

    public static void glShaderSource(int shader, java.nio.ByteBuffer string) {
        PointerBuffer strings = BufferUtils.createPointerBuffer(1);
        IntBuffer lengths = BufferUtils.createIntBuffer(1);

        strings.put(0, string);
        lengths.put(0, new String(string.array()).length()); // source.length());
        org.lwjgl.opengl.GL20.glShaderSource(shader, strings, lengths);
    }
}
