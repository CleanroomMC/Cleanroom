package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.*;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class KDTree {

    private Node root = null;

    public void add(@NonNull List<Meshlet> meshlets) {
        Preconditions.checkNotNull(meshlets);

        if (root == null) {
            root = new Node();
        }

        record Recurrence(Node node, List<Meshlet> toInsert, int depth) {}

        Stack<Recurrence> recurrenceStack = new ObjectArrayList<>();
        recurrenceStack.push(new Recurrence(root, meshlets, 0));
        while (!recurrenceStack.isEmpty()) {
            Recurrence recurrence = recurrenceStack.pop();
            int dimension = recurrence.depth % 3;
            List<Meshlet> left = new ObjectArrayList<>();
            List<Meshlet> right = new ObjectArrayList<>();
            recurrence.node.left = new Node();
            recurrence.node.right = new Node();
            if (recurrence.node.meshlet == null) {
                recurrence.node.meshlet = QuantileUtils.median(recurrence.toInsert.toArray(new Meshlet[0]));
                recurrence.toInsert.remove(recurrence.node.meshlet);
            }
            for(Meshlet meshlet : recurrence.toInsert) {
                if (isLeft(recurrence.node.meshlet, meshlet, dimension)) {
                    left.add(meshlet);
                } else {
                    right.add(meshlet);
                }
            }
            recurrenceStack.push(new Recurrence(recurrence.node.left, left, recurrence.depth + 1));
            recurrenceStack.push(new Recurrence(recurrence.node.right, right, recurrence.depth + 1));
        }
    }

    public Optional<Set<Meshlet>> knn(@NonNull Meshlet meshlet, float cutoff, int k) {
        Preconditions.checkNotNull(meshlet);

        if (root == null) {
            return Optional.empty();
        }

        PriorityQueue<Meshlet> neighbours = new ObjectArrayPriorityQueue<>();
        float cutoffSquared = cutoff*cutoff;

        Stack<Node> stack = new ReferenceArrayList<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.meshlet == null) {
                continue;
            }
            if (node.meshlet.median().distanceSquared(meshlet.median()) <= cutoffSquared) {
                neighbours.enqueue(node.meshlet);
            }

            float distanceLeft = Float.MAX_VALUE;
            float distanceRight = Float.MAX_VALUE;

            if (node.left != null) {
                distanceLeft = node.left.meshlet.median().distanceSquared(meshlet.median());
            }
            if (node.right != null) {
                distanceRight = node.right.meshlet.median().distanceSquared(meshlet.median());
            }

            if (distanceLeft < distanceRight || distanceLeft <= cutoff) {
                stack.push(node.left);
            }
            if (distanceRight < distanceLeft || distanceRight <= cutoff) {
                stack.push(node.right);
            }
        }

        Set<Meshlet> meshlets = new ReferenceArraySet<>(k);

        for (int i = 0; i < k; i++) {
            meshlets.add(neighbours.dequeue());
        }

        return Optional.of(meshlets);
    }

    private static boolean isLeft(Meshlet median, Meshlet meshlet, int dimension) {
        return switch(dimension) {
            case 0 -> meshlet.median().x < median.median().x;
            case 1 -> meshlet.median().y < median.median().y;
            default -> meshlet.median().z < median.median().z;
        };
    }

    private static class Node {
        Meshlet meshlet;
        Node left, right;
    }
}
