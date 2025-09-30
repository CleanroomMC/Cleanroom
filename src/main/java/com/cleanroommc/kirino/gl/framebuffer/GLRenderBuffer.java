package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import org.lwjgl.opengl.GL30;

public class GLRenderBuffer extends GLDisposable {
    public final int rboID;
    public final int width;
    public final int height;

    public GLRenderBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        rboID = GL30.glGenRenderbuffers();
        GLResourceManager.addDisposable(this);
    }

    public static void bind(int rboID) {
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboID);
    }

    public void bind() {
        bind(rboID);
    }

    public void alloc(TextureFormat format) {
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format.internalFormat, width, height);
    }

    @Override
    public void dispose() {
        GL30.glDeleteRenderbuffers(rboID);
    }
}
