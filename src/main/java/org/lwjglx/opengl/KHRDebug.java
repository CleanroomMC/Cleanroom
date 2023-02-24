package org.lwjglx.opengl;

public class KHRDebug {

    public static final int GL_BUFFER = (int) 33504;
    public static final int GL_CONTEXT_FLAG_DEBUG_BIT = (int) 2;
    public static final int GL_DEBUG_CALLBACK_FUNCTION = (int) 33348;
    public static final int GL_DEBUG_CALLBACK_USER_PARAM = (int) 33349;
    public static final int GL_DEBUG_GROUP_STACK_DEPTH = (int) 33389;
    public static final int GL_DEBUG_LOGGED_MESSAGES = (int) 37189;
    public static final int GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH = (int) 33347;
    public static final int GL_DEBUG_OUTPUT = (int) 37600;
    public static final int GL_DEBUG_OUTPUT_SYNCHRONOUS = (int) 33346;
    public static final int GL_DEBUG_SEVERITY_HIGH = (int) 37190;
    public static final int GL_DEBUG_SEVERITY_LOW = (int) 37192;
    public static final int GL_DEBUG_SEVERITY_MEDIUM = (int) 37191;
    public static final int GL_DEBUG_SEVERITY_NOTIFICATION = (int) 33387;
    public static final int GL_DEBUG_SOURCE_API = (int) 33350;
    public static final int GL_DEBUG_SOURCE_APPLICATION = (int) 33354;
    public static final int GL_DEBUG_SOURCE_OTHER = (int) 33355;
    public static final int GL_DEBUG_SOURCE_SHADER_COMPILER = (int) 33352;
    public static final int GL_DEBUG_SOURCE_THIRD_PARTY = (int) 33353;
    public static final int GL_DEBUG_SOURCE_WINDOW_SYSTEM = (int) 33351;
    public static final int GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR = (int) 33357;
    public static final int GL_DEBUG_TYPE_ERROR = (int) 33356;
    public static final int GL_DEBUG_TYPE_MARKER = (int) 33384;
    public static final int GL_DEBUG_TYPE_OTHER = (int) 33361;
    public static final int GL_DEBUG_TYPE_PERFORMANCE = (int) 33360;
    public static final int GL_DEBUG_TYPE_POP_GROUP = (int) 33386;
    public static final int GL_DEBUG_TYPE_PORTABILITY = (int) 33359;
    public static final int GL_DEBUG_TYPE_PUSH_GROUP = (int) 33385;
    public static final int GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR = (int) 33358;
    public static final int GL_DISPLAY_LIST = (int) 33511;
    public static final int GL_MAX_DEBUG_GROUP_STACK_DEPTH = (int) 33388;
    public static final int GL_MAX_DEBUG_LOGGED_MESSAGES = (int) 37188;
    public static final int GL_MAX_DEBUG_MESSAGE_LENGTH = (int) 37187;
    public static final int GL_MAX_LABEL_LENGTH = (int) 33512;
    public static final int GL_PROGRAM = (int) 33506;
    public static final int GL_PROGRAM_PIPELINE = (int) 33508;
    public static final int GL_QUERY = (int) 33507;
    public static final int GL_SAMPLER = (int) 33510;
    public static final int GL_SHADER = (int) 33505;

    public static void glDebugMessageControl(int source, int type, int severity, java.nio.IntBuffer ids,
            boolean enabled) {
        org.lwjgl.opengl.KHRDebug.glDebugMessageControl(source, type, severity, ids, enabled);
    }

    public static void glDebugMessageInsert(int source, int type, int id, int severity, java.lang.CharSequence buf) {
        org.lwjgl.opengl.KHRDebug.glDebugMessageInsert(source, type, id, severity, buf);
    }

    public static void glDebugMessageInsert(int source, int type, int id, int severity, java.nio.ByteBuffer buf) {
        org.lwjgl.opengl.KHRDebug.glDebugMessageInsert(source, type, id, severity, buf);
    }

    public static int glGetDebugMessageLog(int count, java.nio.IntBuffer sources, java.nio.IntBuffer types,
            java.nio.IntBuffer ids, java.nio.IntBuffer severities, java.nio.IntBuffer lengths,
            java.nio.ByteBuffer messageLog) {
        return org.lwjgl.opengl.KHRDebug
                .glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
    }

    public static java.lang.String glGetObjectLabel(int identifier, int name, int bufSize) {
        return org.lwjgl.opengl.KHRDebug.glGetObjectLabel(identifier, name, bufSize);
    }

    public static void glGetObjectLabel(int identifier, int name, java.nio.IntBuffer length,
            java.nio.ByteBuffer label) {
        org.lwjgl.opengl.KHRDebug.glGetObjectLabel(identifier, name, length, label);
    }

    public static void glObjectLabel(int identifier, int name, java.lang.CharSequence label) {
        org.lwjgl.opengl.KHRDebug.glObjectLabel(identifier, name, label);
    }

    public static void glObjectLabel(int identifier, int name, java.nio.ByteBuffer label) {
        org.lwjgl.opengl.KHRDebug.glObjectLabel(identifier, name, label);
    }

    public static void glPopDebugGroup() {
        org.lwjgl.opengl.KHRDebug.glPopDebugGroup();
    }

    public static void glPushDebugGroup(int source, int id, java.lang.CharSequence message) {
        org.lwjgl.opengl.KHRDebug.glPushDebugGroup(source, id, message);
    }

    public static void glPushDebugGroup(int source, int id, java.nio.ByteBuffer message) {
        org.lwjgl.opengl.KHRDebug.glPushDebugGroup(source, id, message);
    }
}
