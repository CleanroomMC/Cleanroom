package com.cleanroommc.compute.smrtptr;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

public enum GarbageCollector {
    INSTANCE;

    final short startTTL = 16; // TODO: Pull from config
    final MutableGraph<SmartPointer> referenceGraph = GraphBuilder.undirected().allowsSelfLoops(false).build();

    public void sweep() {

    }
}
