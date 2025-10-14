package com.cleanroommc.kirino.engine.render.staging;

import com.cleanroommc.kirino.engine.render.staging.handle.PersistentEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.PersistentVBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVBOHandle;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.EBOView;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.VBOView;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StagingBufferManager {
    // todo: ring buffer double buffering
    private final Map<String, Pair<VBOView, ByteBuffer>> persistentVbos = new HashMap<>();
    private final Map<String, Pair<EBOView, ByteBuffer>> persistentEbos = new HashMap<>();

    private final List<VBOView> temporaryVbos = new ArrayList<>();
    private final List<EBOView> temporaryEbos = new ArrayList<>();
    private long temporaryHandleGeneration = 0;

    public long getTemporaryHandleGeneration() {
        return temporaryHandleGeneration;
    }

    protected boolean active = false;

    private void beginStaging() {
        temporaryHandleGeneration++;
        for (VBOView vboView : temporaryVbos) {
            GLResourceManager.disposeEarly(vboView.buffer);
        }
        for (EBOView eboView : temporaryEbos) {
            GLResourceManager.disposeEarly(eboView.buffer);
        }
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
    }

    public void genPersistentBuffers(String key, int vboSize, int eboSize) {
        Preconditions.checkArgument(!persistentVbos.containsKey(key), "The key already exists.");

        VBOView vboView = new VBOView(new GLBuffer());
        EBOView eboView = new EBOView(new GLBuffer());

        vboView.bind();
        vboView.allocPersistent(vboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.FLUSH_EXPLICIT_BIT);
        vboView.mapPersistent(0, vboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.FLUSH_EXPLICIT_BIT);
        vboView.bind(0);

        eboView.bind();
        eboView.allocPersistent(eboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.FLUSH_EXPLICIT_BIT);
        eboView.mapPersistent(0, eboSize * 2, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.FLUSH_EXPLICIT_BIT);
        eboView.bind(0);

        persistentVbos.put(key, Pair.of(vboView, vboView.getPersistentMappedBuffer()));
        persistentEbos.put(key, Pair.of(eboView, eboView.getPersistentMappedBuffer()));
    }

    // todo
    public void flushPersistent() {

    }

    // todo
    protected PersistentVBOHandle getPersistentVBOHandle(String key) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        return null;
    }

    // todo
    protected PersistentEBOHandle getPersistentEBOHandle(String key) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        return null;
    }

    protected TemporaryVBOHandle getTemporaryVBOHandle(int size) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        VBOView vboView = new VBOView(new GLBuffer());
        vboView.turnOffValidation();
        vboView.bind();
        vboView.alloc(size, BufferUploadHint.STATIC_DRAW);
        vboView.bind(0);
        temporaryVbos.add(vboView);
        return new TemporaryVBOHandle(this, temporaryHandleGeneration, 0, size, vboView);
    }

    protected TemporaryEBOHandle getTemporaryEBOHandle(int size) {
        Preconditions.checkState(active, "Must not access buffers from StagingBufferManager when the manager is inactive.");

        EBOView eboView = new EBOView(new GLBuffer());
        eboView.turnOffValidation();
        eboView.bind();
        eboView.alloc(size, BufferUploadHint.STATIC_DRAW);
        eboView.bind(0);
        temporaryEbos.add(eboView);
        return new TemporaryEBOHandle(this, temporaryHandleGeneration, 0, size, eboView);
    }
}
