package com.cleanroommc.hackery;

import jdk.internal.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.function.Predicate;

@SuppressWarnings("removal")
public final class ReflectionHackery {

    private static final Field field$modifiers;
    public static Unsafe unsafe;

    static {
        Field modifiers;
        try {
            modifiers = Field.class.getDeclaredField("modifiers"); // Try the standard way, this should always work with ImagineBreaker
        } catch (ReflectiveOperationException e) {
            try {
                modifiers = deepSearchForField(Field.class, field -> "modifiers".equals(field.getName()), false); // Reliable Fallback
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
        field$modifiers = modifiers;
        field$modifiers.setAccessible(true);
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (Exception e) {}
    }

    // Sensitive fields such as "modifiers" are only query-able via the native getDeclaredFields0 call
    public static @Nullable Field deepSearchForField(Class<?> clazz, Predicate<Field> search, boolean setAccessible) throws ReflectiveOperationException {
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class); // TODO: cache?
        getDeclaredFields0.setAccessible(true);
        for (Field declaredField : (Field[]) getDeclaredFields0.invoke(clazz, false)) {
            if (search.test(declaredField)) {
                if (setAccessible) {
                    declaredField.setAccessible(true);
                }
                return declaredField;
            }
        }
        return null;
    }

    public static void stripFieldOfFinalModifier(Field field) throws IllegalAccessException {
        stripFieldOfModifier(field, Modifier.FINAL);
    }

    public static void stripFieldOfModifier(Field field, int modifierFlag) throws IllegalAccessException {
        field$modifiers.setInt(field, field.getModifiers() & ~modifierFlag);
    }

    public static void setField(Field field, Object owner, Object value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            putHelper(field.getType(), unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            putHelper(field.getType(), owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setBooleanField(Field field, Object owner, boolean value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putBoolean(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putBoolean(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setByteField(Field field, Object owner, byte value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putByte(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putByte(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setCharField(Field field, Object owner, char value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putChar(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putChar(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setShortField(Field field, Object owner, short value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putShort(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putShort(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setIntField(Field field, Object owner, int value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putInt(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putInt(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setLongField(Field field, Object owner, long value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putLong(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putLong(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setFloatField(Field field, Object owner, float value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putFloat(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putFloat(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static void setDoubleField(Field field, Object owner, double value) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            unsafe.putDouble(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            unsafe.putDouble(owner, unsafe.objectFieldOffset(field), value);
        }
    }

    public static Object getField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return getHelper(field.getType(), unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return getHelper(field.getType(), owner, unsafe.objectFieldOffset(field));
        }
    }

    public static boolean getBooleanField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getBoolean(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getBoolean(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static byte getByteField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getByte(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getByte(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static char getCharField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getChar(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getChar(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static short getShortField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getShort(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getShort(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static int getIntField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getInt(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getInt(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static long getLongField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getLong(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getLong(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static float getFloatField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getFloat(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getFloat(owner, unsafe.objectFieldOffset(field));
        }
    }

    public static double getDoubleField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return unsafe.getDouble(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return unsafe.getDouble(owner, unsafe.objectFieldOffset(field));
        }
    }

    private static void putHelper(Class<?> clazz, Object owner, long offset, Object value) {
        if (clazz == Integer.TYPE) {
            unsafe.putInt(owner, offset, (int) value);
        } else if (clazz == Byte.TYPE) {
            unsafe.putByte(owner, offset, (byte) value);
        } else if (clazz == Long.TYPE) {
            unsafe.putLong(owner, offset, (long) value);
        } else if (clazz == Float.TYPE) {
            unsafe.putFloat(owner, offset, (float) value);
        } else if (clazz == Double.TYPE) {
            unsafe.putDouble(owner, offset, (double) value);
        } else if (clazz == Boolean.TYPE) {
            unsafe.putBoolean(owner, offset, (boolean) value);
        } else if (clazz == Character.TYPE) {
            unsafe.putChar(owner, offset, (char) value);
        } else {
            unsafe.putObject(owner, offset, value);
        }
    }

    private static Object getHelper(Class<?> clazz, Object owner, long offset) {
        if (clazz == Integer.TYPE) {
            return unsafe.getInt(owner, offset);
        } else if (clazz == Byte.TYPE) {
            return unsafe.getByte(owner, offset);
        } else if (clazz == Long.TYPE) {
            return unsafe.getLong(owner, offset);
        } else if (clazz == Float.TYPE) {
            return unsafe.getFloat(owner, offset);
        } else if (clazz == Double.TYPE) {
            return unsafe.getDouble(owner, offset);
        } else if (clazz == Boolean.TYPE) {
            return unsafe.getBoolean(owner, offset);
        } else if (clazz == Character.TYPE) {
            return unsafe.getChar(owner, offset);
        } else {
            return unsafe.getObject(owner, offset);
        }
    }

}
