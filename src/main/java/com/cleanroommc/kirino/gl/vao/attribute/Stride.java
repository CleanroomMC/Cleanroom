package com.cleanroommc.kirino.gl.vao.attribute;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class Stride {
    protected final Deque<Slot> slotStack = new ArrayDeque<>();
    private final int size;
    private int cumulativeSize = 0;

    public int getSize() {
        return size;
    }

    public int getCumulativeSize() {
        return cumulativeSize;
    }

    public Stride(int size) {
        Preconditions.checkArgument(size >= 0, "Stride size cannot be less than 0.");

        this.size = size;
    }

    public Stride push(Slot slot) {
        Preconditions.checkArgument(cumulativeSize + slot.getSize() <= size,
                "The maximum stride size is %d and you (%d) are exceeding it.", size, cumulativeSize + slot.getSize());

        cumulativeSize += slot.getSize();
        slotStack.push(slot);
        return this;
    }

    @Nullable
    public Slot pop() {
        if (slotStack.peek() == null) {
            return null;
        }
        Slot slot = slotStack.pop();
        cumulativeSize -= slot.getSize();
        return slot;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(size);
        result = 31 * result + Integer.hashCode(cumulativeSize);
        result = 31 * result + Arrays.hashCode(slotStack.toArray());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Stride other = (Stride) obj;
        return size == other.size &&
                cumulativeSize == other.cumulativeSize &&
                Arrays.equals(slotStack.toArray(), other.slotStack.toArray());
    }
}
