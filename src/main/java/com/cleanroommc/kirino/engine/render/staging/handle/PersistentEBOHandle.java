package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class PersistentEBOHandle extends StagingBufferHandle<PersistentEBOHandle> {
    private final ByteBuffer byteBuffer;

    public PersistentEBOHandle(StagingBufferManager stagingBufferManager, int offset, int maxLength, ByteBuffer byteBuffer) {
        super(stagingBufferManager, offset, maxLength);
        this.byteBuffer = byteBuffer;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength,
                "Buffer slice size must be greater than or equal to \"offset + byteBuffer.remaining()\".");

        int oldPos = this.byteBuffer.position();
        this.byteBuffer.position(this.offset + offset);
        this.byteBuffer.put(byteBuffer);
        this.byteBuffer.position(oldPos);
    }
}
