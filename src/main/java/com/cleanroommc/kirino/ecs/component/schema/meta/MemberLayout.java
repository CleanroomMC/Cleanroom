package com.cleanroommc.kirino.ecs.component.schema.meta;

import java.util.Arrays;
import java.util.List;

public class MemberLayout {
    public final List<String> fieldNames;

    public MemberLayout(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public MemberLayout(String... fieldNames) {
        this.fieldNames = Arrays.asList(fieldNames);
    }
}
