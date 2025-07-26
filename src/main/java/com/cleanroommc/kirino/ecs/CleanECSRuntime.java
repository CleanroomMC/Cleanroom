package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.KirinoRendering;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.field.struct.StructRegistry;

public class CleanECSRuntime {
    public final StructRegistry structRegistry;
    public final FieldRegistry fieldRegistry;

    public final ComponentRegistry componentRegistry;

    public CleanECSRuntime() {
        structRegistry = new StructRegistry();

        // todo: read config and register structs

        fieldRegistry = new FieldRegistry(structRegistry);

        // todo: read config and register fields

        // hard coded fields
        fieldRegistry.registerField("int", new FieldDef(ScalarType.INT));
        fieldRegistry.registerField("float", new FieldDef(ScalarType.FLOAT));
        fieldRegistry.registerField("bool", new FieldDef(ScalarType.BOOL));
        fieldRegistry.registerField("vec2", new FieldDef(ScalarType.VEC2));
        fieldRegistry.registerField("vec3", new FieldDef(ScalarType.VEC3));
        fieldRegistry.registerField("vec4", new FieldDef(ScalarType.VEC4));
        fieldRegistry.registerField("mat3", new FieldDef(ScalarType.MAT3));
        fieldRegistry.registerField("mat4", new FieldDef(ScalarType.MAT4));

        componentRegistry = new ComponentRegistry(fieldRegistry);

        // todo: read config and register components

        componentRegistry.register("Position", "vec3");

        KirinoRendering.LOGGER.info("debug: " + componentRegistry.serializeComponentDesc(componentRegistry.getComponentDesc("Position")));
        KirinoRendering.LOGGER.info("debug: " + componentRegistry.getComponentDescRuntime("Position").toString());
    }
}
