package com.cleanroommc.kirino.gl.shader;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

public enum ShaderType {
    VERTEX(GL20.GL_VERTEX_SHADER, "vert"),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER, "frag"),
    GEOMETRY(GL32.GL_GEOMETRY_SHADER, "geom"),
    TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER, "tesc"),
    TESS_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER, "tese"),
    COMPUTE(GL43.GL_COMPUTE_SHADER, "comp");

    public final int glValue;
    public final String suffix;

    ShaderType(int glValue, String suffix) {
        this.glValue = glValue;
        this.suffix = suffix;
    }

    @Nullable
    public static ShaderType parse(String suffix) {
        return switch (suffix) {
            case "vert" -> VERTEX;
            case "frag" -> FRAGMENT;
            case "geom" -> GEOMETRY;
            case "tesc" -> TESS_CONTROL;
            case "tese" -> TESS_EVALUATION;
            case "comp" -> COMPUTE;
            default -> null;
        };
    }
}
