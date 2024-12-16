package org.lwjglx.opengl;

public class ARBDebugOutput {

    public static final int GL_DEBUG_CALLBACK_FUNCTION_ARB = (int) 33348;
    public static final int GL_DEBUG_CALLBACK_USER_PARAM_ARB = (int) 33349;
    public static final int GL_DEBUG_LOGGED_MESSAGES_ARB = (int) 37189;
    public static final int GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH_ARB = (int) 33347;
    public static final int GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB = (int) 33346;
    public static final int GL_DEBUG_SEVERITY_HIGH_ARB = (int) 37190;
    public static final int GL_DEBUG_SEVERITY_LOW_ARB = (int) 37192;
    public static final int GL_DEBUG_SEVERITY_MEDIUM_ARB = (int) 37191;
    public static final int GL_DEBUG_SOURCE_API_ARB = (int) 33350;
    public static final int GL_DEBUG_SOURCE_APPLICATION_ARB = (int) 33354;
    public static final int GL_DEBUG_SOURCE_OTHER_ARB = (int) 33355;
    public static final int GL_DEBUG_SOURCE_SHADER_COMPILER_ARB = (int) 33352;
    public static final int GL_DEBUG_SOURCE_THIRD_PARTY_ARB = (int) 33353;
    public static final int GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB = (int) 33351;
    public static final int GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB = (int) 33357;
    public static final int GL_DEBUG_TYPE_ERROR_ARB = (int) 33356;
    public static final int GL_DEBUG_TYPE_OTHER_ARB = (int) 33361;
    public static final int GL_DEBUG_TYPE_PERFORMANCE_ARB = (int) 33360;
    public static final int GL_DEBUG_TYPE_PORTABILITY_ARB = (int) 33359;
    public static final int GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB = (int) 33358;
    public static final int GL_MAX_DEBUG_LOGGED_MESSAGES_ARB = (int) 37188;
    public static final int GL_MAX_DEBUG_MESSAGE_LENGTH_ARB = (int) 37187;

    public static void glDebugMessageControlARB(int source, int type, int severity, java.nio.IntBuffer ids,
            boolean enabled) {
        org.lwjgl.opengl.ARBDebugOutput.glDebugMessageControlARB(source, type, severity, ids, enabled);
    }

    public static void glDebugMessageInsertARB(int source, int type, int id, int severity, java.lang.CharSequence buf) {
        org.lwjgl.opengl.ARBDebugOutput.glDebugMessageInsertARB(source, type, id, severity, buf);
    }

    public static void glDebugMessageInsertARB(int source, int type, int id, int severity, java.nio.ByteBuffer buf) {
        org.lwjgl.opengl.ARBDebugOutput.glDebugMessageInsertARB(source, type, id, severity, buf);
    }

    public static int glGetDebugMessageLogARB(int count, java.nio.IntBuffer sources, java.nio.IntBuffer types,
            java.nio.IntBuffer ids, java.nio.IntBuffer severities, java.nio.IntBuffer lengths,
            java.nio.ByteBuffer messageLog) {
        return org.lwjgl.opengl.ARBDebugOutput
                .glGetDebugMessageLogARB(count, sources, types, ids, severities, lengths, messageLog);
    }
}
