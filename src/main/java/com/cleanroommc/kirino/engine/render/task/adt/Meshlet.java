package com.cleanroommc.kirino.engine.render.task.data;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import net.minecraft.util.EnumFacing;

public class Meshlet {
    EnumFacing normal;
    short[] blocks = new short[32];
    int blockCount = 0;
    AABB boundingBox = new AABB();

    public Meshlet(EnumFacing normal) {
        this.normal = normal;
    }

}
