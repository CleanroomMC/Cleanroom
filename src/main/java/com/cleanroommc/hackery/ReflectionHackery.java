package com.cleanroommc.hackery;

import net.lenni0451.reflect.Fields;
import sun.misc.Unsafe;
import net.minecraft.launchwrapper.LaunchClassLoader;
import top.outlands.foundation.boot.UnsafeHolder;
import zone.rong.imaginebreaker.ImagineBreaker;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.net.URL;
import java.util.function.Predicate;

public final class ReflectionHackery {

    private static final Field field$modifiers;
    public static Unsafe unsafe = UnsafeHolder.UNSAFE;

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
        field$modifiers.setInt(field, field.getModifiers() & ~modifierFlag);
    }

    public static URL[] getURL(ClassLoader loader) {
        try{
            Field UCP = LaunchClassLoader.class.getClassLoader().getClass().getSuperclass().getDeclaredField("ucp");
            UCP.setAccessible(true);
            Object urls = UCP.get(loader);
            Class<?> urlClassPath = Class.forName("jdk.internal.loader.URLClassPath");
            Method get = urlClassPath.getMethod("getURLs");
            return (URL[]) get.invoke(urls);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            return new URL[0];
        }
    }



    public static void setField(Field field, Object owner, Object value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.set(owner, field, value);
    }

    public static void setBooleanField(Field field, Object owner, boolean value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setBoolean(owner, field, value);
    }

    public static void setByteField(Field field, Object owner, byte value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setByte(owner, field, value);
    }

    public static void setCharField(Field field, Object owner, char value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setChar(owner, field, value);
    }

    public static void setShortField(Field field, Object owner, short value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setShort(owner, field, value);
    }

    public static void setIntField(Field field, Object owner, int value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setInt(owner, field, value);
    }

    public static void setLongField(Field field, Object owner, long value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setLong(owner, field, value);
    }

    public static void setFloatField(Field field, Object owner, float value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setFloat(owner, field, value);
    }

    public static void setDoubleField(Field field, Object owner, double value) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        Fields.setDouble(owner, field, value);
    }

    public static Object getField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.get(owner, field);
    }

    public static boolean getBooleanField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getBoolean(owner, field);
    }

    public static byte getByteField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getByte(owner, field);
    }

    public static char getCharField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getChar(owner, field);
    }

    public static short getShortField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getShort(owner, field);
    }

    public static int getIntField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getInt(owner, field);
    }

    public static long getLongField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getLong(owner, field);
    }

    public static float getFloatField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getFloat(owner, field);
    }

    public static double getDoubleField(Field field, Object owner) throws IllegalAccessException {
        ImagineBreaker.lookup().ensureInitialized(field.getDeclaringClass());
        return Fields.getDouble(owner, field);
    }

}
