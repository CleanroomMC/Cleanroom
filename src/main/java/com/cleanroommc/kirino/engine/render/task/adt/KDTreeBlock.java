package com.cleanroommc.kirino.engine.render.task.adt;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class KDTreeBlock implements Comparable<KDTreeBlock> {
    public byte x, y, z;
    public byte faces;

    public KDTreeBlock(int x, int y, int z, int faces) {
        this.x = (byte) (x % 16);
        this.y = (byte) (y % 16);
        this.z = (byte) (z % 16);
        this.faces = (byte) (faces & 0b111111);
    }

    public int lengthSquared() {
        return x * x + y * y + z * z;
    }

    public int distanceSquared(KDTreeBlock vector) {
        int dx = x - vector.x;
        int dy = y - vector.y;
        int dz = z - vector.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public int distanceSquared(Vector3i vector) {
        int dx = x - vector.x;
        int dy = y - vector.y;
        int dz = z - vector.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public int compareTo(@NotNull KDTreeBlock o) {
        return this.lengthSquared() - o.lengthSquared();
    }

    @Override
    public String toString() {
        return "Vector3b{" + "x=" + (int)x + ", y=" + (int)y + ", z=" + (int)z + '}';
    }
}
