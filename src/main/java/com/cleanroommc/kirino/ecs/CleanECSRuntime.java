package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.KirinoRendering;
import com.cleanroommc.kirino.ecs.component.ComponentDesc;
import com.cleanroommc.kirino.ecs.component.ComponentDescFlattened;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.ComponentRegisterPlan;
import com.cleanroommc.kirino.ecs.component.scan.StructRegisterPlan;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.helper.ComponentScanningHelper;
import com.cleanroommc.kirino.ecs.component.scan.helper.StructScanningHelper;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.HeapPool;
import com.cleanroommc.kirino.ecs.storage.INativeArray;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.mcbridge.ecs.component.PositionComponent;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.common.MinecraftForge;
import org.joml.*;

import java.util.List;
import java.util.Map;

public class CleanECSRuntime {
    public final StructRegistry structRegistry;
    public final FieldRegistry fieldRegistry;
    public final ComponentRegistry componentRegistry;
    public final EntityManager entityManager;
    public final CleanWorld world;

    @SuppressWarnings("DataFlowIssue")
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
        for (StructRegisterPlan plan : StructScanningHelper.scanStructClasses(structScanningEvent, fieldRegistry)) {
            // struct class loading
            Class<?> structClass;
            try {
                structClass = Class.forName(plan.structClass(), false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) { // impossible
                continue;
            }

            structRegistry.registerStructType(
                    plan.structName(),
                    structClass,
                    plan.memberLayout(),
                    plan.structDef());
            fieldRegistry.registerFieldType(
                    plan.structName(),
                    structClass,
                    new FieldDef(plan.structName()));

            KirinoRendering.LOGGER.info("Registered struct " + plan.structName() + ". Loaded " + plan.structClass());
        }

        KirinoRendering.LOGGER.info("Struct defs are as follows.");
        for (Map.Entry<String, StructDef> entry : structRegistry.getStructDefMap().entrySet()) {
            KirinoRendering.LOGGER.info(entry.getKey() + ": " + entry.getValue().toString(structRegistry));
        }

        componentRegistry = new ComponentRegistry(fieldRegistry);

        ComponentScanningEvent componentScanningEvent = new ComponentScanningEvent();
        MinecraftForge.EVENT_BUS.post(componentScanningEvent);
        for (ComponentRegisterPlan plan : ComponentScanningHelper.scanComponentClasses(componentScanningEvent, fieldRegistry)) {
            // component class loading
            Class<? extends ICleanComponent> componentClass;
            try {
                componentClass = Class.forName(plan.componentClass(), false, Thread.currentThread().getContextClassLoader()).asSubclass(ICleanComponent.class);
            } catch (ClassNotFoundException e) { // impossible
                continue;
            }

            componentRegistry.registerComponent(
                    plan.componentName(),
                    componentClass,
                    plan.memberLayout(),
                    plan.fieldTypeNames());

            KirinoRendering.LOGGER.info("Registered component " + plan.componentName() + ". Loaded " + plan.componentClass());
        }

        KirinoRendering.LOGGER.info("Component descs are as follows.");
        ImmutableMap<String, ComponentDescFlattened> componentDescFlattenedMap = componentRegistry.getComponentDescFlattenedMap();
        for (Map.Entry<String, ComponentDesc> entry : componentRegistry.getComponentDescMap().entrySet()) {
            ComponentDescFlattened componentDescFlattened = componentDescFlattenedMap.get(entry.getKey());
            KirinoRendering.LOGGER.info(entry.getKey() + ": " + entry.getValue().toString(structRegistry));
            KirinoRendering.LOGGER.info(entry.getKey() + ": " + componentDescFlattened.toString());
        }

        entityManager = new EntityManager(componentRegistry);
        world = new CleanWorld(entityManager);

        KirinoRendering.LOGGER.info(componentRegistry.getComponentDescFlattened("PositionComponent").toString());
        KirinoRendering.LOGGER.info("debug test.test2: " + componentRegistry.getFieldOrdinal("PositionComponent", "test", "test2"));
        KirinoRendering.LOGGER.info("debug test.testStruct.test: " + componentRegistry.getFieldOrdinal("PositionComponent", "test", "testStruct", "test"));
        KirinoRendering.LOGGER.info("debug xyz.x: " + componentRegistry.getFieldOrdinal("PositionComponent", "xyz", "x"));

        PositionComponent pos = (PositionComponent) componentRegistry.newComponent("PositionComponent", new Object[]{123, 456, 1f, 2f, 3f});
        ArchetypeDataPool pool = new HeapPool(componentRegistry, List.of(PositionComponent.class), 100, 50, 50);
        pool.addEntity(111, List.of(pos));
        pool.addEntity(222, List.of(pos));
        pool.addEntity(333, List.of(pos));
        KirinoRendering.LOGGER.info(pool.getSnapshot());
        pool.removeEntity(222);
        pos.xyz.x = 10;
        pos.xyz.y = 10;
        pos.xyz.z = 10;
        pool.addEntity(222, List.of(pos));
        pool.addEntity(444, List.of(pos));
        KirinoRendering.LOGGER.info(pool.getSnapshot());

        INativeArray<?> array = pool.getArray(PositionComponent.class, "xyz", "x");
        KirinoRendering.LOGGER.info("debug: " + array.get(0));
        KirinoRendering.LOGGER.info("debug: " + array.get(1));
        KirinoRendering.LOGGER.info("debug: " + array.get(2));
        KirinoRendering.LOGGER.info("debug: " + array.get(3));
    }
}
