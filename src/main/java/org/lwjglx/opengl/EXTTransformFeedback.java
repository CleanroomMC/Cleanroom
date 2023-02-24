package org.lwjglx.opengl;

public class EXTTransformFeedback {

    public static final int GL_INTERLEAVED_ATTRIBS_EXT = (int) 35980;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS_EXT = (int) 35978;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS_EXT = (int) 35979;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS_EXT = (int) 35968;
    public static final int GL_PRIMITIVES_GENERATED_EXT = (int) 35975;
    public static final int GL_RASTERIZER_DISCARD_EXT = (int) 35977;
    public static final int GL_SEPARATE_ATTRIBS_EXT = (int) 35981;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_BINDING_EXT = (int) 35983;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_EXT = (int) 35982;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_MODE_EXT = (int) 35967;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_SIZE_EXT = (int) 35973;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_START_EXT = (int) 35972;
    public static final int GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN_EXT = (int) 35976;
    public static final int GL_TRANSFORM_FEEDBACK_VARYINGS_EXT = (int) 35971;
    public static final int GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH_EXT = (int) 35958;

    public static void glBeginTransformFeedbackEXT(int primitiveMode) {
        org.lwjgl.opengl.EXTTransformFeedback.glBeginTransformFeedbackEXT(primitiveMode);
    }

    public static void glBindBufferBaseEXT(int target, int index, int buffer) {
        org.lwjgl.opengl.EXTTransformFeedback.glBindBufferBaseEXT(target, index, buffer);
    }

    public static void glBindBufferOffsetEXT(int target, int index, int buffer, long offset) {
        org.lwjgl.opengl.EXTTransformFeedback.glBindBufferOffsetEXT(target, index, buffer, offset);
    }

    public static void glBindBufferRangeEXT(int target, int index, int buffer, long offset, long size) {
        org.lwjgl.opengl.EXTTransformFeedback.glBindBufferRangeEXT(target, index, buffer, offset, size);
    }

    public static void glEndTransformFeedbackEXT() {
        org.lwjgl.opengl.EXTTransformFeedback.glEndTransformFeedbackEXT();
    }

    public static java.lang.String glGetTransformFeedbackVaryingEXT(int program, int index, int bufSize,
            java.nio.IntBuffer size, java.nio.IntBuffer type) {
        return org.lwjgl.opengl.EXTTransformFeedback
                .glGetTransformFeedbackVaryingEXT(program, index, bufSize, size, type);
    }

    public static void glGetTransformFeedbackVaryingEXT(int program, int index, java.nio.IntBuffer length,
            java.nio.IntBuffer size, java.nio.IntBuffer type, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.EXTTransformFeedback
                .glGetTransformFeedbackVaryingEXT(program, index, length, size, type, name);
    }

    public static void glTransformFeedbackVaryingsEXT(int program, java.lang.CharSequence[] varyings, int bufferMode) {
        org.lwjgl.opengl.EXTTransformFeedback.glTransformFeedbackVaryingsEXT(program, varyings, bufferMode);
    }
}
