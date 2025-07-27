package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.struct.StructRegistry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FieldRegistry {
    public final StructRegistry structRegistry;

    public FieldRegistry(StructRegistry structRegistry) {
        this.structRegistry = structRegistry;
    }

    private final BiMap<String, Class<?>> fieldTypeNameClassMapping = HashBiMap.create();
    private final Map<String, FieldDef> fieldDefMap = new HashMap<>();

    public void registerFieldType(String name, Class<?> clazz, FieldDef fieldDef) {
        fieldTypeNameClassMapping.put(name, clazz);
        fieldDefMap.put(name, fieldDef);
    }

    public boolean fieldTypeExists(Class<?> clazz) {
        return fieldTypeNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean fieldTypeExists(String name) {
        return fieldTypeNameClassMapping.containsKey(name);
    }

    @Nullable
    public String getFieldTypeName(Class<?> clazz) {
        return fieldTypeNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<?> getFieldClass(String name) {
        return fieldTypeNameClassMapping.get(name);
    }

    @Nullable
    public FieldDef getFieldType(String name) {
        return fieldDefMap.get(name);
    }

    public FlattenedField flatten(FieldDef fieldDef) {
        return new FlattenedField(fieldDef, structRegistry);
    }
}
