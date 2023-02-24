package org.lwjglx.opengl;

public class ARBVertexProgram extends org.lwjglx.opengl.ARBProgram {

    public static final int GL_COLOR_SUM_ARB = (int) 33880;
    public static final int GL_CURRENT_VERTEX_ATTRIB_ARB = (int) 34342;
    public static final int GL_MAX_PROGRAM_ADDRESS_REGISTERS_ARB = (int) 34993;
    public static final int GL_MAX_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB = (int) 34995;
    public static final int GL_MAX_VERTEX_ATTRIBS_ARB = (int) 34921;
    public static final int GL_PROGRAM_ADDRESS_REGISTERS_ARB = (int) 34992;
    public static final int GL_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB = (int) 34994;
    public static final int GL_VERTEX_ATTRIB_ARRAY_ENABLED_ARB = (int) 34338;
    public static final int GL_VERTEX_ATTRIB_ARRAY_NORMALIZED_ARB = (int) 34922;
    public static final int GL_VERTEX_ATTRIB_ARRAY_POINTER_ARB = (int) 34373;
    public static final int GL_VERTEX_ATTRIB_ARRAY_SIZE_ARB = (int) 34339;
    public static final int GL_VERTEX_ATTRIB_ARRAY_STRIDE_ARB = (int) 34340;
    public static final int GL_VERTEX_ATTRIB_ARRAY_TYPE_ARB = (int) 34341;
    public static final int GL_VERTEX_PROGRAM_ARB = (int) 34336;
    public static final int GL_VERTEX_PROGRAM_POINT_SIZE_ARB = (int) 34370;
    public static final int GL_VERTEX_PROGRAM_TWO_SIDE_ARB = (int) 34371;

    public static void glDisableVertexAttribArrayARB(int index) {
        org.lwjgl.opengl.ARBVertexProgram.glDisableVertexAttribArrayARB(index);
    }

    public static void glEnableVertexAttribArrayARB(int index) {
        org.lwjgl.opengl.ARBVertexProgram.glEnableVertexAttribArrayARB(index);
    }

    public static void glVertexAttrib1dARB(int index, double x) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib1dARB(index, x);
    }

    public static void glVertexAttrib1fARB(int index, float x) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib1fARB(index, x);
    }

    public static void glVertexAttrib1sARB(int index, short x) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib1sARB(index, x);
    }

    public static void glVertexAttrib2dARB(int index, double x, double y) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib2dARB(index, x, y);
    }

    public static void glVertexAttrib2fARB(int index, float x, float y) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib2fARB(index, x, y);
    }

    public static void glVertexAttrib2sARB(int index, short x, short y) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib2sARB(index, x, y);
    }

    public static void glVertexAttrib3dARB(int index, double x, double y, double z) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib3dARB(index, x, y, z);
    }

    public static void glVertexAttrib3fARB(int index, float x, float y, float z) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib3fARB(index, x, y, z);
    }

    public static void glVertexAttrib3sARB(int index, short x, short y, short z) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib3sARB(index, x, y, z);
    }

    public static void glVertexAttrib4NubARB(int index, byte x, byte y, byte z, byte w) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib4NubARB(index, x, y, z, w);
    }

    public static void glVertexAttrib4dARB(int index, double x, double y, double z, double w) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib4dARB(index, x, y, z, w);
    }

    public static void glVertexAttrib4fARB(int index, float x, float y, float z, float w) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib4fARB(index, x, y, z, w);
    }

    public static void glVertexAttrib4sARB(int index, short x, short y, short z, short w) {
        org.lwjgl.opengl.ARBVertexProgram.glVertexAttrib4sARB(index, x, y, z, w);
    }

    public static void glVertexAttribPointerARB(int index, int size, int type, boolean normalized, int stride,
            long buffer_buffer_offset) {
        org.lwjgl.opengl.ARBVertexProgram
                .glVertexAttribPointerARB(index, size, type, normalized, stride, buffer_buffer_offset);
    }
}
