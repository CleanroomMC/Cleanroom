package com.cleanroommc.kirino.engine.render.staging;

import com.cleanroommc.kirino.engine.render.staging.handle.*;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.EBOView;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.VBOView;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Triple;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StagingBufferManager {
    // todo: ring buffer double buffering
    private final Map<String, Triple<Integer, VBOView, ByteBuffer>> persistentVbos = new HashMap<>();
    private final Map<String, Triple<Integer, EBOView, ByteBuffer>> persistentEbos = new HashMap<>();

    private final List<VAO> temporaryVaos = new ArrayList<>();
    private final List<VBOView> temporaryVbos = new ArrayList<>();
    private final List<EBOView> temporaryEbos = new ArrayList<>();
    private long temporaryHandleGeneration = 0;

    public long getTemporaryHandleGeneration() {
        return temporaryHandleGeneration;
    }

    protected boolean active = false;

    private void beginStaging() {
        temporaryHandleGeneration++;
        for (VAO vao : temporaryVaos) {
            GLResourceManager.disposeEarly(vao);
        }
        temporaryVaos.clear();
        for (VBOView vboView : temporaryVbos) {
            GLResourceManager.disposeEarly(vboView.buffer);
        }
        temporaryVbos.clear();
        for (EBOView eboView : temporaryEbos) {
            GLResourceManager.disposeEarly(eboView.buffer);
        }
        temporaryEbos.clear();
        active = true;
    }

    private void finishStaging() {
        active = false;
    }

    private final StagingContext stagingContext = new StagingContext();

    public void runStaging(List<StagingCallback> callbacks) {
        beginStaging();
        stagingContext.manager = this;
        for (StagingCallback callback : callbacks) {
            callback.run(stagingContext);
        }
        finishStaging();
        flushPersistent();
    }

    // todo
    public void flushPersistent() {
        //GL30.glFlushMappedBufferRange();
    }

    public void genPersistentBuffers(String key, int vboSize, int eboSize) {
        Preconditions.checkArgument(!persistentVbos.containsKey(key), "The key already exists.");

        VBOView vboView = new VBOView(new GLBuffer());
        EBOView eboView = new EBOView(new GLBuffer());

        // todo: x2 to do double buffering
        vboView.bind();
        vboView.allocPersistent(vboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT); // explicit flush bit is invalid???
        vboView.mapPersistent(0, vboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        vboView.bind(0);

        eboView.bind();
        eboView.allocPersistent(eboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        eboView.mapPersistent(0, eboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        eboView.bind(0);

        persistentVbos.put(key, Triple.of(vboSize, vboView, vboView.getPersistentMappedBuffer()));
        persistentEbos.put(key, Triple.of(eboSize, eboView, eboView.getPersistentMappedBuffer()));
    }

    protected PersistentVBOHandle getPersistentVBOHandle(String key, int offset, int size) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        Triple<Integer, VBOView, ByteBuffer> entry = persistentVbos.get(key);
        Preconditions.checkArgument(entry != null, "Argument key is invalid.");
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
        Preconditions.checkArgument(offset + size <= entry.getLeft(), "Buffer slice size must be greater than or equal to offset + size.");

        return new PersistentVBOHandle(this, offset, size, entry.getRight());
    }

    protected PersistentEBOHandle getPersistentEBOHandle(String key, int offset, int size) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        Triple<Integer, EBOView, ByteBuffer> entry = persistentEbos.get(key);
        Preconditions.checkArgument(entry != null, "Argument key is invalid.");
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
        Preconditions.checkArgument(offset + size <= entry.getLeft(), "Buffer slice size must be greater than or equal to offset + size.");

        return new PersistentEBOHandle(this, offset, size, entry.getRight());
    }

    protected TemporaryVAOHandle getTemporaryVAOHandle(AttributeLayout attributeLayout, TemporaryEBOHandle eboHandle, TemporaryVBOHandle... vboHandles) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(eboHandle.generation == temporaryHandleGeneration, "The temporary EBO handle is expired.");
        for (TemporaryVBOHandle vboHandle : vboHandles) {
            Preconditions.checkArgument(vboHandle.generation == temporaryHandleGeneration, "The temporary VBO handle is expired.");
        }

        TemporaryVAOHandle vaoHandle = new TemporaryVAOHandle(this, temporaryHandleGeneration, attributeLayout, eboHandle, vboHandles);
        temporaryVaos.add(vaoHandle.getVao()); // todo: don't expose vao object
        return vaoHandle;
    }

    protected TemporaryVBOHandle getTemporaryVBOHandle(int size) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");

        VBOView vboView = new VBOView(new GLBuffer());
        vboView.turnOffValidation();
        vboView.bind();
        vboView.alloc(size, BufferUploadHint.STATIC_DRAW);
        vboView.bind(0);
        temporaryVbos.add(vboView);
        return new TemporaryVBOHandle(this, temporaryHandleGeneration, size, vboView);
    }

    protected TemporaryEBOHandle getTemporaryEBOHandle(int size) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");

        EBOView eboView = new EBOView(new GLBuffer());
        eboView.turnOffValidation();
        eboView.bind();
        eboView.alloc(size, BufferUploadHint.STATIC_DRAW);
        eboView.bind(0);
        temporaryEbos.add(eboView);
        return new TemporaryEBOHandle(this, temporaryHandleGeneration, size, eboView);
    }
}
