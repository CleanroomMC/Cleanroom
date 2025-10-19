package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class Octree {
    private final AABB boundingBox;
    private final List<Node> children = new LinkedList<>();
    Node root;

    public Octree(AABB boundingBox) {
        this.boundingBox = boundingBox;
        root = new Node(boundingBox);
    }

    public void insert(@NonNull Block insertedBlock) {
        Preconditions.checkNotNull(insertedBlock);
        Preconditions.checkArgument(inside(insertedBlock.position, boundingBox));

        final int maxInLeaf = 32;

        Deque<Block> blocksToInsert = new ArrayDeque<>();
        blocksToInsert.add(insertedBlock);
        while (!blocksToInsert.isEmpty()) {
            Block block = blocksToInsert.pop();
            Deque<Node> stack = new ArrayDeque<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                Node node = stack.pop();

                if (!inside(block.position, node.boundingBox)) {
                    continue;
                }

                if (node.blocks.size() < maxInLeaf) {
                    node.blocks.add(block);
                }

                float halfMaxX = node.boundingBox.xMax/2;
                float halfMaxY = node.boundingBox.yMax/2;
                float halfMaxZ = node.boundingBox.zMax/2;

                AABB newAABB1 = new AABB(node.boundingBox.xMin,
                        node.boundingBox.yMin,
                        node.boundingBox.zMin,
                        halfMaxX,halfMaxY,halfMaxZ); // Bottom Layer lower left corner
                AABB newAABB2 = new AABB(halfMaxX, node.boundingBox.yMin,
                        node.boundingBox.zMin,
                        node.boundingBox.xMax, halfMaxY, halfMaxZ); // Bottom Layer lower right corner
                AABB newAABB3 = new AABB(node.boundingBox.xMin,
                        node.boundingBox.yMin, halfMaxZ,
                        node.boundingBox.xMax, halfMaxY, node.boundingBox.zMax); // Bottom Layer upper left corner
                AABB newAABB4 = new AABB(halfMaxX, node.boundingBox.yMin,
                        halfMaxZ, node.boundingBox.xMax, halfMaxY, node.boundingBox.zMax); // Bottom Layer upper right corner
                AABB newAABB5 = new AABB(node.boundingBox.xMin,
                        halfMaxY,
                        node.boundingBox.zMin,
                        halfMaxX,node.boundingBox.yMax,halfMaxZ); // Top Layer lower left corner
                AABB newAABB6 = new AABB(halfMaxX, halfMaxY,
                        node.boundingBox.zMin,
                        node.boundingBox.xMax, node.boundingBox.yMax, halfMaxZ); // Top Layer lower right corner
                AABB newAABB7 = new AABB(node.boundingBox.xMin,
                        halfMaxY, halfMaxZ,
                        node.boundingBox.xMax, node.boundingBox.yMax,
                        node.boundingBox.zMax); // Top Layer upper left corner
                AABB newAABB8 = new AABB(halfMaxX, halfMaxY,
                        halfMaxZ, node.boundingBox.xMax,
                        node.boundingBox.yMax, node.boundingBox.zMax); // Top Layer upper right corner

                blocksToInsert.addAll(node.blocks);
                node.blocks = null;
                children.remove(node);

                node.subdivision[0] = new Node(newAABB1);
                stack.push(node.subdivision[0]);
                node.subdivision[1] = new Node(newAABB2);
                stack.push(node.subdivision[1]);
                node.subdivision[2] = new Node(newAABB3);
                stack.push(node.subdivision[2]);
                node.subdivision[3] = new Node(newAABB4);
                stack.push(node.subdivision[3]);
                node.subdivision[4] = new Node(newAABB5);
                stack.push(node.subdivision[4]);
                node.subdivision[5] = new Node(newAABB6);
                stack.push(node.subdivision[5]);
                node.subdivision[6] = new Node(newAABB7);
                stack.push(node.subdivision[6]);
                node.subdivision[7] = new Node(newAABB8);
                stack.push(node.subdivision[7]);
            }
        }
    }

    public Block[][] children() {
        Block[][] result = new Block[children.size()][];
        for (int i = 0; i < children.size(); i++) {
            result[i] = children.get(i).blocks.toArray(new Block[0]);
        }
        return result;
    }

    private static boolean inside(@NonNull Vector3i pos, @NonNull AABB boundingBox) {
        return     pos.x >= boundingBox.xMin
                && pos.x <= boundingBox.xMax
                && pos.z >= boundingBox.zMin
                && pos.z <= boundingBox.zMax
                && pos.y >= boundingBox.yMin
                && pos.y <= boundingBox.yMax;
    }

    private class Node {
        final AABB boundingBox;
        List<Block> blocks;
        Node[] subdivision;

        Node(AABB boundingBox) {
            this.boundingBox = boundingBox;
            blocks = new ArrayList<>();
            subdivision = null;
            children.add(this);
        }
    }
}
