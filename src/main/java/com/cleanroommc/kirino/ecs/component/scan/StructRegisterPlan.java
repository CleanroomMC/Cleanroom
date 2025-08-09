package com.cleanroommc.kirino.ecs.component.scan;

import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;

public record StructRegisterPlan(
        String structName,
        String structClass,
        MemberLayout memberLayout,
        StructDef structDef
) {
}
