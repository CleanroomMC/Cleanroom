package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.PriorityQueue;
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

    public void add(@NonNull List<Vector3b> points) {
        Preconditions.checkNotNull(points);

        final int PROBE_SIZE = 21;

        if (root == null) {
            root = new Node();
        }

        len += points.size();

        record Recurrence(Node node, List<Vector3b> toInsert, int depth) {}

        RandomGenerator rng = new Random(seed);

        Stack<Recurrence> recurrenceStack = new ObjectArrayList<>();
        recurrenceStack.push(new Recurrence(root, points, 0));
        while (!recurrenceStack.isEmpty()) {
            Recurrence recurrence = recurrenceStack.pop();
            final int dimension = recurrence.depth % 3;
            if (recurrence.toInsert != null) {
                if (recurrence.toInsert.size() == 1) {
                    recurrence.node.point = recurrence.toInsert.getFirst();
                    recurrence.node.left = null;
                    recurrence.node.right = null;
                } else if (recurrence.toInsert.size() > 1) {
                    List<Vector3b> left = new ObjectArrayList<>();
                    List<Vector3b> right = new ObjectArrayList<>();
                    if (recurrence.node.point == null) {
                        Vector3b[] probe;
                        if (recurrence.toInsert.size() > PROBE_SIZE) {
                            probe = new Vector3b[PROBE_SIZE];
                            for (int i = 0; i < PROBE_SIZE; i++) {
                                probe[i] = recurrence.toInsert.get(rng.nextInt(recurrence.toInsert.size()));
                            }
                        } else {
                            probe = recurrence.toInsert.toArray(new Vector3b[0]);
                        }
                        recurrence.node.point = QuantileUtils.median(probe, (a, b) -> compareMeshletDimension(a, b, dimension));
                        //recurrence.toInsert.remove(recurrence.node.meshlet);
                    }
                    for(Vector3b point : recurrence.toInsert) {
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
                    }
                    if (!right.isEmpty()) {
                        recurrence.node.right = new Node();
                    }
                    recurrenceStack.push(new Recurrence(recurrence.node.left, left, recurrence.depth + 1));
                    recurrenceStack.push(new Recurrence(recurrence.node.right, right, recurrence.depth + 1));
                }
            }
        }
    }

    public Optional<List<Vector3b>> knn(@NonNull Vector3b vector, float cutoff, int k, boolean removeEquals) {
        Preconditions.checkNotNull(vector);

        if (root == null) {
            return Optional.empty();
        }

        PriorityQueue<Vector3b> neighbours = new ObjectArrayPriorityQueue<>((a,b) -> (a.distanceSquared(vector) - b.distanceSquared(vector)));
        float cutoffSquared = cutoff*cutoff;

        Stack<Node> stack = new ReferenceArrayList<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.point == null) {
                continue;
            }
            if (node.point.distanceSquared(vector) <= cutoffSquared) {
                neighbours.enqueue(node.point);
            }

            float distanceLeft = Float.MAX_VALUE;
            float distanceRight = Float.MAX_VALUE;

            if (node.left != null && node.left.point != null) {
                distanceLeft = node.left.point.distanceSquared(vector);
            }
            if (node.right != null && node.right.point != null) {
                distanceRight = node.right.point.distanceSquared(vector);
            }

            if (distanceLeft < distanceRight || distanceLeft <= cutoff) {
                stack.push(node.left);
            }
            if (distanceRight < distanceLeft || distanceRight <= cutoff) {
                stack.push(node.right);
            }
        }

        if (neighbours.isEmpty()) {
            return Optional.empty();
        }

        List<Vector3b> meshlets = new ReferenceArrayList<>();

        while (meshlets.size() < k && !neighbours.isEmpty()) {
            Vector3b found = neighbours.dequeue();
            if (removeEquals && found.equals(vector)) {
                continue;
            }
            meshlets.add(found);
        }

        return !meshlets.isEmpty() ? Optional.of(meshlets) : Optional.empty();
    }

    // TODO: Rewrite to iterative
    private static Optional<Node> _delete(@Nullable Node from, Vector3b point, int depth) {
        if (from == null || from.point == null) {
            return Optional.empty();
        }

        int dimension = depth % 3;

        if (from.point.distanceSquared(point) == 0) {
            if (from.right != null) {
                Optional<Node> min = findMin(from.right, dimension, depth);
                if (min.isPresent()) {
                    from.point = min.get().point;
                    from.right = _delete(from.right, min.get().point, depth + 1).orElse(null);
                }
            } else if (from.left != null) {
                Optional<Node> min = findMin(from.left, dimension, depth);
                if (min.isPresent()) {
                    from.point = min.get().point;
                    from.left = _delete(from.left, min.get().point, depth + 1).orElse(null);
                }
            } else {
                from = null;
            }
            return Optional.ofNullable(from);
        }

        if (compareMeshletDimension(from.point, point, dimension) < 0) {
            from.left = _delete(from.left, point, depth + 1).orElse(null);
        } else {
            from.right = _delete(from.right, point, depth + 1).orElse(null);
        }

        return Optional.of(from);
    }

    public void delete(@NonNull Vector3b meshlet) {
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

        if (left.isPresent() && compareMeshletDimension(from.point, left.get().point, dimension) < 0) {
            min = left;
        }
        if (right.isPresent() && compareMeshletDimension(from.point, right.get().point, dimension) < 0) {
            min = right;
        }

        return min;
    }

    /**
     * Select the optimal point for building the meshlet
     * @return Meshlet of the lowest node on the left branch
     */
    public Optional<Vector3b> getLeftExtremity() {
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
    public Optional<Vector3b> getRightExtremity() {
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

    private static int compareMeshletDimension(Vector3b median, Vector3b meshlet, int dimension) {
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
        Vector3b point;
        Node left, right;

        @Override
        public String toString() {
            return "Node [position=" +
                    point +
                    ", left=" +
                    left +
                    ", right=" +
                    right +
                    "]";
        }
    }
}
