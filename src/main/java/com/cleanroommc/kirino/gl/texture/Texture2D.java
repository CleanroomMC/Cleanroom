package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL11;

public final class Texture2D extends GLDisposable implements ITexture {
    private final int textureID;
    private final int width;
    private final int height;

    @Override
    public int textureID() {
        return textureID;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int target() {
        return GL11.GL_TEXTURE_2D;
    }

    public Texture2D(int width, int height) {
        this.width = width;
        this.height = height;
        textureID = GL11.glGenTextures();
        GLResourceManager.addDisposable(this);
    }

    @Override
    public void dispose() {
        GL11.glDeleteTextures(textureID);
    }
}
