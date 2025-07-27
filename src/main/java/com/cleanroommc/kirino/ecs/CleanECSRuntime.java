package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.KirinoRendering;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.component.impl.PositionComponent;
import com.cleanroommc.kirino.ecs.component.impl.struct.TestStruct;
import org.joml.*;

public class CleanECSRuntime {
    public final StructRegistry structRegistry;
    public final FieldRegistry fieldRegistry;

    public final ComponentRegistry componentRegistry;

    public CleanECSRuntime() {
        structRegistry = new StructRegistry();

        // todo: scan classes and register structs

        // todo: remove struct demo
        structRegistry.registerStructType("Test", TestStruct.class, new StructDef(new FieldDef(ScalarType.INT)));

        fieldRegistry = new FieldRegistry(structRegistry);

        // todo: register fields based on structs (field acts like a wrapper of struct)

        // todo: remove field demo
        fieldRegistry.registerFieldType("Test", TestStruct.class, new FieldDef("Test"));

        // hard coded fields
        fieldRegistry.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fieldRegistry.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        fieldRegistry.registerFieldType("bool", boolean.class, new FieldDef(ScalarType.BOOL));
        fieldRegistry.registerFieldType("vec2", Vector2f.class, new FieldDef(ScalarType.VEC2));
        fieldRegistry.registerFieldType("vec3", Vector3f.class, new FieldDef(ScalarType.VEC3));
        fieldRegistry.registerFieldType("vec4", Vector4f.class, new FieldDef(ScalarType.VEC4));
        fieldRegistry.registerFieldType("mat3", Matrix3f.class, new FieldDef(ScalarType.MAT3));
        fieldRegistry.registerFieldType("mat4", Matrix4f.class, new FieldDef(ScalarType.MAT4));

        componentRegistry = new ComponentRegistry(fieldRegistry);

        // todo: scan classes and register components

        componentRegistry.register("Position", PositionComponent.class, "vec3", "Test");

        KirinoRendering.LOGGER.info("debug: " + componentRegistry.serializeComponentDesc(componentRegistry.getComponentDesc("Position")));
        KirinoRendering.LOGGER.info("debug: " + componentRegistry.getComponentDescRuntime("Position").toString());
    }
}
