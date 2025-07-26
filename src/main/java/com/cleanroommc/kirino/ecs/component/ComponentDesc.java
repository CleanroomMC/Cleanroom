package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.struct.StructRegistry;

import java.util.List;

public final class ComponentDesc {
    public final List<FieldDef> fields;

    protected ComponentDesc(List<FieldDef> fields) {
        this.fields = fields;
    }

    public String toString(StructRegistry structRegistry) {
        StringBuilder builder = new StringBuilder();
        builder.append("ComponentDesc{ ");
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
