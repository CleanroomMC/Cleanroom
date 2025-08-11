package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import org.joml.*;

public final class ScalarDeconstructor {
    private ScalarDeconstructor() {
    }

    public static Object[] flattenScalar(ScalarType scalarType, Object scalarInstance) {
        if (scalarInstance == null) {
            throw new IllegalStateException("Scalar instance is null.");
        }
        switch (scalarType) {
            case INT -> {
                if (!(scalarInstance instanceof Integer)) {
                    throw new IllegalStateException("Expected a java.lang.Integer. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{scalarInstance};
            }
            case FLOAT -> {
                if (!(scalarInstance instanceof Float)) {
                    throw new IllegalStateException("Expected a java.lang.Float. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{scalarInstance};
            }
            case BOOL -> {
                if (!(scalarInstance instanceof Boolean)) {
                    throw new IllegalStateException("Expected a java.lang.Boolean. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{scalarInstance};
            }
            case VEC2 -> {
                if (!(scalarInstance instanceof Vector2f vector2f)) {
                    throw new IllegalStateException("Expected a org.joml.Vector2f. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{vector2f.x, vector2f.y};
            }
            case VEC3 -> {
                if (!(scalarInstance instanceof Vector3f vector3f)) {
                    throw new IllegalStateException("Expected a org.joml.vector3f. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{vector3f.x, vector3f.y, vector3f.z};
            }
            case VEC4 -> {
                if (!(scalarInstance instanceof Vector4f vector4f)) {
                    throw new IllegalStateException("Expected a org.joml.vector4f. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{vector4f.x, vector4f.y, vector4f.z, vector4f.w};
            }
            case MAT3 -> {
                if (!(scalarInstance instanceof Matrix3f matrix3f)) {
                    throw new IllegalStateException("Expected a org.joml.Matrix3f. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{matrix3f.m00, matrix3f.m01, matrix3f.m02, matrix3f.m10, matrix3f.m11, matrix3f.m12, matrix3f.m20, matrix3f.m21, matrix3f.m22};
            }
            case MAT4 -> {
                if (!(scalarInstance instanceof Matrix4f matrix4f)) {
                    throw new IllegalStateException("Expected a org.joml.Matrix4f. Got " + scalarInstance.getClass().getName() + ".");
                }
                return new Object[]{matrix4f.m00(), matrix4f.m01(), matrix4f.m02(), matrix4f.m03(), matrix4f.m10(), matrix4f.m11(), matrix4f.m12(), matrix4f.m13(), matrix4f.m20(), matrix4f.m21(), matrix4f.m22(), matrix4f.m23(), matrix4f.m30(), matrix4f.m31(), matrix4f.m32(), matrix4f.m33()};
            }
        }
        throw new IllegalStateException("Invalid scalar type.");
    }
}
