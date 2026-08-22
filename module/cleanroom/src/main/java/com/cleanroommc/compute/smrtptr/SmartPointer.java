package com.cleanroommc.compute.smrtptr;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.io.Closeable;

public abstract class SmartPointer implements Closeable {

    private short ttl;
    private final short startTTL;
    private boolean isClosed = false;

    public SmartPointer(final short startTTL) {
        this.startTTL = startTTL;
        this.ttl = startTTL;
        synchronized (REFERENCES) {
            REFERENCES.addNode(this);
        }
    }

    public SmartPointer() {
        this(START_TTL);
    }

    public final void reference(SmartPointer pointer) {
        synchronized (REFERENCES) {
            REFERENCES.putEdge(this, pointer);
        }
        this.ttl = startTTL;
    }

    public final void dereference(SmartPointer pointer) {
        synchronized (REFERENCES) {
            REFERENCES.removeEdge(this, pointer);
        }
        if (this.ttl == 0)
            this.close();
        else if (REFERENCES.adjacentNodes(this).isEmpty()) // Only reading. Shouldn't cause data races
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
        synchronized (REFERENCES) {
            REFERENCES.removeNode(this);
        }
    }

    private static final short START_TTL = 16; // TODO: Pull from config
    private static final MutableGraph<SmartPointer> REFERENCES = GraphBuilder.undirected().allowsSelfLoops(false).build();
}
