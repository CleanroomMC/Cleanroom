package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.KirinoRendering;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.scan.RegisterStructPlan;
import com.cleanroommc.kirino.ecs.component.scan.StructScanningHelper;
import com.cleanroommc.kirino.ecs.component.scan.StructScanningEvent;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.mcbridge.ecs.component.PositionComponent;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.system.render.RenderSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.mcbridge.ecs.system.MinecraftRenderSystem;
import net.minecraftforge.common.MinecraftForge;
import org.joml.*;

import java.util.Map;

public class CleanECSRuntime {
    public final StructRegistry structRegistry;
    public final FieldRegistry fieldRegistry;
    public final ComponentRegistry componentRegistry;
    public final EntityManager entityManager;
    public final CleanWorld world;
    public final RenderSystem renderSystem;

    private CleanECSRuntime() {
        structRegistry = new StructRegistry();
        fieldRegistry = new FieldRegistry(structRegistry);

        // hard coded fields
        fieldRegistry.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fieldRegistry.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        fieldRegistry.registerFieldType("bool", boolean.class, new FieldDef(ScalarType.BOOL));
        fieldRegistry.registerFieldType("vec2", Vector2f.class, new FieldDef(ScalarType.VEC2));
        fieldRegistry.registerFieldType("vec3", Vector3f.class, new FieldDef(ScalarType.VEC3));
        fieldRegistry.registerFieldType("vec4", Vector4f.class, new FieldDef(ScalarType.VEC4));
        fieldRegistry.registerFieldType("mat3", Matrix3f.class, new FieldDef(ScalarType.MAT3));
        fieldRegistry.registerFieldType("mat4", Matrix4f.class, new FieldDef(ScalarType.MAT4));

        StructScanningEvent structScanningEvent = new StructScanningEvent();
        MinecraftForge.EVENT_BUS.post(structScanningEvent);
        for (RegisterStructPlan plan : StructScanningHelper.scanAndLoadStructClasses(structScanningEvent, fieldRegistry)) {
            structRegistry.registerStructType(
                    plan.structName(),
                    plan.structClass(),
                    plan.memberLayout(),
                    plan.structDef());
            fieldRegistry.registerFieldType(
                    plan.structName(),
                    plan.structClass(),
                    new FieldDef(plan.structName()));
            KirinoRendering.LOGGER.info("Registered struct " + plan.structName() + ": " + plan.structClass());
        }

        KirinoRendering.LOGGER.info("Struct defs are as follows.");
        for (Map.Entry<String, StructDef> entry : structRegistry.getStructDefMap().entrySet()) {
            KirinoRendering.LOGGER.info(entry.getKey() + ": " + entry.getValue().toString(structRegistry));
        }

        componentRegistry = new ComponentRegistry(fieldRegistry);

//        ComponentScanningEvent componentScanningEvent = new ComponentScanningEvent();
//        MinecraftForge.EVENT_BUS.post(componentScanningEvent);
//
//        try (ScanResult scanResult = new ClassGraph()
//                .enableClassInfo()
//                .enableAnnotationInfo()
//                .acceptPackages(componentScanningEvent.scanPackageNames.toArray(new String[0]))
//                .scan()) {
//
//            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation("com.cleanroommc.kirino.ecs.component.scan.CleanComponent")) {
//                Class<?> clazz = classInfo.loadClass();
//                KirinoRendering.LOGGER.info("Found and started scanning component class: " + clazz.getName());
//                // todo: scan classes
//            }
//        }

        componentRegistry.registerComponent(
                "Position",
                PositionComponent.class,
                new MemberLayout("xyz", "test"),
                "vec3", "TestStruct2");

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
