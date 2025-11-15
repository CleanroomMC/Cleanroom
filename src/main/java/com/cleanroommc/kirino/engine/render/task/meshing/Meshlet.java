package com.cleanroommc.kirino.engine.render.task.meshing;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.utils.QuantileUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.List;

public class Meshlet implements Comparable<Meshlet> {
    EnumFacing normal;
    List<Block> blocks = new LinkedList<>();
    Block median;
    boolean isDirty;
    int len = 0;
    AABB boundingBox;
    boolean transparent;

    public Meshlet(EnumFacing normal, int x, int y, int z, int faces, boolean transparent) {
        this.normal = normal;
        this.boundingBox = new AABB(x, y, z, x+1, y+1, z+1);
        this.transparent = transparent;
        addBlock(new Block(x, y, z, faces));
        median = blocks.getFirst();
        isDirty = false;
    }

    public int size() {
        return len;
    }

    public AABB aabb() {
        return boundingBox;
    }

    public boolean transparent() {
        return transparent;
    }

    // Empties the blocks onto the set
    public List<Block> blockList() {
        return blocks;
    }

    public Vector3f normal() {
        return new Vector3f(normal.getXOffset(), normal.getYOffset(), normal.getZOffset());
    }

    public void addBlock(Block block) {
        isDirty = true;
        blocks.add(block);
        len++;

        if (block.position.x < boundingBox.xMin) {
            boundingBox.xMin = block.position.x;
        }
        if (block.position.x + 1 > boundingBox.xMax) {
            boundingBox.xMax = block.position.x + 1;
        }
        if (block.position.y < boundingBox.yMin) {
            boundingBox.yMin = block.position.y;
        }
        if (block.position.y + 1 > boundingBox.yMax) {
            boundingBox.yMax = block.position.y + 1;
        }
        if (block.position.z < boundingBox.zMin) {
            boundingBox.zMin = block.position.z;
        }
        if (block.position.z + 1 > boundingBox.zMax) {
            boundingBox.zMax = block.position.z + 1;
        }
    }

    public void merge(@NonNull Meshlet m) {
        Preconditions.checkNotNull(m);
        Preconditions.checkState(!this.equals(m), "Recurrent addition");

        for (int i = 0; i < m.blocks.size(); i++) {
            addBlock(m.blocks.get(i));
        }
    }

    public Block median() {
        if (isDirty) {
            median = QuantileUtils.median(blocks.toArray(new Block[0]), (a, b) -> (int) (a.position.lengthSquared() - b.position.lengthSquared()));
            isDirty = false;
        }
        return median;
    }

    @Override
    public int compareTo(@NotNull Meshlet o) {
        return (int) (median().position.lengthSquared()-o.median().position.lengthSquared());
    }

    @Override
    public String toString() {
        return "Meshlet [" +
                "normal=" + normal +
                ", median=" + median +
                ", encodedBlocks=" + blocks +
                ", size=" + len +
                ", transparent=" + transparent +
                "]";
    }
}
