package com.cleanroommc.kirino.ecs.component.field.struct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StructRegistry {
    private final BiMap<String, Class<?>> structTypeNameClassMapping = HashBiMap.create();
    private final Map<String, StructDef> structDefMap = new HashMap<>();

    public void registerStructType(String name, Class<?> clazz, StructDef structDef) {
        structTypeNameClassMapping.put(name, clazz);
        structDefMap.put(name, structDef);
    }

    public boolean structTypeExists(Class<?> clazz) {
        return structTypeNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean structTypeExists(String name) {
        return structTypeNameClassMapping.containsKey(name);
    }

    @Nullable
    public String getStructTypeName(Class<?> clazz) {
        return structTypeNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<?> getStructClass(String name) {
        return structTypeNameClassMapping.get(name);
    }

    @Nullable
    public StructDef getStructType(String name) {
        return structDefMap.get(name);
    }
}
