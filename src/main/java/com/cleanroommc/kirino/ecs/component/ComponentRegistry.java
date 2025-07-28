package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberDescriptor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
    private final Map<String, MemberDescriptor> classMemberDescMap = new HashMap<>();

    /**
     * This method is the entry point to register components.
     * <code>memberDescriptor.fieldNames</code> must match <code>fieldTypeNames</code> one by one.
     *
     * @param name The registry name of the component
     * @param clazz The corresponding class of the component
     * @param memberDescriptor The metadata of the component class
     * @param fieldTypeNames The field registry names of the component
     */
    public void register(String name, Class<? extends ICleanComponent> clazz, MemberDescriptor memberDescriptor, String... fieldTypeNames) {
        register(name, clazz, memberDescriptor, Arrays.asList(fieldTypeNames));
    }

    /**
     * This method is the entry point to register components.
     * <code>memberDescriptor.fieldNames</code> must match <code>fieldTypeNames</code> one by one.
     *
     * @param name The registry name of the component
     * @param clazz The corresponding class of the component
     * @param memberDescriptor The metadata of the component class
     * @param fieldTypeNames The field registry names of the component
     */
    public void register(String name, Class<? extends ICleanComponent> clazz, MemberDescriptor memberDescriptor, List<String> fieldTypeNames) {
        comNameClassMapping.put(name, clazz);
        classMemberDescMap.put(name, memberDescriptor);

        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getFieldType(fieldTypeName);
            if (field == null) {
                throw new IllegalStateException("Field type " + fieldTypeName + " doesn't exist.");
            }
            fields.add(field);
        }

        ComponentDesc componentDesc = new ComponentDesc(name, fields);
        componentDescMap.put(name, componentDesc);
        componentDescFlattenedMap.put(name, new ComponentDescFlattened(componentDesc, fieldRegistry));
    }

    public boolean componentExists(Class<? extends ICleanComponent> clazz) {
        return comNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean componentExists(String name) {
        return comNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberDescriptor getClassMemberDescriptor(String name) {
        return classMemberDescMap.get(name);
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

    public String serializeComponentDesc(ComponentDesc componentDesc) {
        return componentDesc.toString(fieldRegistry.structRegistry);
    }
}
