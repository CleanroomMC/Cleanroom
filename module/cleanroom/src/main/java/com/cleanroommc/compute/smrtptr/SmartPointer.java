package com.cleanroommc.compute.smrtptr;

import java.io.Closeable;

public abstract class SmartPointer implements Closeable {

    private short ttl;
    private final short startTTL;
    private boolean isClosed = false;

    public SmartPointer(final short startTTL) {
        this.startTTL = startTTL;
        this.ttl = startTTL;
        synchronized (GarbageCollector.INSTANCE.referenceGraph) {
            GarbageCollector.INSTANCE.referenceGraph.addNode(this);
        }
    }

    public SmartPointer() {
        this(GarbageCollector.INSTANCE.startTTL);
    }

    public final void reference(SmartPointer pointer) {
        synchronized (GarbageCollector.INSTANCE.referenceGraph) {
            GarbageCollector.INSTANCE.referenceGraph.putEdge(this, pointer);
        }
        this.ttl = startTTL;
    }

    public final void dereference(SmartPointer pointer) {
        synchronized (GarbageCollector.INSTANCE.referenceGraph) {
            GarbageCollector.INSTANCE.referenceGraph.removeEdge(this, pointer);
        }
    }

    public final void tick() {
        if (this.ttl == 0)
            this.close();
        else if (GarbageCollector.INSTANCE.referenceGraph.adjacentNodes(this).isEmpty()) // Only reading. Shouldn't cause data races
            this.ttl--;
    }

    public final short ttl() {
        return this.ttl;
    }

    public final boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public void close() {
        this.isClosed = true;
        synchronized (GarbageCollector.INSTANCE.referenceGraph) {
            GarbageCollector.INSTANCE.referenceGraph.removeNode(this);
        }
    }
}
