package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.FieldManager;
import com.cleanroommc.kirino.ecs.component.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.field.struct.StructManager;

public class CleanECSRuntime {
    public final StructManager structManager;
    public final FieldManager fieldManager;

    public CleanECSRuntime() {
        structManager = new StructManager();

        // todo: read config and register structs

        fieldManager = new FieldManager(structManager);

        // todo: read config and register fields

        // hard coded fields
        fieldManager.registerField("int", new FieldDef(ScalarType.INT));
        fieldManager.registerField("float", new FieldDef(ScalarType.FLOAT));
        fieldManager.registerField("bool", new FieldDef(ScalarType.BOOL));
        fieldManager.registerField("vec2", new FieldDef(ScalarType.VEC2));
        fieldManager.registerField("vec3", new FieldDef(ScalarType.VEC3));
        fieldManager.registerField("vec4", new FieldDef(ScalarType.VEC4));
        fieldManager.registerField("mat3", new FieldDef(ScalarType.MAT3));
        fieldManager.registerField("mat4", new FieldDef(ScalarType.MAT4));
    }
}
