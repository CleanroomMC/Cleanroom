package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;

public class GLRenderBuffer extends GLDisposable {
    public final int rboID;
    private int width;
    private int height;
    private TextureFormat currentFormat = null;

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
        currentFormat = format;
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format.internalFormat, width, height);
    }

    public void alloc() {
        alloc(TextureFormat.RGBA8_UNORM);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        if (currentFormat == null) {
            alloc();
        } else {
            alloc(currentFormat);
        }
    }

    public void resize(int width, int height, TextureFormat format) {
        this.width = width;
        this.height = height;
        alloc(format);
    }

    @Override
    public void dispose() {
        GL30.glDeleteRenderbuffers(rboID);
    }
}
