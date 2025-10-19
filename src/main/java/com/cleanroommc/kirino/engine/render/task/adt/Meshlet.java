package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.shorts.ShortHeapPriorityQueue;
import net.minecraft.util.EnumFacing;

public class Meshlet {
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

    public float median() {
        if (maxHeap.size() > minHeap.size()) {
            return maxHeap.first();
        } else {
            return (maxHeap.size() + minHeap.size()) / 2.0f;
        }
    }
}
