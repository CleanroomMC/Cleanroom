package org.lwjglx.opengl;

public class ARBProvokingVertex {

    public static final int GL_FIRST_VERTEX_CONVENTION = (int) 36429;
    public static final int GL_LAST_VERTEX_CONVENTION = (int) 36430;
    public static final int GL_PROVOKING_VERTEX = (int) 36431;
    public static final int GL_QUADS_FOLLOW_PROVOKING_VERTEX_CONVENTION = (int) 36428;

    public static void glProvokingVertex(int mode) {
        org.lwjgl.opengl.ARBProvokingVertex.glProvokingVertex(mode);
    }
}
