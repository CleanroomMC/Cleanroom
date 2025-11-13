package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ScalarConstructor {
    private ScalarConstructor() {
    }

    @Nullable
    public static Object newScalar(@NonNull ScalarType scalarType, @NonNull Object @NonNull ... args) {
        switch (scalarType) {
            case INT -> {
                if (args.length == 1) {
                    if (args[0] instanceof Integer) {
                        return args[0];
                    }
                }
                return null;
            }
            case FLOAT -> {
                if (args.length == 1) {
                    if (args[0] instanceof Float) {
                        return args[0];
                    }
                }
                return null;
            }
            case BOOL -> {
                if (args.length == 1) {
                    if (args[0] instanceof Boolean) {
                        return args[0];
                    }
                }
                return null;
            }
            case VEC2 -> {
                if (args.length == 2) {
                    for (int i = 0; i < args.length; i++) {
                        if (!(args[i] instanceof Float)) {
                            return null;
                        }
                    }
                    return new Vector2f(
                            (float) args[0],
                            (float) args[1]);
                }
                return null;
            }
            case VEC3 -> {
                if (args.length == 3) {
                    for (int i = 0; i < args.length; i++) {
                        if (!(args[i] instanceof Float)) {
                            return null;
                        }
                    }
                    return new Vector3f(
                            (float) args[0],
                            (float) args[1],
                            (float) args[2]);
                }
                return null;
            }
            case VEC4 -> {
                if (args.length == 4) {
                    for (int i = 0; i < args.length; i++) {
                        if (!(args[i] instanceof Float)) {
                            return null;
                        }
                    }
                    return new Vector4f(
                            (float) args[0],
                            (float) args[1],
                            (float) args[2],
                            (float) args[3]);
                }
                return null;
            }
            case MAT3 -> {
                if (args.length == 9) {
                    for (int i = 0; i < args.length; i++) {
                        if (!(args[i] instanceof Float)) {
                            return null;
                        }
                    }
                    return new Matrix3f(
                            (float) args[0],
                            (float) args[1],
                            (float) args[2],
                            (float) args[3],
                            (float) args[4],
                            (float) args[5],
                            (float) args[6],
                            (float) args[7],
                            (float) args[8]);
                }
                return null;
            }
            case MAT4 -> {
                if (args.length == 16) {
                    for (int i = 0; i < args.length; i++) {
                        if (!(args[i] instanceof Float)) {
                            return null;
                        }
                    }
                    return new Matrix4f(
                            (float) args[0],
                            (float) args[1],
                            (float) args[2],
                            (float) args[3],
                            (float) args[4],
                            (float) args[5],
                            (float) args[6],
                            (float) args[7],
                            (float) args[8],
                            (float) args[9],
                            (float) args[10],
                            (float) args[11],
                            (float) args[12],
                            (float) args[13],
                            (float) args[14],
                            (float) args[15]);
                }
                return null;
            }
        }
        return null;
    }
}
