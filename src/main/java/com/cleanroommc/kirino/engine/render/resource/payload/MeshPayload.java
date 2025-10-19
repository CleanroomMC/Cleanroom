package com.cleanroommc.kirino.engine.render.resource.payload;

import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import org.jspecify.annotations.NonNull;

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
}
