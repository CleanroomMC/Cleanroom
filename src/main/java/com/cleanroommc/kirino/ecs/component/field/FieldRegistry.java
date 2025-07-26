package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.struct.StructRegistry;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FieldRegistry {
    public final StructRegistry structRegistry;

    public FieldRegistry(StructRegistry structRegistry) {
        this.structRegistry = structRegistry;
    }

    private final Map<String, FieldDef> fieldDefMap = new HashMap<>();

    public void registerField(String name, FieldDef fieldDef) {
        fieldDefMap.put(name, fieldDef);
    }

    @Nullable
    public FieldDef getField(String name) {
        return fieldDefMap.get(name);
    }

    public FlattenedField flatten(FieldDef fieldDef) {
        return new FlattenedField(fieldDef, structRegistry);
    }
}
