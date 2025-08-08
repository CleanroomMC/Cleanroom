package com.cleanroommc.kirino.ecs.component.scan;

import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;

public record RegisterStructPlan(
        String structName,
        Class<?> structClass,
        MemberLayout memberLayout,
        StructDef structDef
) {
}
