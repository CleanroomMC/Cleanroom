package com.cleanroommc.kirino.ecs.component.field.struct;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StructRegistry {
    private final Map<String, StructDef> structDefMap = new HashMap<>();

    public void registerStruct(String name, StructDef structDef) {
        structDefMap.put(name, structDef);
    }

    @Nullable
    public StructDef getStruct(String name) {
        return structDefMap.get(name);
    }
}
