package com.cleanroommc.kirino.engine.render.geometry;

import com.cleanroommc.kirino.ecs.component.scan.CleanStruct;
import org.joml.Vector3i;

import java.util.BitSet;

@CleanStruct
public class Block {
    public Vector3i position;
    public int faces;

    public Block() {
        position = new Vector3i();
        faces = 0b111111;
    }

    public Block(int x, int y, int z) {
        this(x,y,z,0b111111);
    }

    public Block(int x, int y, int z, int faces) {
        position = new Vector3i(x, y, z);
        this.faces = faces;
    }

    int compress() {
        return (position.x & 0b1111) << 14
                | (position.y & 0b1111) << 10
                | (position.z & 0b1111) << 6
                | faces;
    }
}
