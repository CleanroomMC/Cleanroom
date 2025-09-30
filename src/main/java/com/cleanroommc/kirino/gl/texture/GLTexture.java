package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL11;

public class GLTexture extends GLDisposable {
    public final int textureID;
    public final int width;
    public final int height;

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
