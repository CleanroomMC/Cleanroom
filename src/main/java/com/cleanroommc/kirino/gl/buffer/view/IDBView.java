package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL40;

public class IDBView extends BufferView {
    public IDBView(GLBuffer buffer) {
        super(buffer);
    }

    @Override
    public int target() {
        return GL40.GL_DRAW_INDIRECT_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL40.GL_DRAW_INDIRECT_BUFFER_BINDING;
    }
}
