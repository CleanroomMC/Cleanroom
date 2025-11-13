package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

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
    private IFramebufferAttachment depthAttachment = null;

    @NonNull
    public IFramebufferAttachment getColorAttachment(int index) {
        Preconditions.checkPositionIndex(index, colorAttachments.size());

        return colorAttachments.get(index);
    }

    @Nullable
    public IFramebufferAttachment getDepthAttachment() {
        return depthAttachment;
    }

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

    private static String getStatusString(int status) {
        return switch (status) {
            case GL30.GL_FRAMEBUFFER_COMPLETE -> "COMPLETE";
            case GL30.GL_FRAMEBUFFER_UNDEFINED -> "UNDEFINED";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "INCOMPLETE_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "MISSING_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "INCOMPLETE_DRAW_BUFFER";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "INCOMPLETE_READ_BUFFER";
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED -> "UNSUPPORTED";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "INCOMPLETE_MULTISAMPLE";
            case GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "INCOMPLETE_LAYER_TARGETS";
            default -> "Unknown status: " + status;
        };
    }

    public void check() {
        int statusDraw = GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER);
        if (statusDraw != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer DRAW incomplete: " + getStatusString(statusDraw));
        }

        int statusRead = GL30.glCheckFramebufferStatus(GL30.GL_READ_FRAMEBUFFER);
        if (statusRead != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer READ incomplete: " + getStatusString(statusRead));
        }
    }

    // bind fbo first
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

    // no need to bind fbo
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        for (IFramebufferAttachment attachment : colorAttachments) {
            if (attachment != null) {
                attachment.resize(width, height);
            }
        }
        if (depthAttachment != null) {
            depthAttachment.resize(width, height);
        }
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
