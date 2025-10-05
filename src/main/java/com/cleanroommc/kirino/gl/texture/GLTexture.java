package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class GLTexture extends GLDisposable {
    public final int textureID;
    protected int width;
    protected int height;
    protected TextureFormat currentFormat = null;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Nullable
    public TextureFormat currentFormat() {
        return currentFormat;
    }

    public GLTexture(int width, int height) {
        this.width = width;
        this.height = height;
        textureID = GL11.glGenTextures();
        GLResourceManager.addDisposable(this);
    }

    public void dispose() {
        GL11.glDeleteTextures(textureID);
    }
}
