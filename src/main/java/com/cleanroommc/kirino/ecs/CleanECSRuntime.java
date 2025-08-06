package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.KirinoRendering;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.mcbridge.ecs.component.TestStruct2;
import com.cleanroommc.kirino.ecs.component.scan.ComponentScanEvent;
import com.cleanroommc.kirino.ecs.component.scan.StructScanEvent;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.mcbridge.ecs.component.PositionComponent;
import com.cleanroommc.kirino.mcbridge.ecs.component.TestStruct;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.system.render.RenderSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.mcbridge.ecs.system.MinecraftRenderSystem;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.minecraftforge.common.MinecraftForge;
import org.joml.*;

public class CleanECSRuntime {
    public final StructRegistry structRegistry;
    public final FieldRegistry fieldRegistry;
    public final ComponentRegistry componentRegistry;
    public final EntityManager entityManager;
    public final CleanWorld world;
    public final RenderSystem renderSystem;

    public CleanECSRuntime() {
        structRegistry = new StructRegistry();

        StructScanEvent structScanEvent = new StructScanEvent();
        MinecraftForge.EVENT_BUS.post(structScanEvent);

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(structScanEvent.scanPackageNames.toArray(new String[0]))
                .scan()) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation("com.cleanroommc.kirino.ecs.component.scan.CleanStruct")) {
                Class<?> clazz = classInfo.loadClass();
                KirinoRendering.LOGGER.info("Found and started scanning struct class: " + clazz.getName());
                // todo: scan classes
            }
        }

        // todo: remove struct demo
        structRegistry.registerStructType(
                "Test",
                TestStruct.class,
                new MemberLayout("test"),
                new StructDef(new FieldDef(ScalarType.INT)));
        structRegistry.registerStructType(
                "Test2",
                TestStruct2.class,
                new MemberLayout("test2", "testStruct"),
                new StructDef(new FieldDef(ScalarType.INT), new FieldDef("Test")));

        fieldRegistry = new FieldRegistry(structRegistry);

        // todo: register fields based on structs
        //  - field acts like a wrapper of struct
        //  - clients only care about struct

        // todo: remove field demo
        fieldRegistry.registerFieldType("Test2", TestStruct2.class, new FieldDef("Test2"));

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

        ComponentScanEvent componentScanEvent = new ComponentScanEvent();
        MinecraftForge.EVENT_BUS.post(componentScanEvent);

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(componentScanEvent.scanPackageNames.toArray(new String[0]))
                .scan()) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation("com.cleanroommc.kirino.ecs.component.scan.CleanComponent")) {
                Class<?> clazz = classInfo.loadClass();
                KirinoRendering.LOGGER.info("Found and started scanning component class: " + clazz.getName());
                // todo: scan classes
            }
        }

        componentRegistry.register(
                "Position",
                PositionComponent.class,
                new MemberLayout("xyz", "test"),
                "vec3", "Test2");

        // todo: remove debug
        PositionComponent pos = (PositionComponent) componentRegistry.newComponent("Position", 1f, 2f, 3f, 123, 456);
        KirinoRendering.LOGGER.info("debug Position.xyz.x: " + pos.xyz.x); // expect 1
        KirinoRendering.LOGGER.info("debug Position.xyz.y: " + pos.xyz.y); // expect 2
        KirinoRendering.LOGGER.info("debug Position.xyz.z: " + pos.xyz.z); // expect 3
        KirinoRendering.LOGGER.info("debug Position.test.test2: " + pos.test.test2); // expect 123
        KirinoRendering.LOGGER.info("debug Position.test.testStruct.test: " + pos.test.testStruct.test); // expect 456

        entityManager = new EntityManager(componentRegistry);
        world = new CleanWorld(entityManager);
        renderSystem = new MinecraftRenderSystem(world);
    }
}
