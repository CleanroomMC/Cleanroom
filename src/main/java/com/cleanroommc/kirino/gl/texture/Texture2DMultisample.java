package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

public final class Texture2DMultisample extends GLDisposable implements ITexture {
    private final int textureID;
    private final int width;
    private final int height;
    private final int samples;

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
        return GL32.GL_TEXTURE_2D_MULTISAMPLE;
    }

    public int samples() {
        return samples;
    }

    public Texture2DMultisample(int width, int height, int samples) {
        this.width = width;
        this.height = height;
        this.samples = samples;
        textureID = GL11.glGenTextures();
        GLResourceManager.addDisposable(this);
    }

    @Override
    public void dispose() {
        GL11.glDeleteTextures(textureID);
    }
}
