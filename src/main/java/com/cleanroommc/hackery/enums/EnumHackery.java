package com.cleanroommc.hackery.enums;

import com.cleanroommc.hackery.ReflectionHackery;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class EnumHackery {

    private static final Field constructor$constructorAccessor, class$enumConstants, class$enumConstantDirectory, class$enumVars;
    private static final Method constructor$acquireConstructorAccessor, constructorAccessor$newInstance;

    static {
        Field constructorAccessor;
        Field enumConstants;
        Field enumConstantDirectory;
        Method acquireConstructorAccessor;
        Method newInstance;
        try {
            constructorAccessor = ReflectionHackery.deepSearchForField(Constructor.class, field -> "constructorAccessor".equals(field.getName()), true);

            enumConstants = Class.class.getDeclaredField("enumConstants");
            enumConstants.setAccessible(true);

            enumConstantDirectory = Class.class.getDeclaredField("enumConstantDirectory");
            enumConstantDirectory.setAccessible(true);

            acquireConstructorAccessor = Constructor.class.getDeclaredMethod("acquireConstructorAccessor");
            acquireConstructorAccessor.setAccessible(true);

            newInstance = constructorAccessor.getType().getMethod("newInstance", Object[].class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        Field enumVars = null; // OpenJ9 only
        try {
            enumVars = Class.class.getDeclaredField("enumVars");
            enumVars.setAccessible(true);
        } catch (ReflectiveOperationException ignored) { } // We're probably not on OpenJ9

        constructor$constructorAccessor = constructorAccessor;
        class$enumConstants = enumConstants;
        class$enumConstantDirectory = enumConstantDirectory;
        class$enumVars = enumVars;
        constructor$acquireConstructorAccessor = acquireConstructorAccessor;
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
            // Constructors are compiled to have additional prefixed parameters, a [String name] and an [int ordinal] before all the other parameters
            Class<?>[] prefixParameterTypes = new Class<?>[] { String.class, int.class };
            parameterTypes = parameterTypes.length == 0 ? prefixParameterTypes : ArrayUtils.addAll(prefixParameterTypes, parameterTypes);
            Constructor<T> constructor = enumClass.getDeclaredConstructor(parameterTypes);
            // ctor.newInstance() results in: throw new IllegalArgumentException("Cannot reflectively create enum objects");
            constructor$constructorAccessor.setAccessible(true);
            Object constructorAccessor = constructor$constructorAccessor.get(constructor);
            if (constructorAccessor == null) {
                constructorAccessor = constructor$acquireConstructorAccessor.invoke(constructor);
            }
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
        }
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
