/*
 * Cleanroom
 * Copyright (c) 2026 Cleanroom contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.eventhandler;

import com.google.common.reflect.TypeToken;
import net.minecraftforge.fml.common.FMLLog;

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
        var declaredPrivateSubscribers = new ArrayList<SubscriberMethod>();
        for (Class<?> declaration : declarations) {
            for (Method method : declaredMethods(declaration)) {
                int modifiers = method.getModifiers();
                if (method.isSynthetic() || Modifier.isStatic(modifiers) != isStatic || !method.isAnnotationPresent(SubscribeEvent.class)) {
                    continue;
                }
                if (Modifier.isPrivate(modifiers)) {
                    if (declaration == type) {
                        declaredPrivateSubscribers.add(new SubscriberMethod(method, method));
                    }
                    continue;
                }
                var signature = new Signature(method.getName(), Arrays.asList(method.getParameterTypes()));
                subscribers.computeIfAbsent(signature,
                    ignored -> new SubscriberMethod(method, invocationTarget(type, method, isStatic)));
            }
        }
        declaredPrivateSubscribers.addAll(subscribers.values());
        return List.copyOf(declaredPrivateSubscribers);
    }

    // Resolving every declared signature can fail when a private helper mentions an absent optional dependency.
    // Listing only public methods in that case preserves the subscribers Forge could still discover.
    private static Method[] declaredMethods(Class<?> type) {
        try {
            return type.getDeclaredMethods();
        } catch (NoClassDefFoundError missingType) {
            FMLLog.log.debug("{} declares a method whose types cannot be resolved ({}), registering its public methods only",
                type.getName(), missingType.getMessage());
            return Arrays.stream(type.getMethods())
                .filter(method -> method.getDeclaringClass() == type)
                .toArray(Method[]::new);
        }
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
            for (Method candidate : declaredMethods(child)) {
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
