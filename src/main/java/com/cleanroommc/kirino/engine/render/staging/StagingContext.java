package com.cleanroommc.kirino.engine.render.staging;

import com.cleanroommc.kirino.engine.render.staging.handle.*;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;

public class StagingContext {
    protected StagingBufferManager manager;

    public PersistentVBOHandle getPersistentVBO(String key, int offset, int size) {
        return manager.getPersistentVBOHandle(key, offset, size);
    }

    public PersistentEBOHandle getPersistentEBO(String key, int offset, int size) {
        return manager.getPersistentEBOHandle(key, offset, size);
    }

    public TemporaryVAOHandle getTemporaryVAO(AttributeLayout attributeLayout, TemporaryEBOHandle eboHandle, TemporaryVBOHandle... vboHandles) {
        return manager.getTemporaryVAOHandle(attributeLayout, eboHandle, vboHandles);
    }

    public TemporaryVBOHandle getTemporaryVBO(int size) {
        return manager.getTemporaryVBOHandle(size);
    }

    public TemporaryEBOHandle getTemporaryEBO(int size) {
        return manager.getTemporaryEBOHandle(size);
    }
}
