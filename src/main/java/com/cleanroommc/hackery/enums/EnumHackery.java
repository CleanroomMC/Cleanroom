package com.cleanroommc.hackery.enums;

import com.cleanroommc.hackery.ReflectionHackery;
import jdk.internal.reflect.ReflectionFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class EnumHackery {

    private static final ReflectionFactory factory = ReflectionFactory.getReflectionFactory();

    private static final Field class$enumConstants, class$enumConstantDirectory, class$enumVars;
    private static final Method constructorAccessor$newInstance;

    private static final Class[] head = {String.class, int.class};

    static {
        Field constructorAccessor;
        Field enumConstants;
        Field enumConstantDirectory;
        Method newInstance;
        try {
            constructorAccessor = ReflectionHackery.deepSearchForField(Constructor.class, field -> "constructorAccessor".equals(field.getName()), true);

            enumConstants = Class.class.getDeclaredField("enumConstants");
            enumConstants.setAccessible(true);

            enumConstantDirectory = Class.class.getDeclaredField("enumConstantDirectory");
            enumConstantDirectory.setAccessible(true);

            newInstance = constructorAccessor.getType().getMethod("newInstance", Object[].class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        Field enumVars = null; // OpenJ9 only
        try {
            enumVars = Class.class.getDeclaredField("enumVars");
            enumVars.setAccessible(true);
        } catch (ReflectiveOperationException ignored) { } // We're probably not on OpenJ9

        class$enumConstants = enumConstants;
        class$enumConstantDirectory = enumConstantDirectory;
        class$enumVars = enumVars;
        constructorAccessor$newInstance = newInstance;
    }

    public static <T extends Enum<T>> T addEnumEntry(Class<T> enumClass, String enumName) {
        return addEnumEntry(enumClass, enumName, new Class[0], new Object[0]);
    }


    public static <T extends Enum<T>> T addEnumEntry(Class<T> enumClass, String enumName, Class<?>[] parameterTypes, Object[] parameterValues) {
        if (parameterTypes.length != parameterValues.length) {
            throw new IllegalArgumentException("The amount of parameter types must be the same as the parameter values.");
        }
        try {
            Constructor<T> constructor = enumClass.getDeclaredConstructor(ArrayUtils.addAll(head, parameterTypes));
            constructor.setAccessible(true);
            MethodHandle handle = MethodHandles.lookup().unreflectConstructor(constructor);
            Method m = enumClass.getMethod("values");
            Object o = m.invoke(enumClass);
            //System.out.println(Arrays.toString(parameterValues));
            return (T) handle.invokeWithArguments(ArrayUtils.addAll(new Object[]{enumName, ((Object[])o).length}, parameterValues));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }/*
        try {
            // Constructors are compiled to have additional prefixed parameters, a [String name] and an [int ordinal] before all the other parameters
            Class<?>[] prefixParameterTypes = new Class<?>[] { String.class, int.class };
            parameterTypes = parameterTypes.length == 0 ? prefixParameterTypes : ArrayUtils.addAll(prefixParameterTypes, parameterTypes);
            Constructor<T> constructor = enumClass.getDeclaredConstructor(parameterTypes);
            // ctor.newInstance() results in: throw new IllegalArgumentException("Cannot reflectively create enum objects");
            // Java 19: Constructor#acquireConstructorAccessor results in: throw new IllegalArgumentException("Cannot reflectively create enum objects");
            Object constructorAccessor = factory.getConstructorAccessor(constructor);
            T[] currentConstants = enumClass.getEnumConstants();
            int nextOrdinal = currentConstants.length;
            Object[] prefixParameterValues = new Object[] { enumName, nextOrdinal };
            parameterValues = parameterValues.length == 0 ? prefixParameterValues : ArrayUtils.addAll(prefixParameterValues, parameterValues);
            T enumEntry = (T) constructorAccessor$newInstance.invoke(constructorAccessor, new Object[] { parameterValues });
            T[] newConstants = ArrayUtils.add(currentConstants, enumEntry);
            Field enum$values = enumClass.getDeclaredField("$VALUES");
            enum$values.setAccessible(true);
            ReflectionHackery.stripFieldOfFinalModifier(enum$values);
            enum$values.set(null, newConstants);
            resetEnumRelatedCaches(enumClass);
            return enumEntry;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }*/
    }

    private static <T extends Enum<T>> void resetEnumRelatedCaches(Class<T> enumClass) throws ReflectiveOperationException {
        class$enumConstants.set(enumClass, null);
        class$enumConstantDirectory.set(enumClass, null);
        if (class$enumVars != null) {
            class$enumVars.set(enumClass, null);
        }
    }

    private EnumHackery() { }

}
