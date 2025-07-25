package com.cleanroommc.kirino.gl.buffer.meta;

import org.lwjgl.opengl.GL15;

public enum BufferUploadHint {
    STATIC_DRAW(GL15.GL_STATIC_DRAW),
    DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
    STREAM_DRAW(GL15.GL_STREAM_DRAW);

    public final int glValue;

    BufferUploadHint(int glValue) {
        this.glValue = glValue;
    }
}
