package com.cleanroommc.kirino.ecs.component.field;

import com.cleanroommc.kirino.ecs.component.field.scalar.ScalarType;

public class FieldDef {
    public final FieldKind fieldKind;
    public final ScalarType scalarType;
    public final String structTypeID;

    public FieldDef(ScalarType scalarType) {
        fieldKind = FieldKind.SCALAR;
        this.scalarType = scalarType;
        structTypeID = null;
    }

    public FieldDef(String structTypeID) {
        fieldKind = FieldKind.STRUCT;
        scalarType = null;
        this.structTypeID = structTypeID;
    }
}
