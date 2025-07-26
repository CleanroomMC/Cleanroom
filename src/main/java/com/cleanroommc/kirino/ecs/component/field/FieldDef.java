package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.scalar.ScalarType;

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
}
