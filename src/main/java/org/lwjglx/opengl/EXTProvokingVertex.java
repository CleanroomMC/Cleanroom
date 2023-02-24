package org.lwjglx.opengl;

public class EXTProvokingVertex {

    public static final int GL_FIRST_VERTEX_CONVENTION_EXT = (int) 36429;
    public static final int GL_LAST_VERTEX_CONVENTION_EXT = (int) 36430;
    public static final int GL_PROVOKING_VERTEX_EXT = (int) 36431;
    public static final int GL_QUADS_FOLLOW_PROVOKING_VERTEX_CONVENTION_EXT = (int) 36428;

    public static void glProvokingVertexEXT(int mode) {
        org.lwjgl.opengl.EXTProvokingVertex.glProvokingVertexEXT(mode);
    }
}
