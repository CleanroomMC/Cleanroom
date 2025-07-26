package com.cleanroommc.kirino.ecs.component.field.scalar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum FlattenedScalarType {
    INT,
    FLOAT,
    BOOL;

    public static List<FlattenedScalarType> flatten(ScalarType scalarType) {
        return switch (scalarType) {
            case INT -> Collections.singletonList(INT);
            case FLOAT -> Collections.singletonList(FLOAT);
            case BOOL -> Collections.singletonList(BOOL);
            case VEC2 -> Arrays.asList(FLOAT, FLOAT);
            case VEC3 -> Arrays.asList(FLOAT, FLOAT, FLOAT);
            case VEC4 -> Arrays.asList(FLOAT, FLOAT, FLOAT, FLOAT);
            case MAT3 -> Arrays.asList(FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT);
            case MAT4 -> Arrays.asList(FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT);
        };
    }
}
