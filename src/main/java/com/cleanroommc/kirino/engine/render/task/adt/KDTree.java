package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.random.RandomGenerator;

public class KDTree {

    private Node root = null;
    private int len = 0;
    private final long seed;

    public KDTree(long seed) {
        this.seed = seed;
    }

    public void add(@NonNull List<KDTreeBlock> points) {
        Preconditions.checkNotNull(points);

        final int PROBE_SIZE = 21;

        if (root == null) {
            root = new Node();
        }

        len += points.size();

        record Recurrence(Node node, List<KDTreeBlock> toInsert, int depth) {}

        RandomGenerator rng = new Random(seed);

        Stack<Recurrence> recurrenceStack = new ObjectArrayList<>();
        recurrenceStack.push(new Recurrence(root, points, 0));
        while (!recurrenceStack.isEmpty()) {
            Recurrence recurrence = recurrenceStack.pop();
            final int dimension = recurrence.depth % 3;
            recurrence.node.axis = dimension;
            if (recurrence.toInsert != null) {
                if (recurrence.toInsert.size() == 1) {
                    recurrence.node.point = recurrence.toInsert.getFirst();
                    recurrence.node.left = null;
                    recurrence.node.right = null;
                } else if (recurrence.toInsert.size() > 1) {
                    List<KDTreeBlock> left = new ObjectArrayList<>();
                    List<KDTreeBlock> right = new ObjectArrayList<>();
                    if (recurrence.node.point == null) {
                        KDTreeBlock[] probe;
                        if (recurrence.toInsert.size() > PROBE_SIZE) {
                            probe = new KDTreeBlock[PROBE_SIZE];
                            for (int i = 0; i < PROBE_SIZE; i++) {
                                probe[i] = recurrence.toInsert.get(rng.nextInt(recurrence.toInsert.size()));
                            }
                        } else {
                            probe = recurrence.toInsert.toArray(new KDTreeBlock[0]);
                        }
                        recurrence.node.point = QuantileUtils.median(probe, (a, b) -> compareMeshletDimension(a, b, dimension));
                        //recurrence.toInsert.remove(recurrence.node.meshlet);
                    }
                    for(KDTreeBlock point : recurrence.toInsert) {
                        if (recurrence.node.point.equals(point)) {
                            continue;
                        }
                        if (compareMeshletDimension(recurrence.node.point, point, dimension) < 0) {
                            left.add(point);
                        } else {
                            right.add(point);
                        }
                    }
                    if (!left.isEmpty()) {
                        recurrence.node.left = new Node();
                        recurrenceStack.push(new Recurrence(recurrence.node.left, left, recurrence.depth + 1));
                    }
                    if (!right.isEmpty()) {
                        recurrence.node.right = new Node();
                        recurrenceStack.push(new Recurrence(recurrence.node.right, right, recurrence.depth + 1));
                    }
                }
            }
        }
    }

    public Optional<List<KDTreeBlock>> knn(@NonNull KDTreeBlock vector, float cutoff, int k, boolean removeEquals) {
        Preconditions.checkNotNull(vector);

        if (root == null) {
            return Optional.empty();
        }

        // Max-heap to discard furthest neighbors
        BoundedPriorityQueue<KDTreeBlock> neighbors = new BoundedPriorityQueue<>(ObjectArrayPriorityQueue::new,
                k, Comparator.<KDTreeBlock>comparingInt(a -> a.distanceSquared(vector)).reversed());
        float cutoffSquared = cutoff * cutoff;

        Stack<Node> stack = new ReferenceArrayList<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.point == null) {
                continue;
            }
            neighbors.enqueue(node.point);

            int axis = node.axis;
            Node near, far;
            float diff = compareMeshletDimension(node.point, vector, axis);
            if (diff < 0) {
                near = node.left;
                far = node.right;
            }
            else {
                near = node.right;
                far = node.left;
            }

            // The subtree that contains the target is always checked
            if (near != null) {
                stack.push(near);
            }

            float furthestDist = cutoffSquared;
            if (neighbors.remainingCapacity() > 0) {
                furthestDist = Math.min(neighbors.first().distanceSquared(vector), furthestDist);
            }
            // Other subtree is only checked if its splitting plane intersects the candidate sphere (around the target)
            if (diff * diff <= furthestDist && far != null) {
                stack.push(far);
            }
        }

        List<KDTreeBlock> meshlets = new ReferenceArrayList<>(neighbors.size());
        while (!neighbors.isEmpty()) {
            KDTreeBlock neighbor = neighbors.dequeue();
            if (removeEquals && neighbor.equals(vector)) {
                continue;
            }
            meshlets.add(neighbor);
        }
        return !meshlets.isEmpty() ? Optional.of(meshlets) : Optional.empty();
    }

    // TODO: Rewrite to iterative
    private static Optional<Node> _delete(@Nullable Node from, KDTreeBlock point, int depth) {
        if (from == null || from.point == null) {
            return Optional.empty();
        }

        int nextDepth = (depth + 1) % 3;

        if (from.point.distanceSquared(point) == 0) {
            if (from.right != null) {
                Optional<Node> min = findMin(from.right, depth, nextDepth);
                if (min.isPresent()) {
                    from.point = min.get().point;
                    from.right = _delete(from.right, min.get().point, nextDepth).orElse(null);
                }
            } else if (from.left != null) {
                Optional<Node> min = findMin(from.left, depth, nextDepth);
                if (min.isPresent()) {
                    from.point = min.get().point;
                    // Swap the subtrees
                    from.right = _delete(from.left, min.get().point, nextDepth).orElse(null);
                    from.left = null;
                }
            } else {
                from = null;
            }
        }
        else if (compareMeshletDimension(from.point, point, depth) < 0) {
            from.left = _delete(from.left, point, nextDepth).orElse(null);
        } else {
            from.right = _delete(from.right, point, nextDepth).orElse(null);
        }

        return Optional.ofNullable(from);
    }

    public void delete(@NonNull KDTreeBlock meshlet) {
        _delete(root, meshlet, 0);
        len--;
    }

    // TODO: Rewrite to iterative
    private static @NonNull Optional<Node> findMin(@Nullable Node from, int dimension, int depth) {
        if (from == null) {
            return Optional.empty();
        }

        int nextDepth = (depth + 1) % 3;

        if (depth == dimension) {
            if (from.left == null) {
                return Optional.of(from);
            }
            return findMin(from.left, dimension, nextDepth);
        }

        Optional<Node> left = findMin(from.left, dimension, nextDepth);
        Optional<Node> right = findMin(from.right, dimension, nextDepth);

        Optional<Node> min = Optional.of(from);

        if (left.isPresent() && compareMeshletDimension(min.get().point, left.get().point, dimension) < 0) {
            min = left;
        }
        if (right.isPresent() && compareMeshletDimension(min.get().point, right.get().point, dimension) < 0) {
            min = right;
        }

        return min;
    }

    /**
     * Select the optimal point for building the meshlet
     * @return Meshlet of the lowest node on the left branch
     */
    public Optional<KDTreeBlock> getLeftExtremity() {
        if (root == null) {
            return Optional.empty();
        }

        Node curr = root;
        while ((curr.left != null && curr.left.point != null)
                || ((curr.right != null && curr.right.point != null)
                && (curr.right.left != null && curr.right.left.point != null))) {
            if (curr.left == null) {
                curr = curr.right.left;
            } else {
                curr = curr.left;
            }
        }

        return Optional.of(curr.point);
    }

    /**
     * Select the optimal point for building the meshlet
     * @return Meshlet of the lowest node on the right branch
     */
    public Optional<KDTreeBlock> getRightExtremity() {
        if (root == null) {
            return Optional.empty();
        }

        Node curr = root;
        while ((curr.right != null && curr.right.point != null)
                || ((curr.left != null && curr.left.point != null)
                && (curr.left.right != null && curr.left.right.point != null))) {
            if (curr.right == null) {
                curr = curr.left.right;
            } else {
                curr = curr.right;
            }
        }

        return Optional.ofNullable(curr.point);
    }

    private static int compareMeshletDimension(KDTreeBlock median, KDTreeBlock meshlet, int dimension) {
        return switch(dimension) {
            case 0 -> meshlet.x - median.x;
            case 1 -> meshlet.y - median.y;
            default -> meshlet.z - median.z;
        };
    }

    public int size() {
        return len;
    }

    @Override
    public String toString() {
        return this.root.toString();
    }

    private static class Node {
        KDTreeBlock point;
        Node left, right;
        int axis;

        @Override
        public String toString() {
            return "Node [position=" +
                    point +
                    ", left=" +
                    left +
                    ", right=" +
                    right +
                    ", axis=" +
                    "]";
        }
    }
}
