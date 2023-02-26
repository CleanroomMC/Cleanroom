package org.lwjglx.opengl;

public class NVTransformFeedback2 {

    public static final int GL_TRANSFORM_FEEDBACK_BINDING_NV = (int) 36389;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_ACTIVE_NV = (int) 36388;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_PAUSED_NV = (int) 36387;
    public static final int GL_TRANSFORM_FEEDBACK_NV = (int) 36386;

    public static void glBindTransformFeedbackNV(int target, int id) {
        org.lwjgl.opengl.NVTransformFeedback2.glBindTransformFeedbackNV(target, id);
    }

    public static void glDeleteTransformFeedbacksNV(int id) {
        org.lwjgl.opengl.NVTransformFeedback2.glDeleteTransformFeedbacksNV(id);
    }

    public static void glDeleteTransformFeedbacksNV(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.NVTransformFeedback2.glDeleteTransformFeedbacksNV(ids);
    }

    public static void glDrawTransformFeedbackNV(int mode, int id) {
        org.lwjgl.opengl.NVTransformFeedback2.glDrawTransformFeedbackNV(mode, id);
    }

    public static int glGenTransformFeedbacksNV() {
        return org.lwjgl.opengl.NVTransformFeedback2.glGenTransformFeedbacksNV();
    }

    public static void glGenTransformFeedbacksNV(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.NVTransformFeedback2.glGenTransformFeedbacksNV(ids);
    }

    public static boolean glIsTransformFeedbackNV(int id) {
        return org.lwjgl.opengl.NVTransformFeedback2.glIsTransformFeedbackNV(id);
    }

    public static void glPauseTransformFeedbackNV() {
        org.lwjgl.opengl.NVTransformFeedback2.glPauseTransformFeedbackNV();
    }

    public static void glResumeTransformFeedbackNV() {
        org.lwjgl.opengl.NVTransformFeedback2.glResumeTransformFeedbackNV();
    }
}
