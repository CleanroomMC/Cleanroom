package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarConstructor;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarDeconstructor;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.AccessHandlePool;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

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

    public ImmutableMap<String, StructDef> getStructDefMap() {
        return ImmutableMap.copyOf(structDefMap);
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
        Preconditions.checkArgument(structTypeExists(name),
                "Struct type %s doesn't exist.", name);

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

    @SuppressWarnings("DataFlowIssue")
    public int getFieldOrdinal(String name, String... fieldAccessChain) {
        Preconditions.checkArgument(structTypeExists(name),
                "Struct type %s doesn't exist.", name);
        Preconditions.checkArgument(fieldAccessChain.length != 0,
                "The given \"fieldAccessChain\" must not be empty.");

        MemberLayout memberLayout = getClassMemberLayout(name);
        int index = 0;
        boolean match = false;
        while (!match && index < memberLayout.fieldNames.size()) {
            if (memberLayout.fieldNames.get(index).equals(fieldAccessChain[0])) {
                match = true;
            } else {
                index++;
            }
        }

        Preconditions.checkArgument(match,
                "Can't find a field that matches the \"fieldAccessChain\".");

        StructDef structDef = getStructDef(name);

        int ordinal = 0;
        for (int i = 0; i < index; i++) {
            if (structDef.fields.get(i).fieldKind == FieldKind.SCALAR) {
                ordinal += FlattenedScalarType.flattenedUnitCount(structDef.fields.get(i).scalarType);
            } else {
                ordinal += flattenedUnitCount(structDef.fields.get(i).structTypeName);
            }
        }

        // scalar field
        if (structDef.fields.get(index).fieldKind == FieldKind.SCALAR) {
            if (structDef.fields.get(index).scalarType == ScalarType.INT ||
                    structDef.fields.get(index).scalarType == ScalarType.FLOAT ||
                    structDef.fields.get(index).scalarType == ScalarType.BOOL) {
                if (fieldAccessChain.length == 1) {
                    return ordinal;
                } else {
                    throw new IllegalArgumentException("The given \"fieldAccessChain\" provides redundant terms after the deepest field.");
                }
            } else {
                if (fieldAccessChain.length == 1) {
                    throw new IllegalArgumentException("The given \"fieldAccessChain\" can't reach the deepest field.");
                } else if (fieldAccessChain.length == 2) {
                    // manual enumeration
                    switch (structDef.fields.get(index).scalarType) {
                        case VEC2 -> {
                            switch (fieldAccessChain[1]) {
                                case "x" -> {
                                    return ordinal;
                                }
                                case "y" -> {
                                    return ordinal + 1;
                                }
                            }
                        }
                        case VEC3 -> {
                            switch (fieldAccessChain[1]) {
                                case "x" -> {
                                    return ordinal;
                                }
                                case "y" -> {
                                    return ordinal + 1;
                                }
                                case "z" -> {
                                    return ordinal + 2;
                                }
                            }
                        }
                        case VEC4 -> {
                            switch (fieldAccessChain[1]) {
                                case "x" -> {
                                    return ordinal;
                                }
                                case "y" -> {
                                    return ordinal + 1;
                                }
                                case "z" -> {
                                    return ordinal + 2;
                                }
                                case "w" -> {
                                    return ordinal + 3;
                                }
                            }
                        }
                        case MAT3 -> {
                            switch (fieldAccessChain[1]) {
                                case "m00" -> {
                                    return ordinal;
                                }
                                case "m01" -> {
                                    return ordinal + 1;
                                }
                                case "m02" -> {
                                    return ordinal + 2;
                                }
                                case "m10" -> {
                                    return ordinal + 3;
                                }
                                case "m11" -> {
                                    return ordinal + 4;
                                }
                                case "m12" -> {
                                    return ordinal + 5;
                                }
                                case "m20" -> {
                                    return ordinal + 6;
                                }
                                case "m21" -> {
                                    return ordinal + 7;
                                }
                                case "m22" -> {
                                    return ordinal + 8;
                                }
                            }
                        }
                        case MAT4 -> {
                            switch (fieldAccessChain[1]) {
                                case "m00" -> {
                                    return ordinal;
                                }
                                case "m01" -> {
                                    return ordinal + 1;
                                }
                                case "m02" -> {
                                    return ordinal + 2;
                                }
                                case "m03" -> {
                                    return ordinal + 3;
                                }
                                case "m10" -> {
                                    return ordinal + 4;
                                }
                                case "m11" -> {
                                    return ordinal + 5;
                                }
                                case "m12" -> {
                                    return ordinal + 6;
                                }
                                case "m13" -> {
                                    return ordinal + 7;
                                }
                                case "m20" -> {
                                    return ordinal + 8;
                                }
                                case "m21" -> {
                                    return ordinal + 9;
                                }
                                case "m22" -> {
                                    return ordinal + 10;
                                }
                                case "m23" -> {
                                    return ordinal + 11;
                                }
                                case "m30" -> {
                                    return ordinal + 12;
                                }
                                case "m31" -> {
                                    return ordinal + 13;
                                }
                                case "m32" -> {
                                    return ordinal + 14;
                                }
                                case "m33" -> {
                                    return ordinal + 15;
                                }
                            }
                        }
                    }
                    throw new IllegalArgumentException("Can't find a field that matches the \"fieldAccessChain\".");
                } else {
                    throw new IllegalArgumentException("The given \"fieldAccessChain\" provides redundant terms after the deepest field.");
                }
            }
        // struct field
        } else {
            if (fieldAccessChain.length == 1) {
                throw new IllegalArgumentException("The given \"fieldAccessChain\" can't reach the deepest field.");
            }
            String[] newFieldAccessChain = new String[fieldAccessChain.length - 1];
            System.arraycopy(fieldAccessChain, 1, newFieldAccessChain, 0, newFieldAccessChain.length);
            return ordinal + getFieldOrdinal(structDef.fields.get(index).structTypeName, newFieldAccessChain);
        }
    }

    // -----Struct Construction-----

    private final AccessHandlePool structAccessHandlePool = new AccessHandlePool();

    @Nullable
    @SuppressWarnings("DataFlowIssue")
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

    // -----Struct Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object[] flattenStruct(@NonNull Object structInstance) {
        Preconditions.checkNotNull(structInstance);
        Preconditions.checkArgument(structTypeExists(structInstance.getClass()),
                "Struct class %s isn't registered.", structInstance.getClass().getName());

        String name = getStructTypeName(structInstance.getClass());

        StructDef structDef = getStructDef(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!structAccessHandlePool.classRegistered(structInstance.getClass())) {
            structAccessHandlePool.register(structInstance.getClass(), memberLayout);
        }

        Object[] args = new Object[flattenedUnitCount(name)];

        int index = 0;
        for (int i = 0; i < structDef.fields.size(); i++) {
            FieldDef fieldDef = structDef.fields.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = 0;
            Object[] _args = null;
            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                unitCount = FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                _args = ScalarDeconstructor.flattenScalar(fieldDef.scalarType, structAccessHandlePool.getFieldValue(structInstance.getClass(), structInstance, fieldName));
            } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
                unitCount = flattenedUnitCount(fieldDef.structTypeName);
                _args = flattenStruct(structAccessHandlePool.getFieldValue(structInstance.getClass(), structInstance, fieldName));
            }
            System.arraycopy(_args, 0, args, index, unitCount);
            index += unitCount;
        }

        return args;
    }
}
