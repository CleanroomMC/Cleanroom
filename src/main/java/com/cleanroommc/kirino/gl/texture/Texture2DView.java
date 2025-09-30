package com.cleanroommc.kirino.gl.texture;

import org.lwjgl.opengl.GL11;

public class Texture2DView extends TextureView {
    public Texture2DView(GLTexture texture) {
        super(texture);
    }

    @Override
    public int target() {
        return GL11.GL_TEXTURE_2D;
    }
}
