package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberDescriptor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StructRegistry {
    private final BiMap<String, Class<?>> structTypeNameClassMapping = HashBiMap.create();
    private final Map<String, StructDef> structDefMap = new HashMap<>();
    private final Map<String, MemberDescriptor> classMemberDescMap = new HashMap<>();

    /**
     * This method is the entry point to register struct types.
     * <code>memberDescriptor.fieldNames</code> must match <code>structDef.fields</code> one by one.
     *
     * @param name The registry name of the struct
     * @param clazz The corresponding class of the struct
     * @param memberDescriptor The metadata of the struct class
     * @param structDef The actual struct type layout
     */
    public void registerStructType(String name, Class<?> clazz, MemberDescriptor memberDescriptor, StructDef structDef) {
        structTypeNameClassMapping.put(name, clazz);
        structDefMap.put(name, structDef);
        classMemberDescMap.put(name, memberDescriptor);
    }

    public boolean structTypeExists(Class<?> clazz) {
        return structTypeNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean structTypeExists(String name) {
        return structTypeNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberDescriptor getClassMemberDescriptor(String name) {
        return classMemberDescMap.get(name);
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
