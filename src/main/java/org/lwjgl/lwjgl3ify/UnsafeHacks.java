/*
 * Minecraft Forge Copyright (c) 2016-2019. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software Foundation version 2.1 of
 * the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details. You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 */
package org.lwjgl.lwjgl3ify;

import java.lang.reflect.Field;
import java.util.Optional;

@SuppressWarnings({ "restriction", "sunapi" })
public class UnsafeHacks {

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            final Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Field field, Object object) {
        if (object == null) {
            long offset = UNSAFE.staticFieldOffset(field);
            Object base = UNSAFE.staticFieldBase(field);
            return (T) UNSAFE.getObject(base, offset);
        } else {
            long offset = UNSAFE.objectFieldOffset(field);
            return (T) UNSAFE.getObject(object, offset);
        }
    }

    public static void setField(Field data, Object object, Object value) {
        if (object == null) {
            long offset = UNSAFE.staticFieldOffset(data);
            Object base = UNSAFE.staticFieldBase(data);
            UNSAFE.putObject(base, offset, value);
        } else {
            long offset = UNSAFE.objectFieldOffset(data);
            UNSAFE.putObject(object, offset, value);
        }
    }

    public static int getIntField(Field field, Object object) {
        if (object == null) {
            long offset = UNSAFE.staticFieldOffset(field);
            Object base = UNSAFE.staticFieldBase(field);
            return UNSAFE.getInt(base, offset);
        } else {
            long offset = UNSAFE.objectFieldOffset(field);
            return UNSAFE.getInt(object, offset);
        }
    }

    public static void setIntField(Field data, Object object, int value) {
        if (object == null) {
            long offset = UNSAFE.staticFieldOffset(data);
            Object base = UNSAFE.staticFieldBase(data);
            UNSAFE.putInt(base, offset, value);
        } else {
            long offset = UNSAFE.objectFieldOffset(data);
            UNSAFE.putInt(object, offset, value);
        }
    }

    // Make sure we don't crash if any future versions change field names
    private static Optional<Field> findField(Class<?> clazz, String name) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(name)) {
                return Optional.of(f);
            }
        }
        return Optional.empty();
    }

    public static void cleanEnumCache(Class<? extends Enum<?>> enumClass) throws Exception {
        findField(Class.class, "enumConstantDirectory").ifPresent(f -> setField(f, enumClass, null));
        findField(Class.class, "enumConstants").ifPresent(f -> setField(f, enumClass, null));
    }
}
