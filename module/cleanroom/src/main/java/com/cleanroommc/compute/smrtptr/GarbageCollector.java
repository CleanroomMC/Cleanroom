package com.cleanroommc.compute.smrtptr;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Automated Garbage Collection for OpenCL objects.
 */
public enum GarbageCollector {
    INSTANCE;

    public final short startTTL = 16; // TODO: Pull from config
    private final MutableGraph<SmartPointer> referenceGraph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    public final SweepTask sweepTask = new SweepTask();

    /**
     * Adds a pointer to reference tracking.
     * @param pointer the pointer
     * @see MutableGraph#addNode(Object)
     */
    void add(SmartPointer pointer) {

        try {
            writeLock.lock();
            this.referenceGraph.addNode(pointer);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a pointer from reference tracking.
     * @param pointer the pointer
     * @see MutableGraph#removeNode(Object)
     */
    void remove(SmartPointer pointer) {
        try {
            writeLock.lock();
            this.referenceGraph.removeNode(pointer);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Creates a reference between two pointers.
     * @param from pointer 1
     * @param to pointer 2
     * @see MutableGraph#putEdge(Object, Object)
     */
    void reference(SmartPointer from, SmartPointer to) {
        try {
            writeLock.lock();
            this.referenceGraph.putEdge(from, to);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a reference between two pointers.
     * @param from pointer 1
     * @param to pointer 2
     * @see MutableGraph#removeEdge(Object, Object)
     */
    void dereference(SmartPointer from, SmartPointer to) {
        try {
            writeLock.lock();
            this.referenceGraph.removeEdge(from, to);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns all pointers that reference the given pointer.
     * @param pointer the pointer
     * @return referencing pointers
     * @see com.google.common.graph.Graph#adjacentNodes(Object)
     */
    Set<SmartPointer> references(SmartPointer pointer) {
        try {
            readLock.lock();
            return referenceGraph.adjacentNodes(pointer);
        } finally {
            readLock.unlock();
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
           referenceGraph.nodes().forEach(SmartPointer::tick);
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
            referenceGraph.nodes().forEach(SmartPointer::close);
            SweepTask.running.compareAndExchangeRelease(true, false);
        } finally {
            writeLock.unlock();
        }
    }
}
