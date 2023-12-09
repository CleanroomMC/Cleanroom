package com.cleanroommc.hackery;

import com.cleanroommc.hackery.enums.EnumHackery;
import jdk.internal.misc.Unsafe;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLLog;

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
            putHelper(field.getType(), unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
        } else {
            putHelper(field.getType(), owner, unsafe.objectFieldOffset(field), value);
        }
    }

    private static void putHelper(Class<?> clazz, Object owner, long offset, Object value) {
        if (clazz.equals(int.class)) {
            unsafe.putInt(owner, offset, (int) value);
            return;
        }
        if (clazz.equals(byte.class)) {
            unsafe.putByte(owner, offset, (byte) value);
            return;
        }
        if (clazz.equals(long.class)) {
            unsafe.putLong(owner, offset, (long) value);
            return;
        }
        if (clazz.equals(float.class)) {
            unsafe.putFloat(owner, offset, (float) value);
            return;
        }
        if (clazz.equals(double.class)) {
            unsafe.putDouble(owner, offset, (double) value);
            return;
        }
        if (clazz.equals(boolean.class)) {
            unsafe.putBoolean(owner, offset, (boolean) value);
            return;
        }
        if (clazz.equals(char.class)) {
            unsafe.putChar(owner, offset, (char) value);
            return;
        }
        unsafe.putObject(owner, offset, value);
    }

    public static Object getField(Field field, Object owner) throws IllegalAccessException {
        if (Modifier.isStatic(field.getModifiers())) {
            unsafe.ensureClassInitialized(field.getDeclaringClass());
            return getHelper(field.getType(), unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        } else {
            return getHelper(field.getType(), owner, unsafe.objectFieldOffset(field));
        }
    }

    private static Object getHelper(Class<?> clazz, Object owner, long offset) {
        if (clazz.equals(int.class)) {
            return unsafe.getInt(owner, offset);
        }
        if (clazz.equals(byte.class)) {
            return unsafe.getByte(owner, offset);
        }
        if (clazz.equals(long.class)) {
            return unsafe.getLong(owner, offset);
        }
        if (clazz.equals(float.class)) {
            return unsafe.getFloat(owner, offset);
        }
        if (clazz.equals(double.class)) {
            return unsafe.getDouble(owner, offset);
        }
        if (clazz.equals(boolean.class)) {
            return unsafe.getBoolean(owner, offset);
        }
        if (clazz.equals(char.class)) {
            return unsafe.getChar(owner, offset);
        }
        return unsafe.getObject(owner, offset);
    }

}
