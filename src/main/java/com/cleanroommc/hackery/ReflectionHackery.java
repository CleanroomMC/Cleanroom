package com.cleanroommc.hackery;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.Fields;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.function.Predicate;

public final class ReflectionHackery {

    private static final Field field$modifiers;

    static {
        Field modifiers;
        try {
            modifiers = Field.class.getDeclaredField("modifiers"); // Try the standard way, this should always work with ImagineBreaker
        } catch (ReflectiveOperationException e) {
            try {
                modifiers = deepSearchForField(Field.class, field -> "modifiers".equals(field.getName()), false); // Reliable Fallback
            } catch (ReflectiveOperationException ex) {
                try {
                    modifiers = Fields.getDeclaredField(Field.class, "modifiers");
                } catch (Exception exception) {
                    throw new RuntimeException(ex);
                }
            }
        }
        field$modifiers = modifiers;
        field$modifiers.setAccessible(true);
    }

    // Sensitive fields such as "modifiers" are only query-able via the native getDeclaredFields0 call
    public static @Nullable Field deepSearchForField(Class<?> clazz, Predicate<Field> search, boolean setAccessible) throws ReflectiveOperationException {
        Method getDeclaredFields0;
        if (System.getProperty("java.vendor").contains("IBM")) {
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFieldsImpl");
            getDeclaredFields0.setAccessible(true);
            for (Field declaredField : (Field[]) getDeclaredFields0.invoke(clazz)) {
                if (search.test(declaredField)) {
                    if (setAccessible) {
                        declaredField.setAccessible(true);
                    }
                    return declaredField;
                }
            }
        } else {
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class); // TODO: cache?
            getDeclaredFields0.setAccessible(true);
            for (Field declaredField : (Field[]) getDeclaredFields0.invoke(clazz, false)) {
                if (search.test(declaredField)) {
                    if (setAccessible) {
                        declaredField.setAccessible(true);
                    }
                    return declaredField;
                }
            }
        }
        return null;
    }

    public static void stripFieldOfFinalModifier(Field field) throws IllegalAccessException {
        stripFieldOfModifier(field, Modifier.FINAL);
    }

    public static void stripFieldOfModifier(Field field, int modifierFlag) throws IllegalAccessException {
        Fields.setInt(field, field$modifiers, field.getModifiers() & ~modifierFlag);
    }

    public static void setField(Field field, Object owner, Object value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.set(owner, field, value);
    }

    public static void setBooleanField(Field field, Object owner, boolean value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setBoolean(owner, field, value);
    }

    public static void setByteField(Field field, Object owner, byte value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setByte(owner, field, value);
    }

    public static void setCharField(Field field, Object owner, char value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setChar(owner, field, value);
    }

    public static void setShortField(Field field, Object owner, short value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setShort(owner, field, value);
    }

    public static void setIntField(Field field, Object owner, int value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setInt(owner, field, value);
    }

    public static void setLongField(Field field, Object owner, long value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setLong(owner, field, value);
    }

    public static void setFloatField(Field field, Object owner, float value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setFloat(owner, field, value);
    }

    public static void setDoubleField(Field field, Object owner, double value) {
        Classes.ensureInitialized(field.getDeclaringClass());
        Fields.setDouble(owner, field, value);
    }

    public static Object getField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.get(owner, field);
    }

    public static boolean getBooleanField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getBoolean(owner, field);
    }

    public static byte getByteField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getByte(owner, field);
    }

    public static char getCharField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getChar(owner, field);
    }

    public static short getShortField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getShort(owner, field);
    }

    public static int getIntField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getInt(owner, field);
    }

    public static long getLongField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getLong(owner, field);
    }

    public static float getFloatField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getFloat(owner, field);
    }

    public static double getDoubleField(Field field, Object owner) {
        Classes.ensureInitialized(field.getDeclaringClass());
        return Fields.getDouble(owner, field);
    }

}
