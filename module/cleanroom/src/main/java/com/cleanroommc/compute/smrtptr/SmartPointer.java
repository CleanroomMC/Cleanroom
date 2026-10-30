package com.cleanroommc.compute.smrtptr;

import com.cleanroommc.compute.cmd.CommandQueue;

import java.io.Closeable;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Smart Pointer class. Used to track references to OpenCL objects.
 * Should not be relied on, closing manually is preferable.
 * @see GarbageCollector
 */
public abstract class SmartPointer implements Closeable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * @return Take a wild damn guess.
     */
    public final boolean isClosed() {
        return this.isClosed.getAcquire();
    }

    /**
     * Closes the smart pointer.
     * @implSpec ALWAYS IMPLEMENT THIS, IT MUST RELEASE OPENCL OBJECTS. IT HAS TO USE {@link Cleaner.Cleanable#clean()}.
     */
    @Override
    public void close() {
        this.isClosed.compareAndExchangeRelease(false, true);
    }
}
