package com.cleanroommc.compute.smrtptr;

import com.google.common.base.Preconditions;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.rpmalloc.RPmalloc;
import sun.misc.Unsafe;

import java.lang.ref.Cleaner;
import java.lang.reflect.Field;
import java.nio.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Should be {@link ThreadLocal} if multithreading is to be used.
 */
public class Arena implements AutoCloseable {

    public static final int PAGE_SIZE;
    public static final int ALIGNMENT;

    private final Cleaner.Cleanable cleanable;

    static class State implements Runnable {
        final List<ByteBuffer> pages = new LinkedList<>();

        @Override
        public void run() {
            for (ByteBuffer page : pages) {
                RPmalloc.rpfree(page);
            }
        }
    }

    private final State state = new State();

    private int currPage = 0;
    private int currAddr = 0;
    private int pageCounterIdx = 0;
    private int[] pageCounter = new int[20];

    public Arena() {
        this.cleanable = Cleaner.create().register(this, state);
        this.addPage();
    }

    public ByteBuffer allocate(int size) {
        Preconditions.checkState(size <= PAGE_SIZE, "Size must be less than or equal to %s", PAGE_SIZE);
        if (currAddr + size > PAGE_SIZE)
            addPage();
        return state.pages.get(currPage).position(currAddr).limit(currAddr += size);
    }

    public ShortBuffer allocateShort(int size) {
        return allocate(size * Short.BYTES).asShortBuffer();
    }

    public IntBuffer allocateInt(int size) {
        return allocate(size * Integer.BYTES).asIntBuffer();
    }

    public LongBuffer allocateLong(int size) {
        return allocate(size * Long.BYTES).asLongBuffer();
    }

    public FloatBuffer allocateFloat(int size) {
        return allocate(size * Float.BYTES).asFloatBuffer();
    }

    public DoubleBuffer allocateDouble(int size) {
        return allocate(size * Double.BYTES).asDoubleBuffer();
    }

    public PointerBuffer allocatePointer(int size) {
        return PointerBuffer.create(allocate(size * PointerBuffer.POINTER_SIZE));
    }

    private void addPage() {
        currPage++;
        currAddr = 0;
        ByteBuffer buffer = RPmalloc.rpaligned_alloc(ALIGNMENT, PAGE_SIZE);
        Preconditions.checkNotNull(buffer, "Failed to allocate memory");
        state.pages.add(buffer);
    }

    public void reset() {
        if (pageCounterIdx == pageCounter.length) {
            shrink();
        } else {
            pageCounter[pageCounterIdx++] = state.pages.size();
        }
        currAddr = 0;
        currPage = 0;
    }

    private void shrink() {
        int mean = 0;
        for (int k : pageCounter) {
            mean += k;
        }
        mean /= 20;
        double standardDeviation = 0;
        for (int j : pageCounter) {
            standardDeviation += (j - mean) * (j - mean);
        }
        standardDeviation = Math.sqrt(standardDeviation / 20.0);
        if (standardDeviation < .3 && state.pages.size() - mean >= (state.pages.size() / 10)) {
            ListIterator<ByteBuffer> it = state.pages.listIterator(mean);
            while (it.hasNext()) {
                RPmalloc.rpfree(it.next());
                it.remove();
            }
        }
        pageCounterIdx = 0;
    }

    static {
        int pageSize = 4096;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe)f.get(null);

            pageSize = unsafe.pageSize();
        } catch (NoSuchFieldException | IllegalAccessException _) {}
        PAGE_SIZE = pageSize;
        ALIGNMENT = "64".equals(System.getProperty("sun.arch.data.model")) ? 8 : 4;
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
}
