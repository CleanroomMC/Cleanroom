package com.cleanroommc.kirino.utils.reflection;

import org.jspecify.annotations.Nullable;

public final class TypeUtils {
    public static boolean looseTypeCheck(Class<?> clazz1, Class<?> clazz2) {
        if (clazz1.equals(clazz2)) {
            return true;
        } else if (isIntOrWrappedInt(clazz1) && isIntOrWrappedInt(clazz2)) {
            return true;
        } else if (isLongOrWrappedLong(clazz1) && isLongOrWrappedLong(clazz2)) {
            return true;
        } else if (isShortOrWrappedShort(clazz1) && isShortOrWrappedShort(clazz2)) {
            return true;
        } else if (isByteOrWrappedByte(clazz1) && isByteOrWrappedByte(clazz2)) {
            return true;
        } else if (isDoubleOrWrappedDouble(clazz1) && isDoubleOrWrappedDouble(clazz2)) {
            return true;
        } else if (isFloatOrWrappedFloat(clazz1) && isFloatOrWrappedFloat(clazz2)) {
            return true;
        } else if (isCharacterOrWrappedCharacter(clazz1) && isCharacterOrWrappedCharacter(clazz2)) {
            return true;
        } else {
            return isBooleanOrWrappedBoolean(clazz1) && isBooleanOrWrappedBoolean(clazz2);
        }
    }

    public static boolean isPrimitiveOrWrappedPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || isWrappedPrimitive(clazz);
    }

    public static boolean isWrappedPrimitive(Class<?> clazz) {
        return clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Short.class ||
                clazz == Byte.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Character.class ||
                clazz == Boolean.class;
    }

    @Nullable
    public static Class<?> toWrappedPrimitive(Class<?> primitiveClass) {
        return switch (primitiveClass.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "char" -> Character.class;
            case "boolean" -> Boolean.class;
            default -> null;
        };
    }

    @Nullable
    public static Class<?> toPrimitive(Class<?> wrappedPrimitiveClass) {
        if (wrappedPrimitiveClass == Integer.class) {
            return int.class;
        }
        if (wrappedPrimitiveClass == Long.class) {
            return long.class;
        }
        if (wrappedPrimitiveClass == Short.class) {
            return short.class;
        }
        if (wrappedPrimitiveClass == Byte.class) {
            return byte.class;
        }
        if (wrappedPrimitiveClass == Double.class) {
            return double.class;
        }
        if (wrappedPrimitiveClass == Float.class) {
            return float.class;
        }
        if (wrappedPrimitiveClass == Character.class) {
            return char.class;
        }
        if (wrappedPrimitiveClass == Boolean.class) {
            return boolean.class;
        }
        return null;
    }

    public static boolean isIntOrWrappedInt(Class<?> clazz) {
        return clazz.getName().equals("int") || clazz.equals(Integer.class);
    }

    public static boolean isLongOrWrappedLong(Class<?> clazz) {
        return clazz.getName().equals("long") || clazz.equals(Long.class);
    }

    public static boolean isShortOrWrappedShort(Class<?> clazz) {
        return clazz.getName().equals("short") || clazz.equals(Short.class);
    }

    public static boolean isByteOrWrappedByte(Class<?> clazz) {
        return clazz.getName().equals("byte") || clazz.equals(Byte.class);
    }

    public static boolean isDoubleOrWrappedDouble(Class<?> clazz) {
        return clazz.getName().equals("double") || clazz.equals(Double.class);
    }

    public static boolean isFloatOrWrappedFloat(Class<?> clazz) {
        return clazz.getName().equals("float") || clazz.equals(Float.class);
    }

    public static boolean isCharacterOrWrappedCharacter(Class<?> clazz) {
        return clazz.getName().equals("char") || clazz.equals(Character.class);
    }

    public static boolean isBooleanOrWrappedBoolean(Class<?> clazz) {
        return clazz.getName().equals("boolean") || clazz.equals(Boolean.class);
    }
}
