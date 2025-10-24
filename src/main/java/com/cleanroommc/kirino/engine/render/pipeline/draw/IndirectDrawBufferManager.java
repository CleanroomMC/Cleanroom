package com.cleanroommc.kirino.engine.render.pipeline.draw;

import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.RingBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.IDBView;

import java.util.List;

public class IndirectDrawBufferManager {
    private final IDBView idbView;
    private final RingBuffer ringBuffer;
    public final int bufferSize;

    public int getIdbID() {
        return idbView.bufferID;
    }

    public boolean isFinishedFirstCycle() {
        return ringBuffer.isFinishedFirstCycle();
    }

    public int getReadOffset() {
        return ringBuffer.getReadOffset();
    }

    public IndirectDrawBufferManager(int bufferSize, int bufferingCount) {
        this.bufferSize = bufferSize;
        this.idbView = new IDBView(new GLBuffer());
        idbView.bind();
        idbView.allocPersistent(bufferSize * bufferingCount, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        idbView.mapPersistent(0, bufferSize * bufferingCount, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        idbView.bind(0);
        this.ringBuffer = new RingBuffer(idbView.getPersistentMappedBuffer(), bufferSize, bufferingCount);
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li><code>units</code> are all <code>MULTI_ELEMENTS_INDIRECT_UNIT</code> typed commands</li>
     * </ul>
     *
     * @param units Low-level <code>MULTI_ELEMENTS_INDIRECT_UNIT</code> typed commands
     */
    public void write(List<LowLevelDC> units) {

    }
}
