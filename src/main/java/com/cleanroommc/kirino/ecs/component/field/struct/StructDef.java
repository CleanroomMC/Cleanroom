package com.cleanroommc.kirino.ecs.component.field.struct;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;

import java.util.Arrays;
import java.util.List;

public final class StructDef {
    public final List<FieldDef> fields;

    public StructDef(FieldDef... fields) {
        this.fields = Arrays.asList(fields);
    }
}
