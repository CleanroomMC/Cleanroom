package com.cleanroommc.compute.smrtptr;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.MutableGraph;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Automated Garbage Collection for OpenCL objects.
 */
public enum GarbageCollector {
    INSTANCE;

    public final Cleaner cleaner = Cleaner.create();
    public final short startTTL = 16; // TODO: Pull from config
    private final Set<WeakReference<SmartPointer>> objects = new ObjectArraySet<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    final Lock readLock = lock.readLock();
    final Lock writeLock = lock.writeLock();
    public final SweepTask sweepTask = new SweepTask();
    final PriorityQueue<Runnable> deletionQueue = new ObjectArrayFIFOQueue<>();
    final AtomicBoolean doneCleaning = new AtomicBoolean(false);

    /**
     * Adds a pointer to reference tracking.
     * @param pointer the pointer
     */
    void add(SmartPointer pointer) {
        try {
            writeLock.lock();
            this.objects.add(new WeakReference<>(pointer));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a pointer from reference tracking.
     * @param pointer the pointer
     */
    void remove(SmartPointer pointer) {
        try {
            writeLock.lock();
            this.objects.removeIf(ref -> ref.refersTo(pointer));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Creates a reference between two pointers.
     * @param from pointer 1
     * @param to pointer 2
     */
    void reference(SmartPointer from, SmartPointer to) {
        try {
            to.writeLock.lock();
            // Can't be null since we have the objects.
            from.references.add(new WeakReference<>(to));
            to.references.add(new WeakReference<>(from));
        } finally {
            to.writeLock.unlock();
        }
    }

    /**
     * Removes a reference between two pointers.
     * @param from pointer 1
     * @param to pointer 2
     */
    void dereference(SmartPointer from, SmartPointer to) {
        try {
            to.writeLock.lock();
            for (WeakReference<SmartPointer> ref : from.references) {
                if (ref.refersTo(to)) {
                    from.references.remove(ref);
                    break;
                }
            }
            for (WeakReference<SmartPointer> ref : to.references) {
                if (ref.refersTo(from)) {
                    to.references.remove(ref);
                    break;
                }
            }
        } finally {
            to.writeLock.unlock();
        }
    }

    /**
     * Garbage collection.
     * Decreases TTL of all command queues and unreferenced objects, then closes them if they expired.
     * @see SmartPointer#tick()
     */
    public void sweep() {
       try {
           writeLock.lock();
           this.objects.removeIf(ref -> ref.get() == null);
           this.objects.forEach(ref -> {if (ref.get() != null) ref.get().tick();});
       } finally {
           writeLock.unlock();
       }
    }

    /**
     * Free all memory.
     * @apiNote This should <u>only</u> be called when the server, internal or external, is shutting down.
     * @see SmartPointer#close()
     */
    public void wash() {
        try {
            writeLock.lock();
            SweepTask.running.compareAndExchangeRelease(true, false);
            this.objects.forEach(ref -> {if (ref.get() != null) ref.get().close();});
            deleteAllSweptObjects();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Remove all objects marked for deletion.
     */
    public void deleteAllSweptObjects() {
        if (!doneCleaning.getAcquire())
            return;
        try {
            writeLock.lock();
            SweepTask.running.compareAndExchangeRelease(true, false);
            while (!deletionQueue.isEmpty())
                deletionQueue.dequeue().run();
            doneCleaning.setRelease(false);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Creates a runnable that will push the provided runnable to the finalizer queue.
     * @param runnable The runnable that deletes.
     * @return The enqueueing operation.
     */
    public Runnable deletionTask(Runnable runnable) {
        return () -> deletionQueue.enqueue(runnable);
    }
}
