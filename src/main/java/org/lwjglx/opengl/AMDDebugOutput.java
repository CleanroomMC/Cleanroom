package org.lwjglx.opengl;

public class AMDDebugOutput {

    public static final int GL_DEBUG_CATEGORY_API_ERROR_AMD = (int) 37193;
    public static final int GL_DEBUG_CATEGORY_APPLICATION_AMD = (int) 37199;
    public static final int GL_DEBUG_CATEGORY_DEPRECATION_AMD = (int) 37195;
    public static final int GL_DEBUG_CATEGORY_OTHER_AMD = (int) 37200;
    public static final int GL_DEBUG_CATEGORY_PERFORMANCE_AMD = (int) 37197;
    public static final int GL_DEBUG_CATEGORY_SHADER_COMPILER_AMD = (int) 37198;
    public static final int GL_DEBUG_CATEGORY_UNDEFINED_BEHAVIOR_AMD = (int) 37196;
    public static final int GL_DEBUG_CATEGORY_WINDOW_SYSTEM_AMD = (int) 37194;
    public static final int GL_DEBUG_LOGGED_MESSAGES_AMD = (int) 37189;
    public static final int GL_DEBUG_SEVERITY_HIGH_AMD = (int) 37190;
    public static final int GL_DEBUG_SEVERITY_LOW_AMD = (int) 37192;
    public static final int GL_DEBUG_SEVERITY_MEDIUM_AMD = (int) 37191;
    public static final int GL_MAX_DEBUG_LOGGED_MESSAGES_AMD = (int) 37188;
    public static final int GL_MAX_DEBUG_MESSAGE_LENGTH_AMD = (int) 37187;

    public static void glDebugMessageEnableAMD(int category, int severity, java.nio.IntBuffer ids, boolean enabled) {
        org.lwjgl.opengl.AMDDebugOutput.glDebugMessageEnableAMD(category, severity, ids, enabled);
    }

    public static void glDebugMessageInsertAMD(int category, int severity, int id, java.lang.CharSequence buf) {
        org.lwjgl.opengl.AMDDebugOutput.glDebugMessageInsertAMD(category, severity, id, buf);
    }

    public static void glDebugMessageInsertAMD(int category, int severity, int id, java.nio.ByteBuffer buf) {
        org.lwjgl.opengl.AMDDebugOutput.glDebugMessageInsertAMD(category, severity, id, buf);
    }

    public static int glGetDebugMessageLogAMD(int count, java.nio.IntBuffer categories, java.nio.IntBuffer severities,
            java.nio.IntBuffer ids, java.nio.IntBuffer lengths, java.nio.ByteBuffer messageLog) {
        return org.lwjgl.opengl.AMDDebugOutput
                .glGetDebugMessageLogAMD(count, categories, severities, ids, lengths, messageLog);
    }
}
