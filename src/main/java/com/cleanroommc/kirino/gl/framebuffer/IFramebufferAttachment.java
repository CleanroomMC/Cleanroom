package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;

public interface IFramebufferAttachment {
    void attach();
    AttachmentKind kind();
    void resize(int width, int height);
}
