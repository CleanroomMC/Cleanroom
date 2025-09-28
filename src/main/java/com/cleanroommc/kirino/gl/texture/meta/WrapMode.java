package com.cleanroommc.kirino.gl.texture.meta;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

public enum WrapMode {
    REPEAT(GL11.GL_REPEAT),
    CLAMP(GL11.GL_CLAMP),
    CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GL13.GL_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GL14.GL_MIRRORED_REPEAT);

    public final int glValue;

    WrapMode(int glValue) {
        this.glValue = glValue;
    }
}
