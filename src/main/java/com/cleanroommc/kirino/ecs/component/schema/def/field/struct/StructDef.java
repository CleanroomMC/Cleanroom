package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * The opposite concept is a scalar ({@link ScalarType}).
 */
public final class StructDef {
    public final ImmutableList<FieldDef> fields;

    public StructDef(List<FieldDef> fields) {
        this.fields = ImmutableList.copyOf(fields);
    }

    public StructDef(FieldDef... fields) {
        this.fields = ImmutableList.copyOf(fields);
    }

    public String toString(StructRegistry structRegistry) {
        StringBuilder builder = new StringBuilder();
        builder.append("StructDef{ ");
        for (int i = 0; i < fields.size(); i++) {
            FieldDef field = fields.get(i);
            if (i == fields.size() - 1) {
                builder.append(field.toString(structRegistry));
            } else {
                builder.append(field.toString(structRegistry)).append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
