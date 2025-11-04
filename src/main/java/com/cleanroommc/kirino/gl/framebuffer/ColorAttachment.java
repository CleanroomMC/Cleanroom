package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;
import com.cleanroommc.kirino.gl.texture.Texture2DView;
import org.lwjgl.opengl.GL30;

public class ColorAttachment implements IFramebufferAttachment{
    public final int index;
    public final Texture2DView texture2D;

    public ColorAttachment(int index, Texture2DView texture2D) {
        this.index = index;
        this.texture2D = texture2D;
    }

    @Override
    public void attach() {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + index, texture2D.target(), texture2D.texture.textureID, 0);
    }

    @Override
    public AttachmentKind kind() {
        return AttachmentKind.COLOR;
    }

    @Override
    public void resize(int width, int height) {
        texture2D.bind();
        texture2D.resizeAndAllocNull(width, height);
        texture2D.bind(0);
    }
}
