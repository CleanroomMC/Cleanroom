package com.cleanroommc.kirino.mcbridge.ecs.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;
import org.joml.Vector3f;

@CleanComponent
public class PositionComponent implements ICleanComponent {
    public Vector3f xyz;
}
