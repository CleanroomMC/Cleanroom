package com.cleanroommc.kirino.gl.debug;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

public enum DebugMsgSource {
    ANY(GL11.GL_DONT_CARE),
    API(GL43.GL_DEBUG_SOURCE_API),
    WINDOW_SYSTEM(GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM),
    SHADER_COMPILER(GL43.GL_DEBUG_SOURCE_SHADER_COMPILER),
    THIRD_PARTY(GL43.GL_DEBUG_SOURCE_THIRD_PARTY),
    APPLICATION(GL43.GL_DEBUG_SOURCE_APPLICATION),
    OTHER(GL43.GL_DEBUG_SOURCE_OTHER);

    public final int glValue;

    DebugMsgSource(int glValue) {
        this.glValue = glValue;
    }

    public static DebugMsgSource parse(int value) {
        return switch (value) {
            case GL11.GL_DONT_CARE -> ANY;
            case GL43.GL_DEBUG_SOURCE_API -> API;
            case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> WINDOW_SYSTEM;
            case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER -> SHADER_COMPILER;
            case GL43.GL_DEBUG_SOURCE_THIRD_PARTY -> THIRD_PARTY;
            case GL43.GL_DEBUG_SOURCE_APPLICATION -> APPLICATION;
            case GL43.GL_DEBUG_SOURCE_OTHER -> OTHER;
            default -> ANY;
        };
    }
}
