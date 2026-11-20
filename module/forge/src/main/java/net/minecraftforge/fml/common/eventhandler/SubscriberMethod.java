/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 */

package net.minecraftforge.fml.common.eventhandler;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

record SubscriberMethod(Method source, Method callback) {
    private static final ClassValue<List<SubscriberMethod>> STATIC = new ClassValue<>() {
        @Override
        protected List<SubscriberMethod> computeValue(Class<?> type) {
            return scan(type, true);
        }
    };
    private static final ClassValue<List<SubscriberMethod>> INSTANCE = new ClassValue<>() {
        @Override
        protected List<SubscriberMethod> computeValue(Class<?> type) {
            return scan(type, false);
        }
    };

    static Class<?> typeOf(Object target) {
        return target instanceof Class<?> type ? type : target.getClass();
    }

    static List<SubscriberMethod> forTarget(Object target) {
        return (target instanceof Class<?> ? STATIC : INSTANCE).get(typeOf(target));
    }

    private static List<SubscriberMethod> scan(Class<?> type, boolean isStatic) {
        Iterable<? extends Class<?>> declarations = isStatic ? List.of(type) : TypeToken.of(type).getTypes().rawTypes();
        var subscribers = new LinkedHashMap<Signature, SubscriberMethod>();
        var privateSubscribers = new ArrayList<SubscriberMethod>();
        for (Class<?> declaration : declarations) {
            for (Method method : declaration.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (method.isSynthetic() || Modifier.isStatic(modifiers) != isStatic || !method.isAnnotationPresent(SubscribeEvent.class)) {
                    continue;
                }
                if (Modifier.isPrivate(modifiers)) {
                    privateSubscribers.add(new SubscriberMethod(method, method));
                    continue;
                }
                var signature = new Signature(method.getName(), Arrays.asList(method.getParameterTypes()));
                subscribers.computeIfAbsent(signature,
                    ignored -> new SubscriberMethod(method, invocationTarget(type, method, isStatic)));
            }
        }
        privateSubscribers.addAll(subscribers.values());
        return List.copyOf(privateSubscribers);
    }

    private static Method invocationTarget(Class<?> type, Method source, boolean isStatic) {
        int modifiers = source.getModifiers();
        if (isStatic || Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers) || Modifier.isFinal(modifiers)) {
            return source;
        }

        // The old transformer publicized the annotated method. A matching method in a different
        // runtime package could then override it even if javac did not see an override.
        Class<?>[] parameters = source.getParameterTypes();
        for (Class<?> child = type; child != source.getDeclaringClass(); child = child.getSuperclass()) {
            for (Method candidate : child.getDeclaredMethods()) {
                int access = candidate.getModifiers();
                if (!Modifier.isStatic(access) && !Modifier.isPrivate(access)
                    && candidate.getName().equals(source.getName()) && candidate.getReturnType() == source.getReturnType()
                    && Arrays.equals(candidate.getParameterTypes(), parameters)) {
                    return candidate;
                }
            }
        }
        return source;
    }

    private record Signature(String name, List<Class<?>> parameters) {}
}
