package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.struct.StructManager;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FieldManager {
    private final StructManager structManager;

    public FieldManager(StructManager structManager) {
        this.structManager = structManager;
    }

    private final Map<String, FieldDef> fieldDefMap = new HashMap<>();

    public void registerField(String name, FieldDef fieldDef) {
        fieldDefMap.put(name, fieldDef);
    }

    @Nullable
    public FieldDef getField(String name) {
        return fieldDefMap.get(name);
    }

    public FieldBuilder fieldBuilder() {
        return new FieldBuilder();
    }

    public FlattenedField flatten(FieldDef fieldDef) {
        return null; //todo
    }
}
