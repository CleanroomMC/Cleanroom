package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.shorts.ShortHeapPriorityQueue;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class Meshlet implements Comparable<Meshlet> {
    EnumFacing normal;
    // Insertion-time median tracking
    PriorityQueue<Short> maxHeap = new ShortHeapPriorityQueue((a, b) -> b - a);
    PriorityQueue<Short> minHeap = new ShortHeapPriorityQueue();
    AABB boundingBox;

    public Meshlet(EnumFacing normal, int x, int y, int z) {
        this.normal = normal;
        this.boundingBox = new AABB(x, y, z, x+1, y+1, z+1);
        addBlock(x, y, z);
    }

    public void addBlock(int x, int y, int z) {
        maxHeap.enqueue((short) ((x & 0b1111) | ((y & 0b1111) << 4) | ((z & 0b1111) << 8)));
        minHeap.enqueue(maxHeap.first());

        if (minHeap.size() > maxHeap.size()) {
            maxHeap.enqueue(minHeap.first());
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
