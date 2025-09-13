package com.cleanroommc.kirino.utils.reflection;

import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ReflectionUtils {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    //<editor-fold desc="find field">
    @Nullable
    public static Field findDeclaredField(Class<?> clazz, String fieldName, String obfFieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getDeclaredField(obfFieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return field;
    }

    @Nullable
    public static Field findDeclaredField(Class<?> clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }

    @Nullable
    public static Field findField(Class<?> clazz, String fieldName, String obfFieldName) {
        Field field = null;
        try {
            field = clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(obfFieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return field;
    }

    @Nullable
    public static Field findField(Class<?> clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getField(fieldName);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }
    //</editor-fold>

    //<editor-fold desc="unreflect">
    private static Object unreflectGetter(Field field) {
        if (field.getType().isPrimitive()) {
            return unreflectGetterHelper(field, TypeUtils.toWrappedPrimitive(field.getType()), field.getDeclaringClass());
        } else {
            return unreflectGetterHelper(field, field.getType(), field.getDeclaringClass());
        }
    }

    @SuppressWarnings("all")
    @Nullable
    private static <TReturn, TOwner> Object unreflectGetterHelper(Field field, Class<TReturn> clazz1, Class<TOwner> clazz2) {
        field.setAccessible(true);
        Object getter = null;
        try {
            MethodHandle handle = LOOKUP.unreflectGetter(field);
            if (Modifier.isStatic(field.getModifiers())) {
                getter = new Supplier<TReturn>() {
                    @Override
                    public TReturn get() {
                        try {
                            return (TReturn) handle.invoke();
                        } catch (Throwable e) {
                            return null;
                        }
                    }
                };
            } else {
                getter = new Function<TOwner, TReturn>() {
                    @Override
                    public TReturn apply(TOwner tOwner) {
                        try {
                            return (TReturn) handle.invoke(tOwner);
                        } catch (Throwable e) {
                            return null;
                        }
                    }
                };
            }
        } catch (Exception ignored) {
        }
        return getter;
    }

    private static Object unreflectSetter(Field field) {
        if (field.getType().isPrimitive()) {
            return unreflectSetterHelper(field, TypeUtils.toWrappedPrimitive(field.getType()), field.getDeclaringClass());
        } else {
            return unreflectSetterHelper(field, field.getType(), field.getDeclaringClass());
        }
    }

    @SuppressWarnings("all")
    @Nullable
    private static <TField, TOwner> Object unreflectSetterHelper(Field field, Class<TField> clazz1, Class<TOwner> clazz2) {
        field.setAccessible(true);
        Object setter = null;
        try {
            MethodHandle handle = LOOKUP.unreflectSetter(field);
            if (Modifier.isStatic(field.getModifiers())) {
                setter = new Consumer<TField>() {
                    @Override
                    public void accept(TField tField) {
                        try {
                            handle.invoke(tField);
                        } catch (Throwable e) {
                        }
                    }
                };
            } else {
                setter = new BiConsumer<TOwner, TField>() {
                    @Override
                    public void accept(TOwner tOwner, TField tField) {
                        try {
                            handle.invoke(tOwner, tField);
                        } catch (Throwable e) {
                        }
                    }
                };
            }
        } catch (Exception ignored) {
        }
        return setter;
    }
    //</editor-fold>

    //<editor-fold desc="get getter">
    /**
     * The target field getter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name
     * @return A {@link Supplier} or {@link Function} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getDeclaredFieldGetter(Class<?> clazz, String fieldName, String obfFieldName) {
        Field field = findDeclaredField(clazz, fieldName, obfFieldName);
        if (field == null) {
            return null;
        }
        return unreflectGetter(field);
    }

    /**
     * The target field getter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @return A {@link Supplier} or {@link Function} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getDeclaredFieldGetter(Class<?> clazz, String fieldName) {
        Field field = findDeclaredField(clazz, fieldName);
        if (field == null) {
            return null;
        }
        return unreflectGetter(field);
    }

    /**
     * The target field getter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name
     * @return A {@link Supplier} or {@link Function} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getFieldGetter(Class<?> clazz, String fieldName, String obfFieldName) {
        Field field = findField(clazz, fieldName, obfFieldName);
        if (field == null) {
            return null;
        }
        return unreflectGetter(field);
    }

    /**
     * The target field getter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @return A {@link Supplier} or {@link Function} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getFieldGetter(Class<?> clazz, String fieldName) {
        Field field = findField(clazz, fieldName);
        if (field == null) {
            return null;
        }
        return unreflectGetter(field);
    }
    //</editor-fold>

    //<editor-fold desc="get setter">
    /**
     * The target field setter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name
     * @return A {@link Consumer} or {@link BiConsumer} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getDeclaredFieldSetter(Class<?> clazz, String fieldName, String obfFieldName) {
        Field field = findDeclaredField(clazz, fieldName, obfFieldName);
        if (field == null) {
            return null;
        }
        return unreflectSetter(field);
    }

    /**
     * The target field setter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @return A {@link Consumer} or {@link BiConsumer} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getDeclaredFieldSetter(Class<?> clazz, String fieldName) {
        Field field = findDeclaredField(clazz, fieldName);
        if (field == null) {
            return null;
        }
        return unreflectSetter(field);
    }

    /**
     * The target field setter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name
     * @return A {@link Consumer} or {@link BiConsumer} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getFieldSetter(Class<?> clazz, String fieldName, String obfFieldName) {
        Field field = findField(clazz, fieldName, obfFieldName);
        if (field == null) {
            return null;
        }
        return unreflectSetter(field);
    }

    /**
     * The target field setter will be unreflected and then returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @return A {@link Consumer} or {@link BiConsumer} depends on the <code>static</code> modifier
     */
    @Nullable
    public static Object getFieldSetter(Class<?> clazz, String fieldName) {
        Field field = findField(clazz, fieldName);
        if (field == null) {
            return null;
        }
        return unreflectSetter(field);
    }
    //</editor-fold>

    //<editor-fold desc="find method">
    @Nullable
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, String obfMethodName, Class<?>... params) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getDeclaredMethod(obfMethodName, params);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return method;
    }

    @Nullable
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... params) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException ignored) {
        }
        return method;
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, String obfMethodName, Class<?>... params) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(obfMethodName, params);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return method;
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... params) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException ignored) {
        }
        return method;
    }
    //</editor-fold>

    //<editor-fold desc="get method handle">
    @Nullable
    public static MethodHandle getDeclaredMethod(Class<?> clazz, String methodName, String obfMethodName, Class<?>... params) {
        Method method = findDeclaredMethod(clazz, methodName, obfMethodName, params);
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    public static MethodHandle getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... params) {
        Method method = findDeclaredMethod(clazz, methodName, params);
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    public static MethodHandle getMethod(Class<?> clazz, String methodName, String obfMethodName, Class<?>... params) {
        Method method = findMethod(clazz, methodName, obfMethodName, params);
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    public static MethodHandle getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        Method method = findMethod(clazz, methodName, params);
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="find constructor">
    @Nullable
    public static <T> Constructor<T> findDeclaredConstructor(Class<T> clazz, Class<?>... params) {
        Constructor<T> ctor = null;
        try {
            ctor = clazz.getDeclaredConstructor(params);
        } catch (NoSuchMethodException ignored) {
        }
        return ctor;
    }

    @Nullable
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... params) {
        Constructor<T> ctor = null;
        try {
            ctor = clazz.getConstructor(params);
        } catch (NoSuchMethodException ignored) {
        }
        return ctor;
    }
    //</editor-fold>

    //<editor-fold desc="get constructor handle">
    @Nullable
    public static MethodHandle getDeclaredConstructor(Class<?> clazz, Class<?>... params) {
        Constructor<?> ctor = findDeclaredConstructor(clazz, params);
        if (ctor == null) {
            return null;
        }
        ctor.setAccessible(true);
        try {
            return LOOKUP.unreflectConstructor(ctor);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    public static MethodHandle getConstructor(Class<?> clazz, Class<?>... params) {
        Constructor<?> ctor = findConstructor(clazz, params);
        if (ctor == null) {
            return null;
        }
        ctor.setAccessible(true);
        try {
            return LOOKUP.unreflectConstructor(ctor);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    //</editor-fold>
}
