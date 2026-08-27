package com.cleanroommc.compute.smrtptr;

import com.cleanroommc.compute.cmd.CommandQueue;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Smart Pointer class. Used to track references to OpenCL objects.
 * Should not be relied on, closing manually is preferable.
 * @see GarbageCollector
 */
public abstract class SmartPointer implements Closeable {

    private short ttl;
    private final short startTTL;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Lock readLock = lock.readLock();
    protected final Lock writeLock = lock.writeLock();

    /**
     * Initialises the smart pointer.
     * @param startTTL Time to live that the pointer will reset to every refresh.
     */
    public SmartPointer(final short startTTL) {
        this.startTTL = startTTL;
        this.ttl = startTTL;
        GarbageCollector.INSTANCE.add(this);
    }

    /**
     * Initialises the smart pointer with a time to live taken form configuration.
     */
    public SmartPointer() {
        this(GarbageCollector.INSTANCE.startTTL);
    }

    /**
     * Connects this smart pointer and another one with a reference.
     * @param pointer The pointer that references or is being referenced by this.
     */
    protected final void reference(SmartPointer pointer) {
        GarbageCollector.INSTANCE.reference(this, pointer);
        try {
            writeLock.lock();
            this.ttl = startTTL;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a reference from this pointer to another.
     * @param pointer The pointer that is no longer referenced by this.
     */
    protected final void dereference(SmartPointer pointer) {
        GarbageCollector.INSTANCE.dereference(this, pointer);
    }

    /**
     * Decrements the time to live of this pointer if it has no references.
     * If TTL is 0 then {@link #close()} is called.
     * @apiNote {@link CommandQueue}s are special, their TTL is always decreased to
     * prevent unused queues from clogging up memory.
     */
    public final void tick() {
        try {
            writeLock.lock();
            if (this.ttl == 0)
                this.close();
            else if (GarbageCollector.INSTANCE.references(this).isEmpty() || this instanceof CommandQueue) // Only reading. Shouldn't cause data races
                this.ttl--;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Reset TTL to it's {@link #startTTL initial value}.
     * @implSpec THIS SHOLD ONLY BE CALLED FROM {@link CommandQueue.Event#execute()}
     */
    protected final void refresh() {
        try {
            writeLock.lock();
            this.ttl = startTTL;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @return Time to live of this Smart Pointer.
     */
    public final short ttl() {
        try {
            readLock.lock();
            return this.ttl;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return Take a wild damn guess.
     */
    public final boolean isClosed() {
        return this.isClosed.getAcquire();
    }

    /**
     * Closes the smart pointer.
     * @implSpec ALWAYS IMPLEMENT THIS, IT MUST RELEASE OPENCL OBJECTS.
     */
    @Override
    public void close() {
        this.isClosed.compareAndExchangeRelease(false, true);
        GarbageCollector.INSTANCE.remove(this);
    }
}
