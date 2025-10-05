package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;
import com.cleanroommc.kirino.gl.texture.Texture2DView;
import org.lwjgl.opengl.GL30;

public class DepthStencilAttachment implements IFramebufferAttachment{
    private final Texture2DView texture2D;
    private final GLRenderBuffer renderBuffer;
    private final boolean isTexture;

    public DepthStencilAttachment(Texture2DView texture2D) {
        this.texture2D = texture2D;
        renderBuffer = null;
        isTexture = true;
    }

    public DepthStencilAttachment(GLRenderBuffer renderBuffer) {
        texture2D = null;
        this.renderBuffer = renderBuffer;
        isTexture = false;
    }

    @Override
    public void attach() {
        if (isTexture) {
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, texture2D.target(), texture2D.texture.textureID, 0);
        } else {
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, renderBuffer.rboID);
        }
    }

    @Override
    public AttachmentKind kind() {
        return AttachmentKind.DEPTH;
    }

    @Override
    public void resize(int width, int height) {
        if (isTexture) {
            texture2D.resizeAndAllocNull(width, height);
        } else {
            renderBuffer.resize(width, height);
        }
    }
}
