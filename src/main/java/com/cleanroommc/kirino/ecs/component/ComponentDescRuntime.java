package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.field.FlattenedField;

import java.util.ArrayList;
import java.util.List;

public final class ComponentDescRuntime {
    public final String name;
    public final List<FlattenedField> fields;

    protected ComponentDescRuntime(String name, ComponentDesc componentDesc, FieldRegistry fieldRegistry) {
        this.name = name;
        fields = new ArrayList<>();
        for (FieldDef field : componentDesc.fields) {
            fields.add(fieldRegistry.flatten(field));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ComponentDescRuntime{ name=").append(name).append(", fields=");
        for (int i = 0; i < fields.size(); i++) {
            FlattenedField field = fields.get(i);
            if (i == fields.size() - 1){
                builder.append(field.toString());
            } else {
                builder.append(field.toString()).append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
