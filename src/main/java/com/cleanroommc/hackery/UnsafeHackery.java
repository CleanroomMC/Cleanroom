package com.cleanroommc.hackery;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class UnsafeHackery {

    private static final Unsafe $;

    static {
        Unsafe _$ = null;
        try {
            _$ = Unsafe.getUnsafe();
        } catch (SecurityException ignored) {
            Field theUnsafeField = null;
            for (Field field : Unsafe.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    if (field.get(null) instanceof Unsafe unsafeInstance) {
                        _$ = unsafeInstance;
                        break;
                    }
                } catch (IllegalAccessException ignored1) { }
            }
        }
        if (_$ == null) {
            throw new RuntimeException("Cleanroom cannot instantiate an instance of Unsafe!");
        }
        $ = _$;
    }



}
