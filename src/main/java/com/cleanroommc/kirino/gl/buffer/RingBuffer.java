package com.cleanroommc.kirino.gl.buffer;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class RingBuffer {
    private final ByteBuffer byteBuffer;
    public final int sliceSize;
    public final int sliceNumber;
    private int writeSliceIndex = 0;
    private int readSliceIndex = 0;
    private boolean finishedFirstCycle = false;

    public boolean isFinishedFirstCycle() {
        return finishedFirstCycle;
    }

    public int getWriteSliceIndex() {
        return writeSliceIndex;
    }

    public int getReadSliceIndex() {
        return readSliceIndex;
    }

    public int getWriteOffset() {
        return writeSliceIndex * sliceSize;
    }

    public int getReadOffset() {
        return readSliceIndex * sliceSize;
    }

    public RingBuffer(ByteBuffer byteBuffer, int sliceSize, int sliceNumber) {
        Preconditions.checkArgument(sliceSize > 0, "Must use non-zero positive \"sliceSize\".");
        Preconditions.checkArgument(sliceNumber > 1, "Must use greater-than-one \"sliceNumber\".");
        Preconditions.checkArgument(byteBuffer.capacity() == sliceSize * sliceNumber,
                "The capacity of the \"byteBuffer\" must equal \"sliceSize\" * \"sliceNumber\".");

        this.byteBuffer = byteBuffer;
        this.sliceSize = sliceSize;
        this.sliceNumber = sliceNumber;
    }

    public void write(ByteBuffer byteBuffer) {
        Preconditions.checkArgument(byteBuffer.remaining() == sliceSize,
                "The remaining of the \"byteBuffer\" must equal \"sliceSize\".");

        this.byteBuffer.position(getWriteOffset());
        this.byteBuffer.put(byteBuffer);
        this.byteBuffer.limit(getWriteOffset() + sliceSize);

        writeSliceIndex++;
        if (!finishedFirstCycle && writeSliceIndex >= sliceNumber - 1) {
            finishedFirstCycle = true;
            readSliceIndex = 0;
        }
        if (writeSliceIndex >= sliceNumber) {
            writeSliceIndex = 0;
        }
        if (finishedFirstCycle) {
            readSliceIndex++;
            if (readSliceIndex >= sliceNumber) {
                readSliceIndex = 0;
            }
        }
    }
}
