package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TemporaryVAOHandle extends StagingBufferHandle<TemporaryVAOHandle> {
    public final long generation;

    /**
     * Access via reflection.
     *
     * @see StagingBufferManager#TEMPORARY_VAO_HANDLE_VAO_GETTER
     */
    private final VAO vao;

    public TemporaryVAOHandle(StagingBufferManager stagingBufferManager, long generation, AttributeLayout attributeLayout, TemporaryEBOHandle eboHandle, TemporaryVBOHandle... vboHandles) {
        super(stagingBufferManager, 0, 0);
        this.generation = generation;
        vao = new VAO(attributeLayout, eboHandle.eboView, Arrays.stream(vboHandles).map(handle -> handle.vboView).toArray(VBOView[]::new));
    }

    public int getVaoID() {
        Preconditions.checkState(generation == stagingBufferManager.getTemporaryHandleGeneration(), "This temporary handle is expired.");

        return vao.vaoID;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        throw new RuntimeException("VAO handle doesn't support write.");
    }
}
