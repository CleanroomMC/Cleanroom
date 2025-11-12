package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;

/**
 * A {@link ScalarType} is not by definition a scalar but more like built-in primitive types.
 * The opposite concept is a struct ({@link StructDef}).
 */
public enum ScalarType {
    INT,
    FLOAT,
    BOOL,
    VEC2,
    VEC3,
    VEC4,
    MAT3,
    MAT4
}
