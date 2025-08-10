package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentDescFlattened;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FlattenedField;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class HeapPool extends ArchetypeDataPool{
    private final List<int[]> intPool = new ArrayList<>();
    private final List<float[]> floatPool = new ArrayList<>();
    private final List<boolean[]> booleanPool = new ArrayList<>();

    public static class ComDataLocation {
        public final int intArrFrom;
        public final int intArrTo;
        public final int floatArrFrom;
        public final int floatArrTo;
        public final int booleanArrFrom;
        public final int booleanArrTo;
        public final ImmutableList<FlattenedScalarType> order;

        private ComDataLocation(ImmutableList<FlattenedScalarType> order, int intArrFrom, int intArrTo, int floatArrFrom, int floatArrTo, int booleanArrFrom, int booleanArrTo) {
            this.intArrFrom = intArrFrom;
            this.intArrTo = intArrTo;
            this.floatArrFrom = floatArrFrom;
            this.floatArrTo = floatArrTo;
            this.booleanArrFrom = booleanArrFrom;
            this.booleanArrTo = booleanArrTo;
            this.order = order;
        }
    }

    private final Map<Class<? extends ICleanComponent>, ComDataLocation> componentDataLocations = new HashMap<>();

    // key: entity id
    // value: array index
    private final Map<Integer, Integer> entityDataIndexes = new HashMap<>();

    private final List<Integer> freeIndexes = new ArrayList<>();
    private int indexCounter = 0;

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     * </ul>
     *
     * @param componentRegistry The component registry
     * @param components The component types for this archetype
     */
    @SuppressWarnings("DataFlowIssue")
    public HeapPool(ComponentRegistry componentRegistry, List<Class<? extends ICleanComponent>> components, int initSize, int growStep, int shrinkStep) {
        super(componentRegistry, components, initSize, growStep, shrinkStep);

        int intArrCount = 0;
        int floatArrCount = 0;
        int booleanArrCount = 0;
        for (Class<? extends ICleanComponent> clazz : components) {
            ComponentDescFlattened descFlattened = componentRegistry.getComponentDescFlattened(componentRegistry.getComponentName(clazz));

            int intArrFrom = intArrCount;
            int floatArrFrom = floatArrCount;
            int booleanArrFrom = booleanArrCount;
            List<FlattenedScalarType> order = new ArrayList<>();

            for (FlattenedField flattenedField : descFlattened.fields) {
                for (FlattenedScalarType flattenedScalarType : flattenedField.scalarTypes) {
                    if (flattenedScalarType == FlattenedScalarType.INT) {
                        intPool.add(new int[initSize]);
                        intArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                        floatPool.add(new float[initSize]);
                        floatArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                        booleanPool.add(new boolean[initSize]);
                        booleanArrCount++;
                    }
                    order.add(flattenedScalarType);
                }
            }

            int intArrTo = intArrCount;
            int floatArrTo = floatArrCount;
            int booleanArrTo = booleanArrCount;

            componentDataLocations.put(clazz, new ComDataLocation(ImmutableList.copyOf(order), intArrFrom, intArrTo, floatArrFrom, floatArrTo, booleanArrFrom, booleanArrTo));
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("DataFlowIssue")
    public ICleanComponent getComponent(int entityID, Class<? extends ICleanComponent> clazz) {
        ComDataLocation location = componentDataLocations.get(clazz);
        int index = entityDataIndexes.get(entityID);

        String comName = componentRegistry.getComponentName(clazz);
        int unitCount = componentRegistry.getComponentDescFlattened(comName).getUnitCount();

        int argIndex = 0;
        Object[] args = new Object[unitCount];

        int intArrIndex = location.intArrFrom;
        int floatArrIndex = location.floatArrFrom;
        int booleanArrIndex = location.booleanArrFrom;
        for (FlattenedScalarType flattenedScalarType : location.order) {
            if (flattenedScalarType == FlattenedScalarType.INT) {
                args[argIndex] = intPool.get(intArrIndex)[index];
                intArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                args[argIndex] = floatPool.get(floatArrIndex)[index];
                floatArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                args[argIndex] = booleanPool.get(booleanArrIndex)[index];
                booleanArrIndex++;
            }
            argIndex++;
        }

        return componentRegistry.newComponent(comName, args);
    }

    @Override
    public void addEntity(int entityID, Map<Class<? extends ICleanComponent>, Object[]> componentArgs) {
        int index;
        if (freeIndexes.isEmpty()) {
            // grow pool
            if (indexCounter >= currentSize) {
                currentSize += growStep;
                intPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                floatPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                booleanPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
            }
            index = indexCounter++;
        } else {
            index = freeIndexes.removeLast();
        }

        entityDataIndexes.put(entityID, index);

        for (Class<? extends ICleanComponent> clazz : components) {
            Object[] args = componentArgs.get(clazz);
            ComDataLocation location = componentDataLocations.get(clazz);

            int argIndex = 0;
            int intArrIndex = location.intArrFrom;
            int floatArrIndex = location.floatArrFrom;
            int booleanArrIndex = location.booleanArrFrom;
            for (FlattenedScalarType flattenedScalarType : location.order) {
                if (flattenedScalarType == FlattenedScalarType.INT) {
                    intPool.get(intArrIndex)[index] = (Integer) args[argIndex];
                    intArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                    floatPool.get(floatArrIndex)[index] = (Float) args[argIndex];
                    floatArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                    booleanPool.get(booleanArrIndex)[index] = (Boolean) args[argIndex];
                    booleanArrIndex++;
                }
                argIndex++;
            }
        }
    }

    @Override
    public void removeEntity(int entityID) {
        int index = entityDataIndexes.remove(entityID);
        if (index == indexCounter - 1) {
            indexCounter--;
            List<Integer> freeIndexesToRemove = new ArrayList<>();
            while (freeIndexes.contains(indexCounter - 1)) {
                freeIndexesToRemove.add(--indexCounter);
            }
            for (Integer freeIndex : freeIndexesToRemove) {
                freeIndexes.remove(freeIndex);
            }
            // shrink pool
            if (indexCounter + 1 + shrinkStep >= currentSize) {
                currentSize -= shrinkStep;
                intPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                floatPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                booleanPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
            }
        } else {
            freeIndexes.add(index);
        }
    }
}
