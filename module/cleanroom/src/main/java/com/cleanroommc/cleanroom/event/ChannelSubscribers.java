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
import org.jspecify.annotations.Nullable;

import net.minecraftforge.eventbus.api.listener.ObjBooleanBiConsumer;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NullMarked
final class ChannelSubscribers {

    private static final ClassValue<ConcurrentHashMap<Method, MethodHandle>> CALLBACK_FACTORIES = new ClassValue<>() {

        @Override
        protected ConcurrentHashMap<Method, MethodHandle> computeValue(Class<?> type) {
            return new ConcurrentHashMap<>();
        }

    };

    private ChannelSubscribers() { }

    static List<Subscription> register(
        EventChannel<?> channel, SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
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
            if (annotation == null) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 0) {
                throw invalid(method, "expected a Forge 7 event argument");
            }
            if (parameters[0] == channel.eventType()) {
                pending.add(prepare(channel, lookup, target, method, isStatic, annotation, parameters));
            }
        }
        if (pending.isEmpty()) {
            throw new IllegalArgumentException(
                "No declared Forge 7 subscribers for " + channel.eventType().getName() + " on " + type.getName()
            );
        }

        return subscriptions.installAll(pending, registration -> registration.install(subscriptions));
    }

    @SuppressWarnings("unchecked")
    private static Registration prepare(
        EventChannel<?> channel, MethodHandles.Lookup lookup, Object target, Method method, boolean isStatic,
        SubscribeEvent annotation, Class<?>[] parameters) {
        Class<?> returnType = method.getReturnType();
        if (parameters.length > 2 || returnType != void.class && returnType != boolean.class) {
            throw invalid(method, "expected one Forge 7 event argument and a void or boolean return, or a monitor");
        }
        byte priority = annotation.priority();
        boolean alwaysCancelling = annotation.alwaysCancelling();
        Kind kind;
        if (parameters.length == 2) {
            if (priority != Priority.MONITOR ||
                alwaysCancelling ||
                returnType != void.class ||
                parameters[1] != boolean.class) {
                throw invalid(method, "expected a void MONITOR with a boolean cancellation argument");
            }
            kind = Kind.MONITOR;
        } else if (returnType == boolean.class || alwaysCancelling) {
            if (priority == Priority.MONITOR) {
                throw invalid(method, "a Forge 7 MONITOR cannot cancel");
            }
            if (returnType == boolean.class && alwaysCancelling) {
                throw invalid(method, "an alwaysCancelling listener must return void");
            }
            kind = alwaysCancelling ? Kind.ALWAYS_CANCEL : Kind.PREDICATE;
        } else {
            kind = Kind.CONSUMER;
        }

        String source = method.toGenericString();
        if (kind == Kind.CONSUMER) {
            var callback = (Consumer<Object>) createCallback(lookup, target, method, isStatic, kind);
            return subscriptions -> channel.subscribe(subscriptions, priority, source, callback);
        }
        if (!(channel instanceof CancellableEventChannel<?> cancellable)) {
            throw invalid(
                method, "cancelling and cancellation-aware monitor subscribers require a CancellableEventChannel"
            );
        }
        Object callback = createCallback(lookup, target, method, isStatic, kind);
        return switch (kind) {
            case ALWAYS_CANCEL ->
                subscriptions -> cancellable.subscribeAlwaysCancelling(
                    subscriptions, priority, source, (Consumer<Object>) callback
                );
            case PREDICATE ->
                subscriptions -> cancellable.subscribeCancelling(
                    subscriptions, priority, source, (Predicate<Object>) callback
                );
            case MONITOR ->
                subscriptions -> cancellable.monitor(subscriptions, source, (ObjBooleanBiConsumer<Object>) callback);
            case CONSUMER -> throw new AssertionError(kind);
        };
    }

    private static Object createCallback(
        MethodHandles.Lookup lookup, Object target, Method method, boolean isStatic, Kind kind) {
        MethodHandle handle;
        try {
            handle = lookup.unreflect(method);
        } catch (IllegalAccessException inaccessible) {
            throw new IllegalArgumentException(
                "The supplied Lookup cannot access Forge 7 subscriber " + method, inaccessible
            );
        }
        if (!isStatic && !handle.type().parameterType(0).isInstance(target)) {
            throw invalid(method, "the supplied Lookup cannot access this subscriber instance");
        }

        if (lookup.hasFullPrivilegeAccess()) {
            try {
                MethodHandle factory = callbackFactory(lookup, method, handle, isStatic, kind);
                if (factory != null) {
                    return isStatic ? factory.invoke() : factory.invoke(target);
                }
            } catch (LambdaConversionException | LinkageError unavailable) {
                // The lookup may invoke the method even when its module or loader cannot define the lambda.
            } catch (RuntimeException | Error failure) {
                throw failure;
            } catch (Throwable failure) {
                throw new IllegalArgumentException("Cannot create Forge 7 subscriber " + method, failure);
            }
        }
        MethodHandle adapted = (isStatic ? handle : handle.bindTo(target)).asType(kind.signature);
        return switch (kind) {
            case CONSUMER, ALWAYS_CANCEL ->
                (Consumer<Object>) event -> {
                    try {
                        adapted.invokeExact(event);
                    } catch (Throwable failure) {
                        throw ChannelSubscribers.<RuntimeException>propagate(failure);
                    }
                };
            case PREDICATE ->
                (Predicate<Object>) event -> {
                    try {
                        return (boolean) adapted.invokeExact(event);
                    } catch (Throwable failure) {
                        throw ChannelSubscribers.<RuntimeException>propagate(failure);
                    }
                };
            case MONITOR ->
                (ObjBooleanBiConsumer<Object>) (event, cancelled) -> {
                    try {
                        adapted.invokeExact(event, cancelled);
                    } catch (Throwable failure) {
                        throw ChannelSubscribers.<RuntimeException>propagate(failure);
                    }
                };
        };
    }

    private static @Nullable
    MethodHandle callbackFactory(
        MethodHandles.Lookup lookup, Method method, MethodHandle handle, boolean isStatic, Kind kind
    ) throws LambdaConversionException {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.getModule() != lookup.lookupClass().getModule() ||
            !isStatic && handle.type().parameterType(0) != declaringClass) {
            return lambdaFactory(lookup, handle, isStatic, kind);
        }

        // The caller's access was checked above. Define the reusable factory with the subscriber's lookup
        // so the cache holds neither the caller's class nor any subscriber instance.
        return CALLBACK_FACTORIES.get(declaringClass).computeIfAbsent(method, subscriber -> {
            try {
                MethodHandles.Lookup definingLookup = MethodHandles.privateLookupIn(declaringClass, lookup);
                return lambdaFactory(definingLookup, definingLookup.unreflect(subscriber), isStatic, kind);
            } catch (IllegalAccessException | LambdaConversionException | LinkageError unavailable) {
                return null;
            }
        });
    }

    private static MethodHandle lambdaFactory(
        MethodHandles.Lookup lookup, MethodHandle handle, boolean isStatic, Kind kind
    ) throws LambdaConversionException {
        MethodType signature = handle.type();
        MethodType factoryType = MethodType.methodType(kind.callbackType);
        if (!isStatic) {
            factoryType = factoryType.appendParameterTypes(signature.parameterType(0));
            signature = signature.dropParameterTypes(0, 1);
        }
        return LambdaMetafactory.metafactory(
            lookup, kind.callbackMethod, factoryType, kind.signature, handle, signature
        )
            .getTarget();
    }

    private static IllegalArgumentException invalid(Method method, String reason) {
        return new IllegalArgumentException("Invalid Forge 7 subscriber " + method + ": " + reason);
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> X propagate(Throwable failure) throws X {
        throw (X) failure;
    }

    private enum Kind {

        CONSUMER(Consumer.class, "accept", MethodType.methodType(void.class, Object.class)),
        ALWAYS_CANCEL(Consumer.class, "accept", MethodType.methodType(void.class, Object.class)),
        PREDICATE(Predicate.class, "test", MethodType.methodType(boolean.class, Object.class)),
        MONITOR(ObjBooleanBiConsumer.class, "accept", MethodType.methodType(void.class, Object.class, boolean.class));

        private final Class<?> callbackType;
        private final String callbackMethod;
        private final MethodType signature;

        Kind(Class<?> callbackType, String callbackMethod, MethodType signature) {
            this.callbackType = callbackType;
            this.callbackMethod = callbackMethod;
            this.signature = signature;
        }

    }

    private interface Registration {

        Subscription install(SubscriptionGroup subscriptions);

    }

}
