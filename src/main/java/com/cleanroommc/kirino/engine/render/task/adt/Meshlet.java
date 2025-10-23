package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortHeapPriorityQueue;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

public class Meshlet implements Comparable<Meshlet> {
    EnumFacing normal;
    // Insertion-time median tracking
    PriorityQueue<Short> maxHeap = new ShortHeapPriorityQueue((a, b) -> b - a);
    PriorityQueue<Short> minHeap = new ShortHeapPriorityQueue();
    int blocks = 0;
    AABB boundingBox;

    public Meshlet(EnumFacing normal, int x, int y, int z) {
        this.normal = normal;
        this.boundingBox = new AABB(x, y, z, x+1, y+1, z+1);
        addBlock(x, y, z);
    }

    public int size() {
        return blocks;
    }

    public AABB aabb() {
        return boundingBox;
    }

    // Empties the blocks onto the set
    public Set<Block> emptyBlocks() {
        Set<Block> blocks = new ObjectOpenHashSet<>();

        while (!maxHeap.isEmpty()) {
            short block = maxHeap.dequeue();
            blocks.add(new Block(block & MASK_X, (block & MASK_Y) >> 4, (block & MASK_Z) >> 8));
        }

        return blocks;
    }

    public Vector3f normal() {
        return new Vector3f(normal.getXOffset(), normal.getYOffset(), normal.getZOffset());
    }

    public void addBlock(short block) {
        addBlock(block & MASK_X, (block & MASK_Y) >> 4, (block & MASK_Z) >> 8);
    }

    public void addBlock(int x, int y, int z) {
        maxHeap.enqueue((short) ((x & 0b1111) | ((y & 0b1111) << 4) | ((z & 0b1111) << 8)));
        minHeap.enqueue(maxHeap.first());
        blocks++;

        if (minHeap.size() > maxHeap.size()) {
            maxHeap.enqueue(minHeap.first());
        }

        if (x < boundingBox.xMin) {
            boundingBox.xMin = x;
        }
        if (x + 1 > boundingBox.xMax) {
            boundingBox.xMax = x + 1;
        }
        if (y < boundingBox.yMin) {
            boundingBox.yMin = y;
        }
        if (y + 1 > boundingBox.yMax) {
            boundingBox.yMax = y + 1;
        }
        if (z < boundingBox.zMin) {
            boundingBox.zMin = z;
        }
        if (z + 1 > boundingBox.zMax) {
            boundingBox.zMax = z + 1;
        }
    }

    public void merge(@NonNull Meshlet m) {
        Preconditions.checkNotNull(m);

        Set<Short> toAdd = new HashSet<>();

        while(!m.maxHeap.isEmpty()) {
            toAdd.add(m.maxHeap.dequeue());
        }

        for (short block : toAdd) {
            addBlock(block);
        }
    }

    public Vector3f median() {
        if (maxHeap.size() > minHeap.size()) {
            int x = maxHeap.first() & MASK_X;
            int y = (maxHeap.first() & MASK_Y) >> 4;
            int z = (maxHeap.first() & MASK_Z) >> 8;
            return new Vector3f((float) x, (float) y, (float) z);
        } else {
            int x = maxHeap.first() & MASK_X;
            int y = (maxHeap.first() & MASK_Y) >> 4;
            int z = (maxHeap.first() & MASK_Z) >> 8;
            Vector3f max = new Vector3f((float) x, (float) y, (float) z);
            x = minHeap.first() & MASK_X;
            y = (minHeap.first() & MASK_Y) >> 4;
            z = (minHeap.first() & MASK_Z) >> 8;
            Vector3f min = new Vector3f((float) x, (float) y, (float) z);
            return max.add(min).div(2.f);
        }
    }

    @Override
    public int compareTo(@NotNull Meshlet o) {
        return (int) (median().lengthSquared()-o.median().lengthSquared());
    }

    private static final short MASK_X = 0b000000001111;
    private static final short MASK_Y = 0b000011110000;
    private static final short MASK_Z = 0b111100000000;
}
