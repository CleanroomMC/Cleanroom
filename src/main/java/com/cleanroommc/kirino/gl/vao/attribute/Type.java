package com.cleanroommc.kirino.gl.vao.attribute;

import org.lwjgl.opengl.GL11;

public enum Type {
    BYTE(GL11.GL_BYTE, 1),
    UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE, 1),
    SHORT(GL11.GL_SHORT, 2),
    UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT, 2),
    INT(GL11.GL_INT, 4),
    UNSIGNED_INT(GL11.GL_UNSIGNED_INT, 4),
    FLOAT(GL11.GL_FLOAT, 4);

    public final int glValue;
    public final int length;

    Type(int glValue, int length) {
        this.glValue = glValue;
        this.length = length;
    }
}
