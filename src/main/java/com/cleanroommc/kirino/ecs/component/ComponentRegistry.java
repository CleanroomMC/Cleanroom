package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.FieldRegistry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentRegistry {
    private final FieldRegistry fieldRegistry;

    public ComponentRegistry(FieldRegistry fieldRegistry) {
        this.fieldRegistry = fieldRegistry;
    }

    private final BiMap<String, Class<? extends ICleanComponent>> comNameClassMapping = HashBiMap.create();
    private final Map<String, ComponentDesc> componentDescMap = new HashMap<>();
    private final Map<String, ComponentDescRuntime> componentDescRuntimeMap = new HashMap<>();

    public void register(String name, Class<? extends ICleanComponent> clazz, String... fieldTypeNames) {
        comNameClassMapping.put(name, clazz);

        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getFieldType(fieldTypeName);
            if (field == null) {
                throw new IllegalStateException("Field type " + fieldTypeName + " doesn't exist.");
            }
            fields.add(field);
        }

        ComponentDesc componentDesc = new ComponentDesc(fields);
        componentDescMap.put(name, componentDesc);
        componentDescRuntimeMap.put(name, new ComponentDescRuntime(name, componentDesc, fieldRegistry));
    }

    public boolean componentExists(Class<? extends ICleanComponent> clazz) {
        return comNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean componentExists(String name) {
        return comNameClassMapping.containsKey(name);
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
    public ComponentDescRuntime getComponentDescRuntime(String name) {
        return componentDescRuntimeMap.get(name);
    }

    public String serializeComponentDesc(ComponentDesc componentDesc) {
        return componentDesc.toString(fieldRegistry.structRegistry);
    }
}
