package com.cleanroommc.kirino.gl.texture;

import org.lwjgl.opengl.GL11;

public interface ITexture {
    int textureID();
    int width();
    int height();
    int target();

    default void bind(int tex) {
        GL11.glBindTexture(target(), tex);
    }

    default void bind() {
        bind(textureID());
    }
}
