package org.lwjglx.opengl;

public class NVVertexBufferUnifiedMemory {

    public static final int GL_COLOR_ARRAY_ADDRESS_NV = (int) 36643;
    public static final int GL_COLOR_ARRAY_LENGTH_NV = (int) 36653;
    public static final int GL_EDGE_FLAG_ARRAY_ADDRESS_NV = (int) 36646;
    public static final int GL_EDGE_FLAG_ARRAY_LENGTH_NV = (int) 36656;
    public static final int GL_ELEMENT_ARRAY_ADDRESS_NV = (int) 36649;
    public static final int GL_ELEMENT_ARRAY_LENGTH_NV = (int) 36659;
    public static final int GL_ELEMENT_ARRAY_UNIFIED_NV = (int) 36639;
    public static final int GL_FOG_COORD_ARRAY_ADDRESS_NV = (int) 36648;
    public static final int GL_FOG_COORD_ARRAY_LENGTH_NV = (int) 36658;
    public static final int GL_INDEX_ARRAY_ADDRESS_NV = (int) 36644;
    public static final int GL_INDEX_ARRAY_LENGTH_NV = (int) 36654;
    public static final int GL_NORMAL_ARRAY_ADDRESS_NV = (int) 36642;
    public static final int GL_NORMAL_ARRAY_LENGTH_NV = (int) 36652;
    public static final int GL_SECONDARY_COLOR_ARRAY_ADDRESS_NV = (int) 36647;
    public static final int GL_SECONDARY_COLOR_ARRAY_LENGTH_NV = (int) 36657;
    public static final int GL_TEXTURE_COORD_ARRAY_ADDRESS_NV = (int) 36645;
    public static final int GL_TEXTURE_COORD_ARRAY_LENGTH_NV = (int) 36655;
    public static final int GL_VERTEX_ARRAY_ADDRESS_NV = (int) 36641;
    public static final int GL_VERTEX_ARRAY_LENGTH_NV = (int) 36651;
    public static final int GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV = (int) 36640;
    public static final int GL_VERTEX_ATTRIB_ARRAY_LENGTH_NV = (int) 36650;
    public static final int GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV = (int) 36638;

    public static void glBufferAddressRangeNV(int pname, int index, long address, long length) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glBufferAddressRangeNV(pname, index, address, length);
    }

    public static void glColorFormatNV(int size, int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glColorFormatNV(size, type, stride);
    }

    public static void glEdgeFlagFormatNV(int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glEdgeFlagFormatNV(stride);
    }

    public static void glFogCoordFormatNV(int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glFogCoordFormatNV(type, stride);
    }

    public static void glGetIntegeruNV(int value, int index, java.nio.LongBuffer result) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glGetIntegerui64i_vNV(value, index, result);
    }

    public static void glIndexFormatNV(int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glIndexFormatNV(type, stride);
    }

    public static void glNormalFormatNV(int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glNormalFormatNV(type, stride);
    }

    public static void glSecondaryColorFormatNV(int size, int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glSecondaryColorFormatNV(size, type, stride);
    }

    public static void glTexCoordFormatNV(int size, int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glTexCoordFormatNV(size, type, stride);
    }

    public static void glVertexAttribFormatNV(int index, int size, int type, boolean normalized, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glVertexAttribFormatNV(index, size, type, normalized, stride);
    }

    public static void glVertexAttribIFormatNV(int index, int size, int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glVertexAttribIFormatNV(index, size, type, stride);
    }

    public static void glVertexFormatNV(int size, int type, int stride) {
        org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glVertexFormatNV(size, type, stride);
    }
}
