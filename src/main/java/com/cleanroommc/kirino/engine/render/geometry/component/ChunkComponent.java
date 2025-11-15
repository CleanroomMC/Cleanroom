package com.cleanroommc.kirino.engine.render.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;

@CleanComponent
public class ChunkComponent implements ICleanComponent {
    public int chunkPosX;
    public int chunkPosY;
    public int chunkPosZ;
    public boolean isDirty = true;
    public int priority = 0; // [0, inf], 0 is the highest priority
}
