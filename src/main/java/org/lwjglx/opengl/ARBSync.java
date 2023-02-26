package org.lwjglx.opengl;

public class ARBSync {

    public static final int GL_ALREADY_SIGNALED = (int) 37146;
    public static final int GL_CONDITION_SATISFIED = (int) 37148;
    public static final int GL_MAX_SERVER_WAIT_TIMEOUT = (int) 37137;
    public static final int GL_OBJECT_TYPE = (int) 37138;
    public static final int GL_SIGNALED = (int) 37145;
    public static final int GL_SYNC_CONDITION = (int) 37139;
    public static final int GL_SYNC_FENCE = (int) 37142;
    public static final int GL_SYNC_FLAGS = (int) 37141;
    public static final int GL_SYNC_FLUSH_COMMANDS_BIT = (int) 1;
    public static final int GL_SYNC_GPU_COMMANDS_COMPLETE = (int) 37143;
    public static final int GL_SYNC_STATUS = (int) 37140;
    public static final int GL_TIMEOUT_EXPIRED = (int) 37147;
    public static final long GL_TIMEOUT_IGNORED = (long) -1;
    public static final int GL_UNSIGNALED = (int) 37144;
    public static final int GL_WAIT_FAILED = (int) 37149;

    public static long glGetInteger64(int pname) {
        return org.lwjgl.opengl.ARBSync.glGetInteger64(pname);
    }
}
