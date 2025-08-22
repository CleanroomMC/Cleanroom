package com.cleanroommc.kirino.engine.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;
import com.cleanroommc.kirino.engine.geometry.AABB;

@CleanComponent
public class MeshletComponent implements ICleanComponent {
    public AABB aabb;
}
