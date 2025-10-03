package com.cleanroommc.kirino.gl.buffer;

public class SegmentedVBOView extends VBOView{
    public SegmentedVBOView(GLBuffer buffer) {
        super(buffer);
        refreshSize();
    }

    private int currentSize;

    public int getCurrentSize() {
        return currentSize;
    }

    public void refreshSize() {
        currentSize = fetchBufferSize();
    }
}
