package org.lwjglx.opengl;

public class ARBOcclusionQuery {

    public static final int GL_CURRENT_QUERY_ARB = (int) 34917;
    public static final int GL_QUERY_COUNTER_BITS_ARB = (int) 34916;
    public static final int GL_QUERY_RESULT_ARB = (int) 34918;
    public static final int GL_QUERY_RESULT_AVAILABLE_ARB = (int) 34919;
    public static final int GL_SAMPLES_PASSED_ARB = (int) 35092;

    public static void glBeginQueryARB(int target, int id) {
        org.lwjgl.opengl.ARBOcclusionQuery.glBeginQueryARB(target, id);
    }

    public static void glDeleteQueriesARB(int id) {
        org.lwjgl.opengl.ARBOcclusionQuery.glDeleteQueriesARB(id);
    }

    public static void glDeleteQueriesARB(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.ARBOcclusionQuery.glDeleteQueriesARB(ids);
    }

    public static void glEndQueryARB(int target) {
        org.lwjgl.opengl.ARBOcclusionQuery.glEndQueryARB(target);
    }

    public static int glGenQueriesARB() {
        return org.lwjgl.opengl.ARBOcclusionQuery.glGenQueriesARB();
    }

    public static void glGenQueriesARB(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.ARBOcclusionQuery.glGenQueriesARB(ids);
    }

    public static void glGetQueryARB(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBOcclusionQuery.glGetQueryivARB(target, pname, params);
    }

    public static void glGetQueryObjectARB(int id, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBOcclusionQuery.glGetQueryObjectivARB(id, pname, params);
    }

    public static int glGetQueryObjectiARB(int id, int pname) {
        return org.lwjgl.opengl.ARBOcclusionQuery.glGetQueryObjectiARB(id, pname);
    }

    public static void glGetQueryObjectuARB(int id, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBOcclusionQuery.glGetQueryObjectuivARB(id, pname, params);
    }

    public static int glGetQueryObjectuiARB(int id, int pname) {
        return org.lwjgl.opengl.ARBOcclusionQuery.glGetQueryObjectuiARB(id, pname);
    }

    public static int glGetQueryiARB(int target, int pname) {
        return org.lwjgl.opengl.ARBOcclusionQuery.glGetQueryiARB(target, pname);
    }

    public static boolean glIsQueryARB(int id) {
        return org.lwjgl.opengl.ARBOcclusionQuery.glIsQueryARB(id);
    }
}
