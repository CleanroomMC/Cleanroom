package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.AccessHandlePool;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ComponentRegistry {
    private final FieldRegistry fieldRegistry;

    public ComponentRegistry(FieldRegistry fieldRegistry) {
        this.fieldRegistry = fieldRegistry;
    }

    private final BiMap<String, Class<? extends ICleanComponent>> comNameClassMapping = HashBiMap.create();
    private final Map<String, ComponentDesc> componentDescMap = new HashMap<>();
    private final Map<String, ComponentDescFlattened> componentDescFlattenedMap = new HashMap<>();
    private final Map<String, MemberLayout> classMemberLayoutMap = new HashMap<>();

    /**
     * This method is the entry point to register components.
     * <code>memberLayout.fieldNames</code> must match <code>fieldTypeNames</code> one by one.
     *
     * @param name The registry name of the component
     * @param clazz The corresponding class of the component
     * @param memberLayout The metadata of the component class
     * @param fieldTypeNames The field registry names of the component
     */
    public void registerComponent(String name, Class<? extends ICleanComponent> clazz, MemberLayout memberLayout, String... fieldTypeNames) {
        comNameClassMapping.put(name, clazz);
        classMemberLayoutMap.put(name, memberLayout);

        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getFieldDef(fieldTypeName);
            Preconditions.checkArgument(field != null,
                    "Field type %s doesn't exist.", fieldTypeName);

            fields.add(field);
        }

        ComponentDesc componentDesc = new ComponentDesc(name, fields, fieldTypeNames);
        componentDescMap.put(name, componentDesc);
        componentDescFlattenedMap.put(name, new ComponentDescFlattened(componentDesc, fieldRegistry));
    }

    public ImmutableMap<String, ComponentDesc> getComponentDescMap() {
        return ImmutableMap.copyOf(componentDescMap);
    }

    public ImmutableMap<String, ComponentDescFlattened> getComponentDescFlattenedMap() {
        return ImmutableMap.copyOf(componentDescFlattenedMap);
    }

    public boolean componentExists(Class<? extends ICleanComponent> clazz) {
        return comNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean componentExists(String name) {
        return comNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(String name) {
        return classMemberLayoutMap.get(name);
    }

    @Nullable
    public String getComponentName(Class<? extends ICleanComponent> clazz) {
        return comNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<? extends ICleanComponent> getComponentClass(String name) {
        return comNameClassMapping.get(name);
    }

    @Nullable
    public ComponentDesc getComponentDesc(String name) {
        return componentDescMap.get(name);
    }

    @Nullable
    public ComponentDescFlattened getComponentDescFlattened(String name) {
        return componentDescFlattenedMap.get(name);
    }

    @SuppressWarnings("DataFlowIssue")
    public int getFieldOrdinal(String name, String... fieldAccessChain) {
        Preconditions.checkArgument(componentExists(name),
                "Component type %s doesn't exist.", name);
        Preconditions.checkArgument(fieldAccessChain.length != 0,
                "The given \"fieldAccessChain\" must not be empty.");

        MemberLayout memberLayout = getClassMemberLayout(name);
        int index = 0;
        boolean match = false;
        while (!match && index < memberLayout.fieldNames.size()) {
            if (memberLayout.fieldNames.get(index).equals(fieldAccessChain[0])) {
                match = true;
            } else {
                index++;
            }
        }

        Preconditions.checkArgument(match,
                "Can't find a field that matches the \"fieldAccessChain\".");

        ComponentDescFlattened componentDescFlattened = getComponentDescFlattened(name);
        int ordinal = 0;
        for (int i = 0; i < index; i++) {
            ordinal += componentDescFlattened.fields.get(i).getUnitCount();
        }

        ComponentDesc componentDesc = getComponentDesc(name);
        // scalar field
        if (componentDesc.fields.get(index).fieldKind == FieldKind.SCALAR) {
            if (componentDesc.fields.get(index).scalarType == ScalarType.INT ||
                    componentDesc.fields.get(index).scalarType == ScalarType.FLOAT ||
                    componentDesc.fields.get(index).scalarType == ScalarType.BOOL) {
                if (fieldAccessChain.length == 1) {
                    return ordinal;
                } else {
                    throw new IllegalArgumentException("The given \"fieldAccessChain\" provides redundant terms after the deepest field.");
                }
            } else {
                if (fieldAccessChain.length == 1) {
                    throw new IllegalArgumentException("The given \"fieldAccessChain\" can't reach the deepest field.");
                } else if (fieldAccessChain.length == 2) {
                    // manually enumeration
                    switch (componentDesc.fields.get(index).scalarType) {
                        case VEC2 -> {
                            switch (fieldAccessChain[1]) {
                                case "x" -> {
                                    return ordinal;
                                }
                                case "y" -> {
                                    return ordinal + 1;
                                }
                            }
                        }
                        case VEC3 -> {
                            switch (fieldAccessChain[1]) {
                                case "x" -> {
                                    return ordinal;
                                }
                                case "y" -> {
                                    return ordinal + 1;
                                }
                                case "z" -> {
                                    return ordinal + 2;
                                }
                            }
                        }
                        case VEC4 -> {
                            switch (fieldAccessChain[1]) {
                                case "x" -> {
                                    return ordinal;
                                }
                                case "y" -> {
                                    return ordinal + 1;
                                }
                                case "z" -> {
                                    return ordinal + 2;
                                }
                                case "w" -> {
                                    return ordinal + 3;
                                }
                            }
                        }
                        case MAT3 -> {
                            switch (fieldAccessChain[1]) {
                                case "m00" -> {
                                    return ordinal;
                                }
                                case "m01" -> {
                                    return ordinal + 1;
                                }
                                case "m02" -> {
                                    return ordinal + 2;
                                }
                                case "m10" -> {
                                    return ordinal + 3;
                                }
                                case "m11" -> {
                                    return ordinal + 4;
                                }
                                case "m12" -> {
                                    return ordinal + 5;
                                }
                                case "m20" -> {
                                    return ordinal + 6;
                                }
                                case "m21" -> {
                                    return ordinal + 7;
                                }
                                case "m22" -> {
                                    return ordinal + 8;
                                }
                            }
                        }
                        case MAT4 -> {
                            switch (fieldAccessChain[1]) {
                                case "m00" -> {
                                    return ordinal;
                                }
                                case "m01" -> {
                                    return ordinal + 1;
                                }
                                case "m02" -> {
                                    return ordinal + 2;
                                }
                                case "m03" -> {
                                    return ordinal + 3;
                                }
                                case "m10" -> {
                                    return ordinal + 4;
                                }
                                case "m11" -> {
                                    return ordinal + 5;
                                }
                                case "m12" -> {
                                    return ordinal + 6;
                                }
                                case "m13" -> {
                                    return ordinal + 7;
                                }
                                case "m20" -> {
                                    return ordinal + 8;
                                }
                                case "m21" -> {
                                    return ordinal + 9;
                                }
                                case "m22" -> {
                                    return ordinal + 10;
                                }
                                case "m23" -> {
                                    return ordinal + 11;
                                }
                                case "m30" -> {
                                    return ordinal + 12;
                                }
                                case "m31" -> {
                                    return ordinal + 13;
                                }
                                case "m32" -> {
                                    return ordinal + 14;
                                }
                                case "m33" -> {
                                    return ordinal + 15;
                                }
                            }
                        }
                    }
                    throw new IllegalArgumentException("Can't find a field that matches the \"fieldAccessChain\".");
                } else {
                    throw new IllegalArgumentException("The given \"fieldAccessChain\" provides redundant terms after the deepest field.");
                }
            }
        // struct field
        } else {
            if (fieldAccessChain.length == 1) {
                throw new IllegalArgumentException("The given \"fieldAccessChain\" can't reach the deepest field.");
            }
            String[] newFieldAccessChain = new String[fieldAccessChain.length - 1];
            System.arraycopy(fieldAccessChain, 1, newFieldAccessChain, 0, newFieldAccessChain.length);
            return ordinal + fieldRegistry.structRegistry.getFieldOrdinal(componentDesc.fields.get(index).structTypeName, newFieldAccessChain);
        }
    }

    // -----Component Construction-----

    private final AccessHandlePool componentAccessHandlePool = new AccessHandlePool();

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public ICleanComponent newComponent(String name, Object... args) {
        if (!componentExists(name)) {
            return null;
        }

        ComponentDesc componentDesc = getComponentDesc(name);
        ComponentDescFlattened componentDescFlattened = getComponentDescFlattened(name);
        Class<? extends ICleanComponent> componentClass = getComponentClass(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!componentAccessHandlePool.classRegistered(componentClass)) {
            componentAccessHandlePool.register(componentClass, memberLayout);
        }

        Object output = componentAccessHandlePool.newClass(componentClass);

        int index = 0;
        for (int i = 0; i < componentDesc.fields.size(); i++) {
            String fieldTypeName = componentDesc.fieldTypeNames.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = componentDescFlattened.fields.get(i).getUnitCount();
            Object value = fieldRegistry.newField(fieldTypeName, Arrays.copyOfRange(args, index, index + unitCount));
            index += unitCount;

            componentAccessHandlePool.setFieldValue(componentClass, output, fieldName, value);
        }

        return (ICleanComponent) output;
    }

    // -----Component Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object[] flattenComponent(@NonNull ICleanComponent component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(componentExists(component.getClass()),
                "Component class %s isn't registered.", component.getClass().getName());

        String name = getComponentName(component.getClass());

        ComponentDesc componentDesc = getComponentDesc(name);
        ComponentDescFlattened componentDescFlattened = getComponentDescFlattened(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!componentAccessHandlePool.classRegistered(component.getClass())) {
            componentAccessHandlePool.register(component.getClass(), memberLayout);
        }

        Object[] args = new Object[componentDescFlattened.getUnitCount()];

        int index = 0;
        for (int i = 0; i < componentDesc.fields.size(); i++) {
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = componentDescFlattened.fields.get(i).getUnitCount();
            Object[] _args = fieldRegistry.flattenField(componentAccessHandlePool.getFieldValue(component.getClass(), component, fieldName));
            System.arraycopy(_args, 0, args, index, unitCount);
            index += unitCount;
        }

        return args;
    }
}
