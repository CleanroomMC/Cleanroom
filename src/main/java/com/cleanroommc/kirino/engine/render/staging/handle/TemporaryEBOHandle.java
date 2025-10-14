package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.EBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class TemporaryEBOHandle extends StagingBufferHandle {
    private final long generation;
    private final EBOView eboView;  // turn off validation; handle preconditions manually here

    public TemporaryEBOHandle(StagingBufferManager stagingBufferManager, long generation, int offset, int maxLength, EBOView eboView) {
        super(stagingBufferManager, offset, maxLength);
        this.generation = generation;
        this.eboView = eboView;
    }

    public int getBufferID() {
        return eboView.bufferID;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkState(generation == stagingBufferManager.getTemporaryHandleGeneration(), "This temporary handle is expired.");
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength, "Allocated buffer size must be greater than or equal to offset + byteBuffer.remaining().");

        eboView.bind();
        eboView.uploadBySubData(offset, byteBuffer);
        eboView.bind(0);
    }
}
