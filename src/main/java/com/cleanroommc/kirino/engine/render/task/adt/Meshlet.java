package com.cleanroommc.kirino.engine.render.task.adt;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.List;

public class Meshlet implements Comparable<Meshlet> {
    EnumFacing normal;
    List<Vector3f> encodedBlocks = new LinkedList<>();
    Vector3f median;
    boolean isDirty;
    int blocks = 0;
    AABB boundingBox;
    boolean transparent;

    public Meshlet(EnumFacing normal, int x, int y, int z, boolean transparent) {
        this.normal = normal;
        this.boundingBox = new AABB(x, y, z, x+1, y+1, z+1);
        this.transparent = transparent;
        addBlock(new Vector3f(x, y, z));
        median = encodedBlocks.getFirst();
        isDirty = false;
    }

    public int size() {
        return blocks;
    }

    public AABB aabb() {
        return boundingBox;
    }

    public boolean transparent() {
        return transparent;
    }

    // Empties the blocks onto the set
    public List<Block> blockList() {
        List<Block> blocks = new ObjectArrayList<>();

        for (Vector3f block : encodedBlocks) {
            blocks.add(new Block((int) block.x, (int) block.y, (int) block.z));
        }

        return blocks;
    }

    public Vector3f normal() {
        return new Vector3f(normal.getXOffset(), normal.getYOffset(), normal.getZOffset());
    }

    public void addBlock(Vector3f block) {
        isDirty = true;
        encodedBlocks.add(block);
        blocks++;

        if (block.x < boundingBox.xMin) {
            boundingBox.xMin = block.x;
        }
        if (block.x + 1 > boundingBox.xMax) {
            boundingBox.xMax = block.x + 1;
        }
        if (block.y < boundingBox.yMin) {
            boundingBox.yMin = block.y;
        }
        if (block.y + 1 > boundingBox.yMax) {
            boundingBox.yMax = block.y + 1;
        }
        if (block.z < boundingBox.zMin) {
            boundingBox.zMin = block.z;
        }
        if (block.z + 1 > boundingBox.zMax) {
            boundingBox.zMax = block.z + 1;
        }
    }

    public void merge(@NonNull Meshlet m) {
        Preconditions.checkNotNull(m);
        Preconditions.checkState(!this.equals(m), "Recurrent addition");

        for (int i = 0; i < m.encodedBlocks.size(); i++) {
            addBlock(m.encodedBlocks.get(i));
        }
    }

    public Vector3f median() {
        if (isDirty) {
            median = QuantileUtils.median(encodedBlocks.toArray(new Vector3f[0]), (a, b) -> (int) (a.lengthSquared() - b.lengthSquared()));
            isDirty = false;
        }
        return median;
    }

    @Override
    public int compareTo(@NotNull Meshlet o) {
        return (int) (median().lengthSquared()-o.median().lengthSquared());
    }

    @Override
    public String toString() {
        return "Meshlet [" +
                "normal=" + normal +
                ", median=" + median +
                ", encodedBlocks=" + encodedBlocks +
                ", size=" + blocks +
                ", transparent=" + transparent +
                "]";
    }
}
