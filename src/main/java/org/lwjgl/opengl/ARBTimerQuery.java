package org.lwjgl.opengl;

public class ARBTimerQuery {

    public static final int GL_TIMESTAMP = (int) 36392;
    public static final int GL_TIME_ELAPSED = (int) 35007;

    public static long glGetQueryObjecti64(int id, int pname) {
        return org.lwjgl3.opengl.ARBTimerQuery.glGetQueryObjecti64(id, pname);
    }

    public static long glGetQueryObjectui64(int id, int pname) {
        return org.lwjgl3.opengl.ARBTimerQuery.glGetQueryObjectui64(id, pname);
    }

    public static void glQueryCounter(int id, int target) {
        org.lwjgl3.opengl.ARBTimerQuery.glQueryCounter(id, target);
    }
}
