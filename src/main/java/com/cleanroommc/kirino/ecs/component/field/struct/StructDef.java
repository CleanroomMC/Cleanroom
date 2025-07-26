package com.cleanroommc.kirino.ecs.component.field.struct;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;

import java.util.Arrays;
import java.util.List;

public final class StructDef {
    public final List<FieldDef> fields;

    public StructDef(FieldDef... fields) {
        this.fields = Arrays.asList(fields);
    }

    public String toString(StructRegistry structRegistry) {
        StringBuilder builder = new StringBuilder();
        builder.append("StructDef{ ");
        for (int i = 0; i < fields.size(); i++) {
            FieldDef field = fields.get(i);
            if (i == fields.size() - 1){
                builder.append(field.toString(structRegistry));
            } else {
                builder.append(field.toString(structRegistry)).append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
