package org.lwjglx.opengl;

public class NVTransformFeedback {

    public static final int GL_ACTIVE_VARYINGS_NV = (int) 35969;
    public static final int GL_ACTIVE_VARYING_MAX_LENGTH_NV = (int) 35970;
    public static final int GL_BACK_PRIMARY_COLOR_NV = (int) 35959;
    public static final int GL_BACK_SECONDARY_COLOR_NV = (int) 35960;
    public static final int GL_CLIP_DISTANCE_NV = (int) 35962;
    public static final int GL_GENERIC_ATTRIB_NV = (int) 35965;
    public static final int GL_INTERLEAVED_ATTRIBS_NV = (int) 35980;
    public static final int GL_LAYER_NV = (int) 36266;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS_NV = (int) 35978;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS_NV = (int) 35979;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS_NV = (int) 35968;
    public static final int GL_PRIMITIVES_GENERATED_NV = (int) 35975;
    public static final int GL_PRIMITIVE_ID_NV = (int) 35964;
    public static final int GL_RASTERIZER_DISCARD_NV = (int) 35977;
    public static final int GL_SEPARATE_ATTRIBS_NV = (int) 35981;
    public static final int GL_TEXTURE_COORD_NV = (int) 35961;
    public static final int GL_TRANSFORM_FEEDBACK_ATTRIBS_NV = (int) 35966;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_BINDING_NV = (int) 35983;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_MODE_NV = (int) 35967;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_NV = (int) 35982;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_SIZE_NV = (int) 35973;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_START_NV = (int) 35972;
    public static final int GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN_NV = (int) 35976;
    public static final int GL_TRANSFORM_FEEDBACK_RECORD_NV = (int) 35974;
    public static final int GL_TRANSFORM_FEEDBACK_VARYINGS_NV = (int) 35971;
    public static final int GL_VERTEX_ID_NV = (int) 35963;

    public static void glActiveVaryingNV(int program, java.lang.CharSequence name) {
        org.lwjgl.opengl.NVTransformFeedback.glActiveVaryingNV(program, name);
    }

    public static void glActiveVaryingNV(int program, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.NVTransformFeedback.glActiveVaryingNV(program, name);
    }

    public static void glBeginTransformFeedbackNV(int primitiveMode) {
        org.lwjgl.opengl.NVTransformFeedback.glBeginTransformFeedbackNV(primitiveMode);
    }

    public static void glBindBufferBaseNV(int target, int index, int buffer) {
        org.lwjgl.opengl.NVTransformFeedback.glBindBufferBaseNV(target, index, buffer);
    }

    public static void glBindBufferOffsetNV(int target, int index, int buffer, long offset) {
        org.lwjgl.opengl.NVTransformFeedback.glBindBufferOffsetNV(target, index, buffer, offset);
    }

    public static void glBindBufferRangeNV(int target, int index, int buffer, long offset, long size) {
        org.lwjgl.opengl.NVTransformFeedback.glBindBufferRangeNV(target, index, buffer, offset, size);
    }

    public static void glEndTransformFeedbackNV() {
        org.lwjgl.opengl.NVTransformFeedback.glEndTransformFeedbackNV();
    }

    public static void glGetActiveVaryingNV(int program, int index, java.nio.IntBuffer length, java.nio.IntBuffer size,
            java.nio.IntBuffer type, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.NVTransformFeedback.glGetActiveVaryingNV(program, index, length, size, type, name);
    }

    public static int glGetTransformFeedbackVaryingNV(int program, int index) {
        return org.lwjgl.opengl.NVTransformFeedback.glGetTransformFeedbackVaryingNV(program, index);
    }

    public static void glGetTransformFeedbackVaryingNV(int program, int index, java.nio.IntBuffer location) {
        org.lwjgl.opengl.NVTransformFeedback.glGetTransformFeedbackVaryingNV(program, index, location);
    }

    public static int glGetVaryingLocationNV(int program, java.lang.CharSequence name) {
        return org.lwjgl.opengl.NVTransformFeedback.glGetVaryingLocationNV(program, name);
    }

    public static int glGetVaryingLocationNV(int program, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.NVTransformFeedback.glGetVaryingLocationNV(program, name);
    }

    public static void glTransformFeedbackAttribsNV(java.nio.IntBuffer attribs, int bufferMode) {
        org.lwjgl.opengl.NVTransformFeedback.glTransformFeedbackAttribsNV(attribs, bufferMode);
    }

    public static void glTransformFeedbackVaryingsNV(int program, java.nio.IntBuffer locations, int bufferMode) {
        org.lwjgl.opengl.NVTransformFeedback.glTransformFeedbackVaryingsNV(program, locations, bufferMode);
    }
}
