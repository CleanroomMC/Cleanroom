package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentDescFlattened;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FlattenedField;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * It guarantees SoA memory layout.
 */
public final class HeapPool extends ArchetypeDataPool{
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

    @Override
    public boolean containsEntity(int entityID) {
        return entityDataIndexes.containsKey(entityID);
    }

    @NonNull
    @Override
    @SuppressWarnings("DataFlowIssue")
    public ICleanComponent getComponent(int entityID, Class<? extends ICleanComponent> component) {
        ComDataLocation location = componentDataLocations.get(component);
        int index = entityDataIndexes.get(entityID);

        String comName = componentRegistry.getComponentName(component);
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
    public void setComponent(int entityID, ICleanComponent component) {
        Object[] args = componentRegistry.flattenComponent(component);

        ComDataLocation location = componentDataLocations.get(component.getClass());
        int index = entityDataIndexes.get(entityID);

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

    @Override
    public void addEntity(int entityID, List<ICleanComponent> components) {
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

        for (Class<? extends ICleanComponent> clazz : this.components) {
            ICleanComponent component = Objects.requireNonNull(components.stream().filter(c -> c.getClass().equals(clazz)).findFirst().orElse(null));

            Object[] args = componentRegistry.flattenComponent(component);
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
            if (indexCounter + shrinkStep <= currentSize) {
                if (currentSize - shrinkStep >= initSize) {
                    currentSize -= shrinkStep;
                    intPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    floatPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    booleanPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                }
            }
        } else {
            freeIndexes.add(index);
        }
    }

    @Override
    public IPrimitiveArray getArray(Class<? extends ICleanComponent> component, String... fieldAccessChain) {
        int ordinal = componentRegistry.getFieldOrdinal(componentRegistry.getComponentName(component), fieldAccessChain);
        ComDataLocation location = componentDataLocations.get(component);

        int index = 0;
        int intArrIndex = location.intArrFrom;
        int floatArrIndex = location.floatArrFrom;
        int booleanArrIndex = location.booleanArrFrom;
        for (FlattenedScalarType flattenedScalarType : location.order) {
            if (flattenedScalarType == FlattenedScalarType.INT) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(intPool.get(intArrIndex));
                }
                intArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(floatPool.get(floatArrIndex));
                }
                floatArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(booleanPool.get(booleanArrIndex));
                }
                booleanArrIndex++;
            }
            index++;
        }

        throw new IllegalArgumentException("Unable to find such array.");
    }

    @Override
    public ArrayRange getArrayRange() {
        return new ArrayRange(0, indexCounter, freeIndexes);
    }

    @Override
    public String getSnapshot() {
        int snapshotLength = Math.min(currentSize, 10);

        StringBuilder builder = new StringBuilder();
        builder.append("\n=====HeapPool Snapshot=====\n");
        int i = 0;
        for (Class<? extends ICleanComponent> clazz : components) {
            ComDataLocation location = componentDataLocations.get(clazz);
            builder.append("[").append(i++).append("] ")
                    .append("Component name: ").append(componentRegistry.getComponentName(clazz))
                    .append("; Component class: ").append(clazz.getName()).append("\n");

            int intArrIndex = location.intArrFrom;
            int floatArrIndex = location.floatArrFrom;
            int booleanArrIndex = location.booleanArrFrom;
            int j = 0;
            for (FlattenedScalarType flattenedScalarType : location.order) {
                builder.append("  [").append(j++).append(" ").append(flattenedScalarType).append("] ");
                if (flattenedScalarType == FlattenedScalarType.INT) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(intPool.get(intArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    intArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(floatPool.get(floatArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    floatArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(booleanPool.get(booleanArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    booleanArrIndex++;
                }
                builder.append("\n");
            }
        }

        return builder.toString();
    }
}
