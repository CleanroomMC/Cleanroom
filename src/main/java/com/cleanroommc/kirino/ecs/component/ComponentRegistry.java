package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.AccessHandlePool;
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
    public void register(String name, Class<? extends ICleanComponent> clazz, MemberLayout memberLayout, String... fieldTypeNames) {
        register(name, clazz, memberLayout, Arrays.asList(fieldTypeNames));
    }

    /**
     * This method is the entry point to register components.
     * <code>memberLayout.fieldNames</code> must match <code>fieldTypeNames</code> one by one.
     *
     * @param name The registry name of the component
     * @param clazz The corresponding class of the component
     * @param memberLayout The metadata of the component class
     * @param fieldTypeNames The field registry names of the component
     */
    public void register(String name, Class<? extends ICleanComponent> clazz, MemberLayout memberLayout, List<String> fieldTypeNames) {
        comNameClassMapping.put(name, clazz);
        classMemberLayoutMap.put(name, memberLayout);

        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getFieldDef(fieldTypeName);
            if (field == null) {
                throw new IllegalStateException("Field type " + fieldTypeName + " doesn't exist.");
            }
            fields.add(field);
        }

        ComponentDesc componentDesc = new ComponentDesc(name, fields, fieldTypeNames);
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

    public String serializeComponentDesc(ComponentDesc componentDesc) {
        return componentDesc.toString(fieldRegistry.structRegistry);
    }

    // -----Component Construction-----

    private final AccessHandlePool componentAccessHandlePool = new AccessHandlePool();

    @SuppressWarnings("DataFlowIssue")
    @Nullable
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
}
