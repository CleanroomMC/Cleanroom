package com.cleanroommc.compute.smrtptr;

import java.lang.ref.Cleaner;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Automated Garbage Collection for OpenCL objects.
 */
public enum GarbageCollector {
    INSTANCE;

    public final Cleaner cleaner = Cleaner.create();
    final Queue<Runnable> deletionQueue = new ConcurrentLinkedQueue<>();

    /**
     * Remove all objects marked for deletion.
     */
    public void deleteAllSweptObjects() {
        Runnable task;
        while ((task = deletionQueue.poll()) != null)
            task.run();
    }

    /**
     * Creates a runnable that will push the provided runnable to the finalizer queue.
     * @param runnable The runnable that deletes.
     * @return The enqueueing operation.
     */
    public Runnable deletionTask(Runnable runnable) {
        return () -> deletionQueue.add(runnable);
    }
}

