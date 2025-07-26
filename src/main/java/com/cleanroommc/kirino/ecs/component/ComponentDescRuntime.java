package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.field.FlattenedField;

import java.util.Arrays;
import java.util.List;

public class ComponentDescRuntime {
    public final String name;
    public final List<FlattenedField> fields;

    protected ComponentDescRuntime(String name, FlattenedField... fields) {
        this.name = name;
        this.fields = Arrays.asList(fields);
    }
}
