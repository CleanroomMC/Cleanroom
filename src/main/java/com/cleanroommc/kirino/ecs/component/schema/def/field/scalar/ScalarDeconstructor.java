package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.google.common.base.Preconditions;
import org.joml.*;
import org.jspecify.annotations.NonNull;

public final class ScalarDeconstructor {
    private ScalarDeconstructor() {
    }

    public static @NonNull Object[] flattenScalar(@NonNull ScalarType scalarType, @NonNull Object scalarInstance) {
        Preconditions.checkNotNull(scalarType);
        Preconditions.checkNotNull(scalarInstance);

        switch (scalarType) {
            case INT -> {
                Preconditions.checkArgument(scalarInstance instanceof Integer,
                        "Expected a java.lang.Integer. Got %s instead.", scalarInstance.getClass().getName());

                return new Object[]{scalarInstance};
            }
            case FLOAT -> {
                Preconditions.checkArgument(scalarInstance instanceof Float,
                        "Expected a java.lang.Float. Got %s instead.", scalarInstance.getClass().getName());

                return new Object[]{scalarInstance};
            }
            case BOOL -> {
                Preconditions.checkArgument(scalarInstance instanceof Boolean,
                        "Expected a java.lang.Boolean. Got %s instead.", scalarInstance.getClass().getName());

                return new Object[]{scalarInstance};
            }
            case VEC2 -> {
                Preconditions.checkArgument(scalarInstance instanceof Vector2f,
                        "Expected a org.joml.Vector2f. Got %s instead.", scalarInstance.getClass().getName());

                Vector2f vector2f = (Vector2f) scalarInstance;
                return new Object[]{vector2f.x, vector2f.y};
            }
            case VEC3 -> {
                Preconditions.checkArgument(scalarInstance instanceof Vector3f,
                        "Expected a org.joml.vector3f. Got %s instead.", scalarInstance.getClass().getName());

                Vector3f vector3f = (Vector3f) scalarInstance;
                return new Object[]{vector3f.x, vector3f.y, vector3f.z};
            }
            case VEC4 -> {
                Preconditions.checkArgument(scalarInstance instanceof Vector4f,
                        "Expected a org.joml.vector4f. Got %s instead.", scalarInstance.getClass().getName());

                Vector4f vector4f = (Vector4f) scalarInstance;
                return new Object[]{vector4f.x, vector4f.y, vector4f.z, vector4f.w};
            }
            case MAT3 -> {
                Preconditions.checkArgument(scalarInstance instanceof Matrix3f,
                        "Expected a org.joml.Matrix3f. Got %s instead.", scalarInstance.getClass().getName());

                Matrix3f matrix3f = (Matrix3f) scalarInstance;
                return new Object[]{matrix3f.m00, matrix3f.m01, matrix3f.m02, matrix3f.m10, matrix3f.m11, matrix3f.m12, matrix3f.m20, matrix3f.m21, matrix3f.m22};
            }
            case MAT4 -> {
                Preconditions.checkArgument(scalarInstance instanceof Matrix4f,
                        "Expected a org.joml.Matrix4f. Got %s instead.", scalarInstance.getClass().getName());

                Matrix4f matrix4f = (Matrix4f) scalarInstance;
                return new Object[]{matrix4f.m00(), matrix4f.m01(), matrix4f.m02(), matrix4f.m03(), matrix4f.m10(), matrix4f.m11(), matrix4f.m12(), matrix4f.m13(), matrix4f.m20(), matrix4f.m21(), matrix4f.m22(), matrix4f.m23(), matrix4f.m30(), matrix4f.m31(), matrix4f.m32(), matrix4f.m33()};
            }
        }

        throw new IllegalStateException("Invalid scalar type."); // impossible
    }
}
