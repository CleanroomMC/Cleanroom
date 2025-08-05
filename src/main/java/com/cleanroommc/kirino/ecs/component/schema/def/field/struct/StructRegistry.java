package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarConstructor;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.AccessHandlePool;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StructRegistry {
    private final BiMap<String, Class<?>> structTypeNameClassMapping = HashBiMap.create();
    private final Map<String, StructDef> structDefMap = new HashMap<>();
    private final Map<String, Integer> structUnitCountMap = new HashMap<>();
    private final Map<String, MemberLayout> classMemberLayoutMap = new HashMap<>();

    /**
     * This method is the entry point to register struct types.
     * <code>memberLayout.fieldNames</code> must match <code>structDef.fields</code> one by one.
     *
     * @param name The registry name of the struct
     * @param clazz The corresponding class of the struct
     * @param memberLayout The metadata of the struct class
     * @param structDef The actual struct type layout
     */
    public void registerStructType(String name, Class<?> clazz, MemberLayout memberLayout, StructDef structDef) {
        structTypeNameClassMapping.put(name, clazz);
        structDefMap.put(name, structDef);
        classMemberLayoutMap.put(name, memberLayout);
    }

    public boolean structTypeExists(Class<?> clazz) {
        return structTypeNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean structTypeExists(String name) {
        return structTypeNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(String name) {
        return classMemberLayoutMap.get(name);
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
    public StructDef getStructDef(String name) {
        return structDefMap.get(name);
    }

    @SuppressWarnings("DataFlowIssue")
    public int flattenedUnitCount(String name) {
        if (!structTypeExists(name)) {
            throw new IllegalStateException("Struct type " + name + " doesn't exist.");
        }
        Integer fetched = structUnitCountMap.get(name);
        if (fetched != null) {
            return fetched;
        }
        int unitCount = 0;
        for (FieldDef fieldDef : getStructDef(name).fields) {
            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                unitCount += FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
            } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
                unitCount += flattenedUnitCount(fieldDef.structTypeName);
            }
        }
        structUnitCountMap.put(name, unitCount);
        return unitCount;
    }

    // -----Struct Construction-----

    private final AccessHandlePool structAccessHandlePool = new AccessHandlePool();

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    public Object newStruct(String name, Object... args) {
        if (!structTypeExists(name)) {
            return null;
        }

        StructDef structDef = getStructDef(name);
        Class<?> structClass = getStructClass(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!structAccessHandlePool.classRegistered(structClass)) {
            structAccessHandlePool.register(structClass, memberLayout);
        }

        Object output = structAccessHandlePool.newClass(structClass);

        int index = 0;
        for (int i = 0; i < structDef.fields.size(); i++) {
            FieldDef fieldDef = structDef.fields.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            Object value = null;
            int unitCount = 0;
            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                unitCount = FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                value = ScalarConstructor.newScalar(fieldDef.scalarType, Arrays.copyOfRange(args, index, index + unitCount));
            } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
                unitCount = flattenedUnitCount(fieldDef.structTypeName);
                value = newStruct(fieldDef.structTypeName, Arrays.copyOfRange(args, index, index + unitCount));
            }
            index += unitCount;

            structAccessHandlePool.setFieldValue(structClass, output, fieldName, value);
        }

        return output;
    }
}
