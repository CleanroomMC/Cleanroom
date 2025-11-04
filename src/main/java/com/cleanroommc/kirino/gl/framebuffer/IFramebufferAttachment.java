package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;

public interface IFramebufferAttachment {
    /**
     * @implSpec Don't bind framebuffer; simply attach.
     */
    void attach();

    /**
     * @return Returns {@link AttachmentKind#COLOR} or {@link AttachmentKind#DEPTH}.
     */
    AttachmentKind kind();

    /**
     * @implSpec Bind the internal object and resize, and then bind to 0.
     */
    void resize(int width, int height);
}
