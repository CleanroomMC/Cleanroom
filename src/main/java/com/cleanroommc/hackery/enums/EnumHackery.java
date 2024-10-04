package com.cleanroommc.hackery.enums;

import com.cleanroommc.hackery.ReflectionHackery;
import jdk.internal.reflect.ReflectionFactory;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.ArrayUtils;
import top.outlands.foundation.boot.JVMDriverHolder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public final class EnumHackery {

    public static final ReflectionFactory factory = ReflectionFactory.getReflectionFactory();

    private static final Field class$enumConstants, class$enumConstantDirectory, enumVars$cachedEnumConstants, enumVars$cachedEnumConstantDirectory;
    private static final Method constructorAccessor$newInstance, class$enumConstantDirectory_method, class$getEnumVars;

    private static final Class[] prefix = {String.class, int.class};

    static {
        Field constructorAccessor;
        Field enumConstants;
        Field enumConstantDirectory;
        Field enumConstantsj9;
        Field enumConstantDirectoryj9;
        Method newInstance;
        Method enumConstantDirectoryMethod;
        Method enumVars;
        try {
            constructorAccessor = ReflectionHackery.deepSearchForField(Constructor.class, field -> "constructorAccessor".equals(field.getName()), true);

            if (System.getProperty("java.vendor").startsWith("IBM")){
                enumVars = Class.class.getDeclaredMethod("getEnumVars");
                enumVars.setAccessible(true);
                enumConstantsj9 = enumVars.getReturnType().getDeclaredField("cachedEnumConstants");
                enumConstantsj9.setAccessible(true);
                enumConstantDirectoryj9 = enumVars.getReturnType().getDeclaredField("cachedEnumConstantDirectory");
                enumConstantsj9.setAccessible(true);
                enumConstants = null;
                enumConstantDirectory = null;
            } else {
                enumConstants = Class.class.getDeclaredField("enumConstants");
                enumConstants.setAccessible(true);

                enumConstantDirectory = Class.class.getDeclaredField("enumConstantDirectory");
                enumConstantDirectory.setAccessible(true);

                enumConstantsj9 = null;
                enumConstantDirectoryj9 = null;
                enumVars = null;
            }


            newInstance = constructorAccessor.getType().getMethod("newInstance", Object[].class);

            enumConstantDirectoryMethod = Class.class.getDeclaredMethod("enumConstantDirectory");
            enumConstantDirectoryMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        class$enumConstants = enumConstants;
        class$enumConstantDirectory = enumConstantDirectory;
        class$getEnumVars = enumVars;
        enumVars$cachedEnumConstants = enumConstantsj9;
        enumVars$cachedEnumConstantDirectory = enumConstantDirectoryj9;
        constructorAccessor$newInstance = newInstance;
        class$enumConstantDirectory_method = enumConstantDirectoryMethod;
    }

    public static <T extends Enum<T>> T addEnumEntry(Class<T> enumClass, String enumName) {
        return addEnumEntry(enumClass, enumName, new Class[0], new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T addEnumEntry(Class<T> enumClass, String enumName, Class<?>[] parameterTypes, Object[] parameterValues) {
        if (parameterTypes.length != parameterValues.length) {
            throw new IllegalArgumentException("The amount of parameter types must be the same as the parameter values.");
        }

        try {
            Constructor<T> constructor = enumClass.getDeclaredConstructor(ArrayUtils.addAll(prefix, parameterTypes));
            constructor.setAccessible(true);
            MethodHandle handle = MethodHandles.lookup().unreflectConstructor(constructor);
            Method m = enumClass.getMethod("values");
            Object o = m.invoke(enumClass);
            T instance = (T) handle.withVarargs(false).invokeWithArguments(ArrayUtils.addAll(new Object[]{enumName, ((Object[]) o).length}, parameterValues));

            Field nameField = Enum.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(instance, enumName);

            Field valuesField = enumClass.getDeclaredField("$VALUES");
            valuesField.setAccessible(true);

            Field ordinalField = Enum.class.getDeclaredField("ordinal");
            ordinalField.setAccessible(true);

            // we get the current Enum[]
            T[] values = (T[]) valuesField.get(null);
            for (int i = 0; i < values.length; i++) {
                T value = values[i];
                if (value.name().equals(enumName)) {
                    //setOrdinal(enumName, value.ordinal());
                    ordinalField.set(instance, value.ordinal());
                    values[i] = instance;
                    Field[] fields = enumClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getName().equals(enumName)) {
                            ReflectionHackery.setField(field, null, instance);
                        }
                    }
                    return instance;
                }
            }

            // we did not find it in the existing array, thus
            // append it to the array
            T[] newValues = Arrays.copyOf(values, values.length + 1);
            newValues[newValues.length - 1] = instance;
            ReflectionHackery.setField(valuesField, null, newValues);
            if (System.getProperty("java.vendor").startsWith("IBM")){
                Object enumVar = class$getEnumVars.invoke(enumClass);
                ReflectionHackery.setField(enumVars$cachedEnumConstants, enumVar, newValues);
            }else {
                // Add new enum to cache
                ReflectionHackery.setField(class$enumConstants, enumClass, newValues);
            }

            // Ensure the cache exists
            Map<String, T> directory = (Map<String, T>) class$enumConstantDirectory_method.invoke(enumClass);
            // Add new enum to cache
            directory.put(enumName, instance);

            int ordinal = newValues.length - 1;
            ordinalField.set(instance, ordinal);

            return instance;

        } catch (Throwable e) {
            FMLLog.log.error(e);
            throw new RuntimeException(e);
        }

    }

    public static <T extends Enum<T>> void resetEnumRelatedCaches(Class<T> enumClass) throws ReflectiveOperationException {
        if (class$enumConstants != null){
            class$enumConstants.set(enumClass, null);
            class$enumConstantDirectory.set(enumClass, null);
        }
    }

    private EnumHackery() { }

}
