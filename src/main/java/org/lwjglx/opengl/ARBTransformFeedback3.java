package org.lwjglx.opengl;

public class ARBTransformFeedback3 {

    public static final int GL_MAX_TRANSFORM_FEEDBACK_BUFFERS = (int) 36464;
    public static final int GL_MAX_VERTEX_STREAMS = (int) 36465;

    public static void glBeginQueryIndexed(int target, int index, int id) {
        org.lwjgl.opengl.ARBTransformFeedback3.glBeginQueryIndexed(target, index, id);
    }

    public static void glDrawTransformFeedbackStream(int mode, int id, int stream) {
        org.lwjgl.opengl.ARBTransformFeedback3.glDrawTransformFeedbackStream(mode, id, stream);
    }

    public static void glEndQueryIndexed(int target, int index) {
        org.lwjgl.opengl.ARBTransformFeedback3.glEndQueryIndexed(target, index);
    }

    public static int glGetQueryIndexedi(int target, int index, int pname) {
        return org.lwjgl.opengl.ARBTransformFeedback3.glGetQueryIndexedi(target, index, pname);
    }
}
