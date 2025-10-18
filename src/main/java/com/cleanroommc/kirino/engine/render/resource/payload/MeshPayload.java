package com.cleanroommc.kirino.engine.render.resource.payload;

import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class MeshPayload implements IResourcePayload<MeshPayload> {
    public MeshPayload() {
    }

    /**
     * Access via reflection.
     *
     * @see GraphicResourceManager#MESH_PAYLOAD_SETTER
     */
    private ByteBuffer byteBuffer;

    @NonNull
    @Override
    public MeshPayload getPayload() {
        return this;
    }
}
