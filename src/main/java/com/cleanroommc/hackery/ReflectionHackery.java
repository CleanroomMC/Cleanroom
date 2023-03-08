package com.cleanroommc.hackery;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
                throw new RuntimeException(ex);
            }
        }
        field$modifiers = modifiers;
        field$modifiers.setAccessible(true);
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

}
