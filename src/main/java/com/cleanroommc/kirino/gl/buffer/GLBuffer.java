package com.cleanroommc.kirino.gl.buffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL15;

public class GLBuffer extends GLDisposable {
    public final int bufferID;

    public GLBuffer() {
        bufferID = GL15.glGenBuffers();
        GLResourceManager.addDisposable(this);
    }

    @Override
    public void dispose() {
        GL15.glDeleteBuffers(bufferID);
    }
}
