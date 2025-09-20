package com.cleanroommc.kirino.engine.render.geometry;

import com.cleanroommc.kirino.ecs.component.scan.CleanStruct;

@CleanStruct
public class AABB {
    public float xMin;
    public float yMin;
    public float zMin;
    public float xMax;
    public float yMax;
    public float zMax;

    public AABB() {
        xMin = 0;
        yMin = 0;
        zMin = 0;
        xMax = 0;
        yMax = 0;
        zMax = 0;
    }

    public AABB(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;
    }
}
