package com.cleanroommc.kirino.ecs.component.scan;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;

public record ComponentRegisterPlan(
        String componentName,
        String componentClass,
        MemberLayout memberLayout,
        String[] fieldTypeNames
) {
}
