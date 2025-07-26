package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.field.FieldManager;
import com.cleanroommc.kirino.ecs.component.field.struct.StructManager;

public class CleanECSRuntime {
    public final FieldManager fieldManager;

    public CleanECSRuntime() {
        fieldManager = new FieldManager(new StructManager());
    }
}
