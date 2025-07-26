package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.FieldRegistry;
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

    private final Map<String, ComponentDesc> componentDescMap = new HashMap<>();
    private final Map<String, ComponentDescRuntime> componentDescRuntimeMap = new HashMap<>();

    public void register(String name, String... fieldTypeNames) {
        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getField(fieldTypeName);
            if (field == null) {
                throw new IllegalStateException("Field type " + fieldTypeName + " doesn't exist.");
            }
            fields.add(field);
        }
        ComponentDesc componentDesc = new ComponentDesc(fields);
        componentDescMap.put(name, componentDesc);
        componentDescRuntimeMap.put(name, new ComponentDescRuntime(name, componentDesc, fieldRegistry));
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
