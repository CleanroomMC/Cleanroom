package com.cleanroommc.cleanroom.hackery.enums;

import net.lenni0451.reflect.Enums;

public final class EnumHackery {

    public static <T extends Enum<T>> T addEnumEntry(Class<T> enumClass, String enumName) {
        return addEnumEntry(enumClass, enumName, new Class[0], new Object[0]);
    }

    public static <T extends Enum<T>> T addEnumEntry(Class<T> enumClass, String enumName, Class<?>[] parameterTypes, Object[] parameterValues) {
        T o = Enums.newInstance(enumClass, enumName, enumClass.getEnumConstants().length, parameterTypes, parameterValues);
        Enums.addEnumInstance(enumClass, o);
        return o;
    }

    private EnumHackery() { }

}
