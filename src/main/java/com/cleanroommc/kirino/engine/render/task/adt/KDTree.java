package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.*;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class KDTree {

    private Node root = null;
    private int len = 0;

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
                if (compareMeshletDimension(recurrence.node.meshlet, meshlet, dimension)) {
                    left.add(meshlet);
                } else {
                    right.add(meshlet);
                }
            }
            recurrenceStack.push(new Recurrence(recurrence.node.left, left, recurrence.depth + 1));
            recurrenceStack.push(new Recurrence(recurrence.node.right, right, recurrence.depth + 1));
        }

        len++;
    }

    public Optional<List<Meshlet>> knn(@NonNull Meshlet meshlet, float cutoff, int k) {
        Preconditions.checkNotNull(meshlet);

        return this.knn(meshlet.median(), cutoff, k);
    }

    public Optional<List<Meshlet>> knn(@NonNull Vector3f vector, float cutoff, int k) {
        Preconditions.checkNotNull(vector);

        if (root == null) {
            return Optional.empty();
        }

        PriorityQueue<Meshlet> neighbours = new ObjectArrayPriorityQueue<>((a,b) -> (int) (a.median().distanceSquared(vector) - b.median().distanceSquared(vector)));
        float cutoffSquared = cutoff*cutoff;

        Stack<Node> stack = new ReferenceArrayList<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.meshlet == null) {
                continue;
            }
            if (node.meshlet.median().distanceSquared(vector) <= cutoffSquared) {
                neighbours.enqueue(node.meshlet);
            }

            float distanceLeft = Float.MAX_VALUE;
            float distanceRight = Float.MAX_VALUE;

            if (node.left != null) {
                distanceLeft = node.left.meshlet.median().distanceSquared(vector);
            }
            if (node.right != null) {
                distanceRight = node.right.meshlet.median().distanceSquared(vector);
            }

            if (distanceLeft < distanceRight || distanceLeft <= cutoff) {
                stack.push(node.left);
            }
            if (distanceRight < distanceLeft || distanceRight <= cutoff) {
                stack.push(node.right);
            }
        }

        List<Meshlet> meshlets = new ReferenceArrayList<>();

        for (int i = 0; i < k; i++) {
            meshlets.add(neighbours.dequeue());
        }

        return Optional.of(meshlets);
    }

    public Optional<Meshlet> nearest(@NonNull Vector3f vector, float cutoff, boolean excludeEquals) {
        Preconditions.checkNotNull(vector);

        if (excludeEquals) {
            Optional<List<Meshlet>> neighbours = knn(vector, cutoff, 2);
            if (neighbours.isPresent()) {
                if (!neighbours.get().isEmpty()) {
                    if (neighbours.get().getFirst().median().distanceSquared(vector) != 0) {
                        return Optional.of(neighbours.get().getFirst());
                    } else {
                        return Optional.of(neighbours.get().getLast());
                    }
                }
            }
        } else {
            Optional<List<Meshlet>> neighbours = knn(vector, cutoff, 1);
            if (neighbours.isPresent()) {
                if (!neighbours.get().isEmpty()) {
                    return Optional.of(neighbours.get().getFirst());
                }
            }
        }
        return Optional.empty();
    }

    // TODO: Rewrite to iterative
    private static Optional<Node> _delete(@Nullable Node from, Meshlet point, int depth) {
        if (from == null) {
            return Optional.empty();
        }

        int dimension = depth % 3;

        if (from.meshlet.median().distanceSquared(point.median()) == 0) {
            if (from.right != null) {
                Optional<Node> min = findMin(from.right, dimension, depth);
                if (min.isPresent()) {
                    from.meshlet = min.get().meshlet;
                    from.right = _delete(from.right, min.get().meshlet, depth + 1).orElse(null);
                }
            } else if (from.left != null) {
                Optional<Node> min = findMin(from.left, dimension, depth);
                if (min.isPresent()) {
                    from.meshlet = min.get().meshlet;
                    from.left = _delete(from.left, min.get().meshlet, depth + 1).orElse(null);
                }
            } else {
                from = null;
            }
            return Optional.ofNullable(from);
        }

        if (compareMeshletDimension(from.meshlet, point, dimension)) {
            from.left = _delete(from.left, point, depth + 1).orElse(null);
        } else {
            from.right = _delete(from.right, point, depth + 1).orElse(null);
        }

        return Optional.of(from);
    }

    public void delete(@NonNull Meshlet meshlet) {
        _delete(root, meshlet, 0);
        len--;
    }

    // TODO: Rewrite to iterative
    private static @NonNull Optional<Node> findMin(@Nullable Node from, int dimension, int depth) {
        if (from == null) {
            return Optional.empty();
        }

        int cd = depth % 3;

        if (cd == dimension) {
            if (from.left == null) {
                return Optional.of(from);
            }
            return findMin(from.left, dimension, depth + 1);
        }

        Optional<Node> left = findMin(from.left, dimension, depth + 1);
        Optional<Node> right = findMin(from.right, dimension, depth + 1);

        Optional<Node> min = Optional.of(from);

        if (left.isPresent() && compareMeshletDimension(from.meshlet, left.get().meshlet, dimension)) {
            min = left;
        }
        if (right.isPresent() && compareMeshletDimension(from.meshlet, right.get().meshlet, dimension)) {
            min = right;
        }

        return min;
    }

    /**
     * Select the optimal point for building the meshlet
     * @return Meshlet of the lowest node on the left branch
     */
    public Optional<Meshlet> getLeftExtremity() {
        if (root == null) {
            return Optional.empty();
        }

        Node curr = root;
        while (curr.left != null || (curr.right != null && curr.right.left != null)) {
            if (curr.left == null) {
                curr = curr.right.left;
            } else {
                curr = curr.left;
            }
        }

        return Optional.of(curr.meshlet);
    }

    /**
     * Select the optimal point for building the meshlet
     * @return Meshlet of the lowest node on the right branch
     */
    public Optional<Meshlet> getRightExtremity() {
        if (root == null) {
            return Optional.empty();
        }

        Node curr = root;
        while (curr.right != null || (curr.left != null && curr.left.right != null)) {
            if (curr.right == null) {
                curr = curr.left.right;
            } else {
                curr = curr.right;
            }
        }

        return Optional.of(curr.meshlet);
    }

    private static boolean compareMeshletDimension(Meshlet median, Meshlet meshlet, int dimension) {
        return switch(dimension) {
            case 0 -> meshlet.median().x < median.median().x;
            case 1 -> meshlet.median().y < median.median().y;
            default -> meshlet.median().z < median.median().z;
        };
    }

    public int size() {
        return len;
    }

    private static class Node {
        Meshlet meshlet;
        Node left, right;
    }
}
