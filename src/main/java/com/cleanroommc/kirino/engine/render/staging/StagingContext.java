package com.cleanroommc.kirino.engine.render.staging;

import com.cleanroommc.kirino.engine.render.staging.handle.PersistentEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.PersistentVBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVBOHandle;

public class StagingContext {
    protected StagingBufferManager manager;

    public PersistentVBOHandle getPersistentVBO(String key) {
        return manager.getPersistentVBOHandle(key);
    }

    public PersistentEBOHandle getPersistentEBO(String key) {
        return manager.getPersistentEBOHandle(key);
    }

    public TemporaryVBOHandle getTemporaryVBO(int size) {
        return manager.getTemporaryVBOHandle(size);
    }

    public TemporaryEBOHandle getTemporaryEBO(int size) {
        return manager.getTemporaryEBOHandle(size);
    }
}
