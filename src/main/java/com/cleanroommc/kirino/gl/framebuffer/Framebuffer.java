package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.exception.RuntimeGLException;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class Framebuffer extends GLDisposable {
    public final int fboID;
    private int width, height;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private final List<IFramebufferAttachment> colorAttachments = new ArrayList<>();
    private IFramebufferAttachment depthAttachment;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        fboID = GL30.glGenFramebuffers();
        GLResourceManager.addDisposable(this);
    }

    public static void bind(int fboID) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
    }

    public void bind() {
        bind(fboID);
    }

    public void check() {
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeGLException("Framebuffer incomplete: " + status);
        }
    }

    public void attach(IFramebufferAttachment attachment) {
        switch (attachment.kind()) {
            case COLOR -> {
                colorAttachments.add(attachment);
            }
            case DEPTH -> {
                depthAttachment = attachment;
            }
        }
        attachment.attach();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        for (IFramebufferAttachment attachment : colorAttachments) {
            attachment.resize(width, height);
        }
        depthAttachment.resize(width, height);
    }

    @Override
    public void dispose() {
        GL30.glDeleteFramebuffers(fboID);
    }

    @Override
    public int disposePriority() {
        return -100; // later than textures / renderbuffers
    }
}
