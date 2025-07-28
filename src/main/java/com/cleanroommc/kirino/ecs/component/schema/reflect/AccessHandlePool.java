package com.cleanroommc.kirino.ecs.component.schema.reflect;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AccessHandlePool {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Map<Class<?>, MemberLayout> memberLayoutMap = new HashMap<>();
    private final Map<Class<?>, MethodHandle> constructorHandleMap = new HashMap<>();
    private final Map<Class<?>, Map<String, MethodHandle>> setterHandleMap = new HashMap<>();

    public void register(Class<?> clazz, MemberLayout memberLayout) {
        try {
            Constructor<?> ctor = clazz.getConstructor();
            ctor.setAccessible(true);
            MethodHandle ctorHandle = LOOKUP.unreflectConstructor(ctor);
            constructorHandleMap.put(clazz, ctorHandle);

            Map<String, MethodHandle> setters = setterHandleMap.computeIfAbsent(clazz, (key) -> new HashMap<>());
            for (String fieldName: memberLayout.fieldNames) {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                MethodHandle setterHandle = LOOKUP.unreflectSetter(field);
                setters.put(fieldName, setterHandle);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        memberLayoutMap.put(clazz, memberLayout);
    }

    public boolean classRegistered(Class<?> clazz) {
        return memberLayoutMap.containsKey(clazz);
    }

    @Nullable
    public Object newClass(Class<?> clazz) {
        if (!classRegistered(clazz)) {
            return null;
        }
        MethodHandle ctor = constructorHandleMap.get(clazz);
        try {
            return ctor.invoke();
        } catch (Throwable e) {
            return null;
        }
    }

    public void setFieldValue(Class<?> clazz, Object target, String fieldName, Object value) {
        if (!classRegistered(clazz)) {
            return;
        }
        MethodHandle setter = setterHandleMap.get(clazz).get(fieldName);
        if (setter == null) {
            return;
        }
        try {
            setter.invoke(target, value);
        } catch (Throwable ignore) {
        }
    }
}
