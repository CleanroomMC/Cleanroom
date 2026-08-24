package com.cleanroommc.compute.smrtptr;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Set;

/**
 * Automated Garbage Collection for OpenCL objects.
 */
public enum GarbageCollector {
    INSTANCE;

    public final short startTTL = 16; // TODO: Pull from config
    private final MutableGraph<SmartPointer> referenceGraph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    private final Object lock = new Object();

    /**
     * Adds a pointer to reference tracking.
     * @param pointer the pointer
     * @see MutableGraph#addNode(Object)
     */
    void add(SmartPointer pointer) {
        if (referenceGraph.nodes().contains(pointer))
            return;
        synchronized (lock) {
            this.referenceGraph.addNode(pointer);
        }
    }

    /**
     * Removes a pointer from reference tracking.
     * @param pointer the pointer
     * @see MutableGraph#removeNode(Object)
     */
    void remove(SmartPointer pointer) {
        if (!referenceGraph.nodes().contains(pointer))
            return;
        synchronized (lock) {
            this.referenceGraph.removeNode(pointer);
        }
    }

    /**
     * Creates a reference between two pointers.
     * @param from pointer 1
     * @param to pointer 2
     * @see MutableGraph#putEdge(Object, Object)
     */
    void reference(SmartPointer from, SmartPointer to) {
        synchronized (lock) {
            this.referenceGraph.putEdge(from, to);
        }
    }

    /**
     * Removes a reference between two pointers.
     * @param from pointer 1
     * @param to pointer 2
     * @see MutableGraph#removeEdge(Object, Object)
     */
    void dereference(SmartPointer from, SmartPointer to) {
        synchronized (lock) {
            this.referenceGraph.removeEdge(from, to);
        }
    }

    /**
     * Returns all pointers that reference the given pointer.
     * @param pointer the pointer
     * @return referencing pointers
     * @see com.google.common.graph.Graph#adjacentNodes(Object)
     */
    Set<SmartPointer> references(SmartPointer pointer) {
        return referenceGraph.adjacentNodes(pointer);
    }

    /**
     * Garbage collection.
     * Decreases TTL of all command queues and unreferenced objects, then closes them if they expired.
     * @see SmartPointer#tick()
     */
    public void sweep() {
        synchronized (lock) {
            referenceGraph.nodes().forEach(SmartPointer::tick);
        }
    }

    /**
     * Free all memory.
     * @apiNote This should <u>only</u> be called when the server, internal or external, is shutting down.
     * @see SmartPointer#close()
     */
    public void wash() {
        synchronized (lock) {
            referenceGraph.nodes().forEach(SmartPointer::close);
        }
    }
}
