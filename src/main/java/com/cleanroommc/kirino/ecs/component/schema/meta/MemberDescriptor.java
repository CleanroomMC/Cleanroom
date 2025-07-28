package com.cleanroommc.kirino.ecs.component.schema.meta;

import java.util.Arrays;
import java.util.List;

public class MemberDescriptor {
    public final List<String> fieldNames;

    public MemberDescriptor(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public MemberDescriptor(String... fieldNames) {
        this.fieldNames = Arrays.asList(fieldNames);
    }
}
