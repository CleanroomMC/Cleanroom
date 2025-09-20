package com.cleanroommc.kirino.ecs;

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
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobRegistry;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.job.event.JobRegistrationEvent;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.joml.*;

import java.util.Map;

public class CleanECSRuntime {
    public final StructRegistry structRegistry;
    public final FieldRegistry fieldRegistry;
    public final ComponentRegistry componentRegistry;
    public final EntityManager entityManager;
    public final JobRegistry jobRegistry;
    public final JobScheduler jobScheduler;

    @SuppressWarnings("DataFlowIssue")
    private CleanECSRuntime(EventBus eventBus, Logger logger) {
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
        eventBus.post(structScanningEvent);
        for (StructRegisterPlan plan : StructScanningHelper.scanStructClasses(structScanningEvent, fieldRegistry)) {
            // struct class loading
            Class<?> structClass;
            try {
                structClass = Class.forName(plan.structClass(), false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) { // impossible
                continue;
            }

            try {
                structClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("CleanStruct " + structClass.getName() + " is missing a default constructor with no parameters.", e);
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

            logger.info("Registered struct " + plan.structName() + ". Loaded " + plan.structClass());
        }

        logger.info("Struct defs are as follows:");
        for (Map.Entry<String, StructDef> entry : structRegistry.getStructDefMap().entrySet()) {
            logger.info("  - " + entry.getKey() + ": " + entry.getValue().toString(structRegistry));
        }

        componentRegistry = new ComponentRegistry(fieldRegistry);

        ComponentScanningEvent componentScanningEvent = new ComponentScanningEvent();
        eventBus.post(componentScanningEvent);
        for (ComponentRegisterPlan plan : ComponentScanningHelper.scanComponentClasses(componentScanningEvent, fieldRegistry)) {
            // component class loading
            Class<? extends ICleanComponent> componentClass;
            try {
                componentClass = Class.forName(plan.componentClass(), false, Thread.currentThread().getContextClassLoader()).asSubclass(ICleanComponent.class);
            } catch (ClassNotFoundException e) { // impossible
                continue;
            }

            try {
                componentClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("CleanComponent " + componentClass.getName() + " is missing a default constructor with no parameters.", e);
            }

            componentRegistry.registerComponent(
                    plan.componentName(),
                    componentClass,
                    plan.memberLayout(),
                    plan.fieldTypeNames());

            logger.info("Registered component " + plan.componentName() + ". Loaded " + plan.componentClass());
        }

        logger.info("Component descs are as follows:");
        ImmutableMap<String, ComponentDescFlattened> componentDescFlattenedMap = componentRegistry.getComponentDescFlattenedMap();
        for (Map.Entry<String, ComponentDesc> entry : componentRegistry.getComponentDescMap().entrySet()) {
            ComponentDescFlattened componentDescFlattened = componentDescFlattenedMap.get(entry.getKey());
            logger.info("  - " + entry.getKey() + ": " + entry.getValue().toString(structRegistry));
            logger.info("  - " + entry.getKey() + ": " + componentDescFlattened.toString());
        }

        entityManager = new EntityManager(componentRegistry);

        jobRegistry = new JobRegistry(componentRegistry);
        jobScheduler = new JobScheduler(jobRegistry, entityManager);

        JobRegistrationEvent jobRegistrationEvent = new JobRegistrationEvent();
        eventBus.post(jobRegistrationEvent);
        for (Class<? extends IParallelJob> clazz : jobRegistrationEvent.parallelJobClasses) {
            jobRegistry.registerParallelJob(clazz);
        }
    }
}
