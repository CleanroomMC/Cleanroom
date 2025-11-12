package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FlattenedField;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public final class ComponentDescFlattened {
    public final ImmutableList<FlattenedField> fields;

    ComponentDescFlattened(ComponentDesc componentDesc, FieldRegistry fieldRegistry) {
        List<FlattenedField> fields = new ArrayList<>();
        for (FieldDef field : componentDesc.fields) {
            fields.add(fieldRegistry.flatten(field));
        }
        this.fields = ImmutableList.copyOf(fields);
    }

    public int getUnitCount() {
        int unitCount = 0;
        for (FlattenedField field : fields) {
            unitCount += field.getUnitCount();
        }
        return unitCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ComponentDescFlattened{ ");
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
