package com.cleanroommc.kirino.gl.debug;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

public enum DebugMsgSeverity {
    ANY(GL11.GL_DONT_CARE),
    HIGH(GL43.GL_DEBUG_SEVERITY_HIGH),
    MEDIUM(GL43.GL_DEBUG_SEVERITY_MEDIUM),
    LOW(GL43.GL_DEBUG_SEVERITY_LOW),
    NOTIFICATION(GL43.GL_DEBUG_SEVERITY_NOTIFICATION);

    public final int glValue;

    DebugMsgSeverity(int glValue) {
        this.glValue = glValue;
    }

    public static DebugMsgSeverity parse(int value) {
        return switch (value) {
            case GL11.GL_DONT_CARE -> ANY;
            case GL43.GL_DEBUG_SEVERITY_HIGH -> HIGH;
            case GL43.GL_DEBUG_SEVERITY_MEDIUM -> MEDIUM;
            case GL43.GL_DEBUG_SEVERITY_LOW -> LOW;
            case GL43.GL_DEBUG_SEVERITY_NOTIFICATION -> NOTIFICATION;
            default -> ANY;
        };
    }
}
