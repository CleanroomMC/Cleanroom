package com.cleanroommc.kirino.engine.render.task.data;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import kotlin.random.Random;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class KDTree {

    private static final int MAX_BLOCKS_IN_LEAF = 32;
    private static final int DIMENSIONS = 3;

    private Node root = null;
    private final Set<Node> leaves = new ReferenceLinkedOpenHashSet<>(); // Linked since there will be a lot of deletions
    private final AABB bounds;

    public KDTree(AABB bounds) {
        this.bounds = bounds;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void insert(@NonNull Block block) {
        Preconditions.checkNotNull(block);

        record BlockInsertion(Block block, Node start, int depth) {}
        record Level(Node node, int depth) {}

        if (root == null) {
            root = new Node(bounds);
            root.addBlock(block);
            return;
        }

        Deque<BlockInsertion> blockStack = new ArrayDeque<>();
        blockStack.add(new BlockInsertion(block, root, 0));
        while (!blockStack.isEmpty()) {
            BlockInsertion call = blockStack.pop();
            Block next = call.block;

            Deque<Level> recurrenceStack = new ArrayDeque<>();
            recurrenceStack.add(new Level(call.start, call.depth));
            while (!recurrenceStack.isEmpty()) {
                Level level = recurrenceStack.pop();

                int dimension = level.depth % DIMENSIONS;

                if (level.node.blockCount >= MAX_BLOCKS_IN_LEAF) {
                    this.leaves.remove(level.node);

                    Integer[] probe = new Integer[16];
                    for (int i = 0; i < 16; i++) {
                        probe[i] = getAxis(level.node.blocks[Random.Default.nextInt(MAX_BLOCKS_IN_LEAF)], dimension);
                    }

                    level.node.pivot = QuantileUtils.median(probe);

                    for (Block b : level.node.blocks) {
                        blockStack.add(new BlockInsertion(b, level.node, level.depth));
                    }

                    level.node.blocks = null;
                    level.node.blockCount = -1;

                    level.node.left = new Node(
                            getBoundingBoxForDepth(level.node.boundingBox, level.node.pivot, dimension, false)
                    );
                    level.node.right = new Node(
                            getBoundingBoxForDepth(level.node.boundingBox, level.node.pivot, dimension, true)
                    );
                } else if (level.node.blockCount == -1) {
                    if (level.node.left != null) {
                        if (inside(next.position, level.node.left.boundingBox)) {
                            recurrenceStack.add(new Level(level.node.left, level.depth+1));
                        }
                    }
                    if (level.node.right != null) {
                        if (inside(next.position, level.node.right.boundingBox)) {
                            recurrenceStack.add(new Level(level.node.right, level.depth+1));
                        }
                    }
                } else {
                    level.node.addBlock(next);
                }
            }
        }
    }



    private static boolean inside(@NonNull Vector3i pos, @NonNull AABB boundingBox) {
        return     pos.x >= boundingBox.xMin
                && pos.x <= boundingBox.xMax
                && pos.z >= boundingBox.zMin
                && pos.z <= boundingBox.zMax
                && pos.y >= boundingBox.yMin
                && pos.y <= boundingBox.yMax;
    }

    private static int getAxis(@NonNull Block block, int dimension) {
        Preconditions.checkNotNull(block);

        return switch (dimension % 3) {
            case 0 -> block.position.x;
            case 1 -> block.position.y;
            default -> block.position.z;
        };
    }

    private static @NonNull AABB getBoundingBoxForDepth(@NonNull AABB boundingBox,
                                                        int pivot, int dimension,
                                                        boolean side) {
        Preconditions.checkNotNull(boundingBox);

        if (side) {
            return switch (dimension % 3) {
                case 0 -> new AABB(
                        pivot,
                        boundingBox.yMin,
                        boundingBox.zMin,
                        boundingBox.xMax,
                        boundingBox.yMax,
                        boundingBox.zMax
                );
                case 1 -> new AABB(
                        boundingBox.xMin,
                        pivot,
                        boundingBox.zMin,
                        boundingBox.xMax,
                        boundingBox.yMax,
                        boundingBox.zMax
                );
                default -> new AABB(
                        boundingBox.xMin,
                        boundingBox.yMin,
                        pivot,
                        boundingBox.xMax,
                        boundingBox.yMax,
                        boundingBox.zMax
                );
            };
        } else {
            return switch (dimension % 3) {
                case 0 -> new AABB(
                        boundingBox.xMin,
                        boundingBox.yMin,
                        boundingBox.zMin,
                        pivot,
                        boundingBox.yMax,
                        boundingBox.zMax
                );
                case 1 -> new AABB(
                        boundingBox.xMin,
                        boundingBox.yMin,
                        boundingBox.zMin,
                        boundingBox.xMax,
                        pivot,
                        boundingBox.zMax
                );
                default -> new AABB(
                        boundingBox.xMin,
                        boundingBox.yMin,
                        boundingBox.zMin,
                        boundingBox.xMax,
                        boundingBox.yMax,
                        pivot
                );
            };
        }
    }

    private class Node {
        Block[] blocks;
        int blockCount;

        int pivot; // -1 means the node was not split
        AABB boundingBox;

        Node left = null, right = null;

        Node(@NonNull AABB boundingBox) {
            Preconditions.checkNotNull(boundingBox);

            pivot = -1;
            this.boundingBox = boundingBox;
            blocks = new Block[MAX_BLOCKS_IN_LEAF];
            blockCount = 0;
            leaves.add(this);
        }

        void addBlock(@NonNull Block block) {
            Preconditions.checkNotNull(block);
            Preconditions.checkArgument(blockCount < 32);

            blocks[blockCount] = block;
            blockCount++;
        }
    }
}
