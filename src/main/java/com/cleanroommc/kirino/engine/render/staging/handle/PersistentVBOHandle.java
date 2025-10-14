package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;

import java.nio.ByteBuffer;

public class PersistentVBOHandle extends StagingBufferHandle {
    public PersistentVBOHandle(StagingBufferManager stagingBufferManager, int offset, int maxLength) {
        super(stagingBufferManager, offset, maxLength);
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {

    }
}
