package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.VBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class TemporaryVBOHandle extends StagingBufferHandle {
    private final long generation;
    private final VBOView vboView; // turn off validation; handle preconditions manually here

    public TemporaryVBOHandle(StagingBufferManager stagingBufferManager, long generation, int offset, int maxLength, VBOView vboView) {
        super(stagingBufferManager, offset, maxLength);
        this.generation = generation;
        this.vboView = vboView;
    }

    public int getBufferID() {
        return vboView.bufferID;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkState(generation == stagingBufferManager.getTemporaryHandleGeneration(), "This temporary handle is expired.");
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength, "Allocated buffer size must be greater than or equal to offset + byteBuffer.remaining().");

        vboView.bind();
        vboView.uploadBySubData(offset, byteBuffer);
        vboView.bind(0);
    }
}
