package com.cleanroommc.kirino.engine.render.resource.payload;

import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class MeshPayload implements IResourcePayload<MeshPayload> {
    public MeshPayload() {
    }

    public ByteBuffer vboByteBuffer;
    public ByteBuffer eboByteBuffer;
    public AttributeLayout attributeLayout;

    @NonNull
    @Override
    public MeshPayload getPayload() {
        return this;
    }

    private boolean released = false;

    @Override
    public void release() {
        if (!released) {
            if (vboByteBuffer != null && vboByteBuffer.isDirect() && MemoryUtil.memAddressSafe(vboByteBuffer) != 0) {
                MemoryUtil.memFree(vboByteBuffer);
                vboByteBuffer = null;
            }
            if (eboByteBuffer != null && eboByteBuffer.isDirect() && MemoryUtil.memAddressSafe(eboByteBuffer) != 0) {
                MemoryUtil.memFree(eboByteBuffer);
                eboByteBuffer = null;
            }
            attributeLayout = null;
            released = true;
        }
    }
}
