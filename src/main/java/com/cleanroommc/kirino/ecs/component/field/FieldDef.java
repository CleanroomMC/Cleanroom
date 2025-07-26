package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.field.struct.StructRegistry;

public final class FieldDef {
    public final FieldKind fieldKind;
    public final ScalarType scalarType;
    public final String structTypeName;

    public FieldDef(ScalarType scalarType) {
        fieldKind = FieldKind.SCALAR;
        this.scalarType = scalarType;
        structTypeName = null;
    }

    public FieldDef(String structTypeName) {
        fieldKind = FieldKind.STRUCT;
        scalarType = null;
        this.structTypeName = structTypeName;
    }

    public String toString(StructRegistry structRegistry) {
        if (fieldKind == FieldKind.SCALAR) {
            return "FieldDef{ scalarType=" + scalarType + " }";
        }
        else if (fieldKind == FieldKind.STRUCT) {
            StructDef structDef = structRegistry.getStruct(structTypeName);
            if (structDef == null) {
                throw new IllegalStateException("Struct type " + structTypeName + " doesn't exist.");
            }
            return "FieldDef{ struct=" + structDef.toString(structRegistry) + " }";
        }
        return "";
    }
}
