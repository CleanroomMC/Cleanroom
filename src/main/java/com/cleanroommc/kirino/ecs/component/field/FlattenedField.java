package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.field.struct.StructManager;

import java.util.ArrayList;
import java.util.List;

public class FlattenedField {
    public final List<FlattenedScalarType> scalarTypes;

    private static List<FlattenedScalarType> makeFlattenedField(FieldDef fieldDef, StructManager structManager) {
        if (fieldDef.fieldKind == FieldKind.SCALAR)
            return FlattenedScalarType.flatten(fieldDef.scalarType);
        else if (fieldDef.fieldKind == FieldKind.STRUCT) {
            StructDef structDef = structManager.getStruct(fieldDef.structTypeName);
            if (structDef == null) {
                throw new IllegalStateException("Struct type " + fieldDef.structTypeName + " isn't defined");
            }
            List<FlattenedScalarType> output = new ArrayList<>();
            for (FieldDef field : structDef.fields) {
                output.addAll(makeFlattenedField(field, structManager));
            }
            return output;
        }
        return new ArrayList<>();
    }

    protected FlattenedField(FieldDef fieldDef, StructManager structManager) {
        scalarTypes = makeFlattenedField(fieldDef, structManager);
    }
}
