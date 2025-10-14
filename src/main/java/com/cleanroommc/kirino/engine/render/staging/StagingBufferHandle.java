package com.cleanroommc.kirino.engine.render.staging;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public abstract class StagingBufferHandle {
    protected final StagingBufferManager stagingBufferManager;
    protected final int offset;
    protected final int maxLength;

    protected StagingBufferHandle(StagingBufferManager stagingBufferManager, int offset, int maxLength) {
        this.stagingBufferManager = stagingBufferManager;
        this.offset = offset;
        this.maxLength = maxLength;
    }

    public final void write(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkState(stagingBufferManager.active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        writeInternal(offset, byteBuffer);
    }

    protected abstract void writeInternal(int offset, ByteBuffer byteBuffer);
}
