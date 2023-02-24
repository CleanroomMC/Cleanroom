package org.lwjglx.opengl;

public class ARBVertexAttribBinding {

    public static final int GL_MAX_VERTEX_ATTRIB_BINDINGS = (int) 33498;
    public static final int GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET = (int) 33497;
    public static final int GL_VERTEX_ATTRIB_BINDING = (int) 33492;
    public static final int GL_VERTEX_ATTRIB_RELATIVE_OFFSET = (int) 33493;
    public static final int GL_VERTEX_BINDING_DIVISOR = (int) 33494;
    public static final int GL_VERTEX_BINDING_OFFSET = (int) 33495;
    public static final int GL_VERTEX_BINDING_STRIDE = (int) 33496;

    public static void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        org.lwjgl.opengl.ARBVertexAttribBinding.glBindVertexBuffer(bindingindex, buffer, offset, stride);
    }

    public static void glVertexAttribBinding(int attribindex, int bindingindex) {
        org.lwjgl.opengl.ARBVertexAttribBinding.glVertexAttribBinding(attribindex, bindingindex);
    }

    public static void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized,
            int relativeoffset) {
        org.lwjgl.opengl.ARBVertexAttribBinding
                .glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
    }

    public static void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengl.ARBVertexAttribBinding.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
    }

    public static void glVertexAttribLFormat(int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengl.ARBVertexAttribBinding.glVertexAttribLFormat(attribindex, size, type, relativeoffset);
    }

    public static void glVertexBindingDivisor(int bindingindex, int divisor) {
        org.lwjgl.opengl.ARBVertexAttribBinding.glVertexBindingDivisor(bindingindex, divisor);
    }
}
