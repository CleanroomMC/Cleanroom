package com.cleanroommc.kirino.ecs.component.schema.meta;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class MemberLayout {
    public final ImmutableList<String> fieldNames;

    public MemberLayout(List<String> fieldNames) {
        this.fieldNames = ImmutableList.copyOf(fieldNames);
    }

    public MemberLayout(String... fieldNames) {
        this.fieldNames = ImmutableList.copyOf(fieldNames);
    }
}
