package com.cleanroommc.kirino.ecs.component.impl;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.impl.struct.TestStruct;
import org.joml.Vector3f;

public class PositionComponent implements ICleanComponent {
    Vector3f xyz;
    TestStruct test;
}
