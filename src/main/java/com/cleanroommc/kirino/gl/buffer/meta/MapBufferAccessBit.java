package com.cleanroommc.kirino.gl.buffer.meta;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

public enum MapBufferAccessBit {
    READ_BIT(GL30.GL_MAP_READ_BIT),
    WRITE_BIT(GL30.GL_MAP_WRITE_BIT),
    INVALIDATE_RANGE_BIT(GL30.GL_MAP_INVALIDATE_RANGE_BIT),
    INVALIDATE_BUFFER_BIT(GL30.GL_MAP_INVALIDATE_BUFFER_BIT),
    FLUSH_EXPLICIT_BIT(GL30.GL_MAP_FLUSH_EXPLICIT_BIT),
    UNSYNCHRONIZED_BIT(GL30.GL_MAP_UNSYNCHRONIZED_BIT),

    MAP_PERSISTENT_BIT(GL44.GL_MAP_PERSISTENT_BIT),
    MAP_COHERENT_BIT(GL44.GL_MAP_COHERENT_BIT);

    public final int glValue;

    MapBufferAccessBit(int glValue) {
        this.glValue = glValue;
    }
}
