package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL30;

public class Framebuffer extends GLDisposable {
    public final int fboID;
    public final int width, height;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        fboID = GL30.glGenFramebuffers();
        GLResourceManager.addDisposable(this);
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
    }

    @Override
    public void dispose() {
        GL30.glDeleteFramebuffers(fboID);
    }
}
