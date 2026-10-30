package com.cleanroommc.compute.smrtptr;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automated Garbage Collection for OpenCL objects.
 */
public enum GarbageCollector {
    INSTANCE;

    public final Cleaner cleaner = Cleaner.create();
    final PriorityQueue<Runnable> deletionQueue = new ObjectArrayFIFOQueue<>();

    /**
     * Remove all objects marked for deletion.
     */
    public void deleteAllSweptObjects() {
        while (!deletionQueue.isEmpty())
            deletionQueue.dequeue().run();
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

