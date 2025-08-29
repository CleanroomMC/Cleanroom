package com.cleanroommc.kirino.gl.buffer;

import org.lwjgl.opengl.GL15;

public class VBOView extends BufferView{
    public VBOView(int bufferID) {
        super(bufferID);
    }

    @Override
    public int target() {
        return GL15.GL_ARRAY_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL15.GL_ARRAY_BUFFER_BINDING;
    }
}
