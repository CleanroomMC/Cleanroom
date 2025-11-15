package com.cleanroommc.kirino.engine.render.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;
import com.cleanroommc.kirino.engine.render.geometry.AABB;
import org.joml.Vector3f;

@CleanComponent
public class MeshletComponent implements ICleanComponent {
    // todo
    public AABB aabb;
    public Vector3f normal;
    public boolean transparent;
    public int meshletID;
    public int generation;
}
