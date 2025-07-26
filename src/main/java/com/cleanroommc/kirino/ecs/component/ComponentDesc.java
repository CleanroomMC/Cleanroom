package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;

import java.util.Arrays;
import java.util.List;

public class ComponentDesc {
    public final List<FieldDef> fields;

    protected ComponentDesc(FieldDef... fields) {
        this.fields = Arrays.asList(fields);
    }
}
