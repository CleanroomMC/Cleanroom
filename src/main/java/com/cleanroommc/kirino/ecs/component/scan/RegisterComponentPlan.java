package com.cleanroommc.kirino.ecs.component.scan;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;

public record RegisterComponentPlan(
        String componentName,
        Class<?> componentClass,
        MemberLayout memberLayout,
        String[] fieldTypeNames
) {
}
