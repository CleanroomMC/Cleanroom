/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.event;

import org.jspecify.annotations.NullMarked;

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.event.InheritableEvent;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NullMarked
final class NativeSubscribers {

    private NativeSubscribers() { }

    static List<Subscription> register(
        SubscriptionGroup subscriptions, BusGroup scope, MethodHandles.Lookup lookup, Object target) {
        Objects.requireNonNull(lookup, "lookup");
        boolean staticOnly = target instanceof Class<?>;
        Class<?> type = staticOnly ? (Class<?>) target : target.getClass();
        var pending = new ArrayList<Registration>();
        for (Method method : type.getDeclaredMethods()) {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (method.isSynthetic() || staticOnly && !isStatic) {
                continue;
            }
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation != null) {
                pending.add(prepare(scope, lookup, target, method, isStatic, annotation));
            }
        }
        if (pending.isEmpty()) {
            throw new IllegalArgumentException("No declared Forge 7 subscribers on " + type.getName());
        }

        var installed = new ArrayList<Subscription>(pending.size());
        try {
            for (Registration registration : pending) {
                installed.add(registration.install(subscriptions));
            }
        } catch (RuntimeException | Error failure) {
            for (Subscription subscription : installed.reversed()) {
                try {
                    subscription.close();
                } catch (RuntimeException | Error cleanup) {
                    if (failure != cleanup) {
                        failure.addSuppressed(cleanup);
                    }
                }
            }
            throw failure;
        }
        return List.copyOf(installed);
    }

    private static Registration prepare(
        BusGroup scope, MethodHandles.Lookup lookup, Object target, Method method, boolean isStatic,
        SubscribeEvent annotation) {
        Class<?>[] parameters = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        if (parameters.length < 1 || parameters.length > 2 || returnType != void.class && returnType != boolean.class) {
            throw invalid(method, "expected one native event argument and a void or boolean return, or a monitor");
        }
        Class<?> eventType = parameters[0];
        boolean cancellable = Cancellable.class.isAssignableFrom(eventType);
        byte priority = annotation.priority();
        boolean alwaysCancelling = annotation.alwaysCancelling();
        NativeEventChannel.Kind kind;
        if (parameters.length == 2) {
            if (!cancellable ||
                priority != Priority.MONITOR ||
                alwaysCancelling ||
                returnType != void.class ||
                parameters[1] != boolean.class) {
                throw invalid(
                    method, "expected a void MONITOR for a cancellable event and a boolean cancellation argument"
                );
            }
            kind = NativeEventChannel.Kind.MONITOR;
        } else if (returnType == boolean.class || alwaysCancelling) {
            if (!cancellable || priority == Priority.MONITOR || returnType == boolean.class && alwaysCancelling) {
                throw invalid(
                    method, "only cancellable events support a boolean return or alwaysCancelling void listener"
                );
            }
            kind = alwaysCancelling ? NativeEventChannel.Kind.ALWAYS_CANCEL : NativeEventChannel.Kind.PREDICATE;
        } else {
            kind = NativeEventChannel.Kind.CONSUMER;
        }

        NativeEventChannel<?> channel;
        if (RecordEvent.class.isAssignableFrom(eventType)) {
            channel = NativeEventChannel.record(scope, eventType.asSubclass(RecordEvent.class));
        } else if (MutableEvent.class.isAssignableFrom(eventType)) {
            channel = NativeEventChannel.mutable(scope, eventType.asSubclass(MutableEvent.class));
        } else if (InheritableEvent.class.isAssignableFrom(eventType)) {
            channel = NativeEventChannel.inheritable(scope, eventType.asSubclass(InheritableEvent.class));
        } else {
            throw invalid(method, "expected a Forge 7 RecordEvent, MutableEvent or InheritableEvent");
        }

        MethodHandle handle;
        try {
            MethodHandle unbound = lookup.unreflect(method);
            handle = isStatic ? unbound : unbound.bindTo(target);
        } catch (IllegalAccessException inaccessible) {
            throw new IllegalArgumentException(
                "The supplied Lookup cannot access Forge 7 subscriber " + method, inaccessible
            );
        }
        NativeEventChannel.Callback<Object> callback;
        if (kind == NativeEventChannel.Kind.MONITOR) {
            MethodHandle adapted = handle.asType(MethodType.methodType(void.class, Object.class, boolean.class));
            callback = (event, cancelled) -> {
                adapted.invokeExact(event, cancelled);
                return false;
            };
        } else if (kind == NativeEventChannel.Kind.PREDICATE) {
            MethodHandle adapted = handle.asType(MethodType.methodType(boolean.class, Object.class));
            callback = (event, cancelled) -> (boolean) adapted.invokeExact(event);
        } else {
            MethodHandle adapted = handle.asType(MethodType.methodType(void.class, Object.class));
            callback = (event, cancelled) -> {
                adapted.invokeExact(event);
                return false;
            };
        }
        return new Registration(channel, priority, method.toGenericString(), kind, callback);
    }

    private static IllegalArgumentException invalid(Method method, String reason) {
        return new IllegalArgumentException("Invalid Forge 7 subscriber " + method + ": " + reason);
    }

    private record Registration(
        NativeEventChannel<?> channel,
        byte priority,
        String source,
        NativeEventChannel.Kind kind,
        NativeEventChannel.Callback<Object> callback) {

        Subscription install(SubscriptionGroup subscriptions) {
            return channel.subscribe(subscriptions, priority, source, kind, callback);
        }

    }

}
