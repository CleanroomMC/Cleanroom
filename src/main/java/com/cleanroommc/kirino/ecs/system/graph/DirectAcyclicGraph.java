package com.cleanroommc.kirino.ecs.system.graph;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.Traverser;

@SuppressWarnings("UnstableApiUsage")
public class DirectAcyclicGraph {
    final MutableNetwork<Node, Edge> graph;

    public DirectAcyclicGraph() {
        this.graph = NetworkBuilder.directed()
                .allowsSelfLoops(false)
                .allowsParallelEdges(true)
                .build();
    }
    public void addEdge(Node from, Node to, Edge edge) {
        Traverser.forGraph(graph.asGraph()).breadthFirst(to).forEach((node) -> {
            if (node == from) {
                throw new IllegalArgumentException("Connecting node" + from + "to" + to + "create a cycle!");
            }
        });
        graph.addEdge(from, to, edge);
    }

    public void addNode(Node node) {
        graph.addNode(node);
    }

}
