package org.lwjglx.opengl;

public class ARBTimerQuery {

    public static final int GL_TIMESTAMP = (int) 36392;
    public static final int GL_TIME_ELAPSED = (int) 35007;

    public static long glGetQueryObjecti64(int id, int pname) {
        return org.lwjgl.opengl.ARBTimerQuery.glGetQueryObjecti64(id, pname);
    }

    public static long glGetQueryObjectui64(int id, int pname) {
        return org.lwjgl.opengl.ARBTimerQuery.glGetQueryObjectui64(id, pname);
    }

    public static void glQueryCounter(int id, int target) {
        org.lwjgl.opengl.ARBTimerQuery.glQueryCounter(id, target);
    }
}
