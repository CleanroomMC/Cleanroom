package com.cleanroommc.kirino.gl.debug;

import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.util.List;

public final class KHRDebug {
    private static Logger LOGGER;

    private static boolean enable = false;

    public static boolean isEnable() {
        return enable;
    }

    public static void enable(@NonNull Logger logger, @NonNull List<@NonNull DebugMessageFilter> messageFilters) {
        if (enable) {
            return;
        }

        LOGGER = logger;

        GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GLDebugMessageCallback callback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
            String msg = GLDebugMessageCallback.getMessage(length, message);
            log(source, type, id, severity, msg);
        });

        GL43C.glDebugMessageCallback(callback, 0);

        // disable all
        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, false);

        for (DebugMessageFilter filter : messageFilters) {
            GL43.glDebugMessageControl(filter.getSource().glValue, filter.getType().glValue, filter.getSeverity().glValue, null, true);
        }

        enable = true;
    }

    public static void disable() {
        if (!enable) {
            return;
        }

        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        GL11.glDisable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GL43C.glDebugMessageCallback(null, 0);

        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, false);

        enable = false;
    }

    private static void log(int source, int type, int id, int severity, String message) {
        StringBuilder builder = new StringBuilder();

        DebugMsgSource source1 = DebugMsgSource.parse(source);
        DebugMsgType type1 = DebugMsgType.parse(type);
        DebugMsgSeverity severity1 = DebugMsgSeverity.parse(severity);

        builder.append("OpenGL Debug: ")
                .append(String.format("(Source=%s, ", source1.toString()))
                .append(String.format("Type=%s, ", type1.toString()))
                .append(String.format("Severity=%s, ", severity1.toString()))
                .append(String.format("ID=%d) ", id))
                .append(message).append(" Stack Trace:\n");

        for (StackTraceElement stackTraceElement : new Exception().getStackTrace()) {
            builder.append('\t').append("at")
                    .append(' ')
                    .append(stackTraceElement.getClassName())
                    .append('.')
                    .append(stackTraceElement.getMethodName())
                    .append('(')
                    .append(stackTraceElement.getFileName())
                    .append(':')
                    .append(stackTraceElement.getLineNumber())
                    .append(')')
                    .append('\n');
        }

        LOGGER.warn(builder.toString());
    }

    public static void pushGroup(String name) {
        GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1, name);
    }

    public static void popGroup() {
        GL43.glPopDebugGroup();
    }

    public static void notify(String arg) {
        GL43.glDebugMessageInsert(GL43.GL_DEBUG_SOURCE_APPLICATION, GL43.GL_DEBUG_TYPE_MARKER, 1, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, arg);
    }
}
