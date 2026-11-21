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

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.ObjBooleanBiConsumer;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IGenericEvent;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Class<?> type = typeOf(target);
        var pending = new ArrayList<Registration>();
        for (Method method : declaredMethods(type, true)) {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (method.isSynthetic() || staticOnly && !isStatic) {
                continue;
            }
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null) {
                continue;
            }
            rejectDualAnnotation(method);
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

    static List<Subscription> registerAllForge7(
        BusGroup scope, SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(lookup, "lookup");
        List<Registration> pending = prepareAllForge7(scope, lookup, target, true);
        if (pending.isEmpty()) {
            throw new IllegalArgumentException("No declared Forge 7 subscribers on " + typeOf(target).getName());
        }
        return subscriptions.installAll(pending, registration -> registration.install(subscriptions));
    }

    static List<Subscription> registerLegacyChannel(
        LegacyEventChannel<?> channel, SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        Objects.requireNonNull(lookup, "lookup");
        EventBus bus = channel.scope();
        var legacy = bus.prepareLegacySubscribers(
            channel.eventType(), channel.registrationList(), channel.genericType(), lookup, target,
            subscriptions.owner()
        );
        var forge7 = prepareForge7Legacy(
            bus, channel.eventType(), channel.registrationList(), channel.genericType(), lookup, target, true
        );
        var pending = new ArrayList<Registration>(legacy.size() + forge7.size());
        for (var listener : legacy) {
            pending.add(subs -> subs.install(channel.describe(listener.source()), listener.install()));
        }
        pending.addAll(forge7);
        if (pending.isEmpty()) {
            throw new IllegalArgumentException(
                "No legacy subscribers for " + channel.eventType().getName() + " on " + typeOf(target).getName()
            );
        }
        return subscriptions.installAll(pending, registration -> registration.install(subscriptions));
    }

    static List<Subscription> registerAllLegacy(
        EventBus bus, SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        Objects.requireNonNull(bus, "bus");
        Objects.requireNonNull(lookup, "lookup");
        var legacy = bus.prepareLegacySubscribers(null, null, null, lookup, target, subscriptions.owner());
        var forge7 = prepareForge7Legacy(bus, null, null, null, lookup, target, true);
        var pending = new ArrayList<Registration>(legacy.size() + forge7.size());
        for (var listener : legacy) {
            pending.add(subs -> subs.install(listener.source(), listener.install()));
        }
        pending.addAll(forge7);
        if (pending.isEmpty()) {
            throw new IllegalArgumentException("No legacy subscribers on " + typeOf(target).getName());
        }
        return subscriptions.installAll(pending, registration -> registration.install(subscriptions));
    }

    static List<Subscription> registerUnified(
        SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        Objects.requireNonNull(lookup, "lookup");
        EventBus legacyBus = MinecraftForge.EVENT_BUS;
        var forge7 = prepareAllForge7(BusGroup.DEFAULT, lookup, target, true);
        var legacy = legacyBus.prepareLegacySubscribers(null, null, null, lookup, target, subscriptions.owner());
        var forge7Legacy = prepareForge7Legacy(legacyBus, null, null, null, lookup, target, true);
        var pending = new ArrayList<Registration>(forge7.size() + legacy.size() + forge7Legacy.size());
        pending.addAll(forge7);
        for (var listener : legacy) {
            pending.add(subs -> subs.install(listener.source(), listener.install()));
        }
        pending.addAll(forge7Legacy);
        if (pending.isEmpty()) {
            throw new IllegalArgumentException("No subscribers found on " + typeOf(target).getName());
        }
        return subscriptions.installAll(pending, registration -> registration.install(subscriptions));
    }

    static void registerDiscovered(SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Class<?> target) {
        Objects.requireNonNull(lookup, "lookup");
        var forge7 = prepareAllForge7(BusGroup.DEFAULT, lookup, target, false);
        var forge7Legacy = prepareForge7Legacy(MinecraftForge.EVENT_BUS, null, null, null, lookup, target, false);
        if (forge7.isEmpty() && forge7Legacy.isEmpty()) {
            return;
        }
        var pending = new ArrayList<Registration>(forge7.size() + forge7Legacy.size());
        pending.addAll(forge7);
        pending.addAll(forge7Legacy);
        subscriptions.installAll(pending, registration -> registration.install(subscriptions));
    }

    private static List<Registration> prepareAllForge7(
        BusGroup scope, MethodHandles.Lookup lookup, Object target, boolean failOnNoClassDef) {
        boolean staticOnly = target instanceof Class<?>;
        Class<?> type = typeOf(target);
        Map<Class<?>, EventChannel<?>> channels = new HashMap<>();
        var pending = new ArrayList<Registration>();
        for (Method method : declaredMethods(type, failOnNoClassDef)) {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (method.isSynthetic() || staticOnly && !isStatic) {
                continue;
            }
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null) {
                continue;
            }
            rejectDualAnnotation(method);
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 0) {
                throw invalid(method, "expected a Forge 7 event argument");
            }
            Class<?> eventType = parameters[0];
            if (Event.class.isAssignableFrom(eventType)) {
                continue;
            }
            EventChannel<?> channel = channels.computeIfAbsent(eventType, key -> channelFor(scope, key));
            pending.add(prepare(channel, lookup, target, method, isStatic, annotation, parameters));
        }
        return pending;
    }

    private static List<Registration> prepareForge7Legacy(
        EventBus bus, @Nullable Class<? extends Event> exactEventType, @Nullable ListenerList registrationList,
        @Nullable Type channelFilter, MethodHandles.Lookup lookup, Object target, boolean failOnNoClassDef) {
        if (channelFilter != null &&
            (exactEventType == null || !IGenericEvent.class.isAssignableFrom(exactEventType))) {
            throw new IllegalArgumentException(
                exactEventType == null
                    ? "Generic selector requires a specific event type"
                    : exactEventType.getName() + " does not implement IGenericEvent"
            );
        }
        boolean staticOnly = target instanceof Class<?>;
        Class<?> type = typeOf(target);
        var pending = new ArrayList<Registration>();
        ListenerList resolvedList = null;
        for (Method method : declaredMethods(type, failOnNoClassDef)) {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (method.isSynthetic() || staticOnly && !isStatic) {
                continue;
            }
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null) {
                continue;
            }
            rejectDualAnnotation(method);
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 0) {
                throw invalid(method, "expected an event argument");
            }
            Class<?> eventType = parameters[0];
            if (exactEventType != null) {
                if (eventType != exactEventType) {
                    continue;
                }
            } else if (!Event.class.isAssignableFrom(eventType)) {
                // Reject non-event parameter types; skip valid Forge 7 event subscribers for legacy scans.
                EventChannel.EventKind.of(eventType);
                continue;
            }

            Class<? extends Event> legacyType = eventType.asSubclass(Event.class);
            Class<?> returnType = method.getReturnType();
            byte priority = annotation.priority();
            boolean alwaysCancelling = annotation.alwaysCancelling();
            if (parameters.length == 2 && parameters[1] == boolean.class) {
                throw new IllegalArgumentException(
                    "Legacy dispatch has no monitor priority; use Priority.LOWEST and receiveCanceled instead"
                );
            }
            if (parameters.length != 1 || returnType != void.class && returnType != boolean.class) {
                throw invalid(method, "expected one legacy event argument and a void or boolean return");
            }
            EventPriority bucket = LegacyEventChannel.legacyPriority(priority);
            if (returnType == boolean.class && alwaysCancelling) {
                throw invalid(method, "an alwaysCancelling listener must return void");
            }
            Kind kind = alwaysCancelling
                ? Kind.ALWAYS_CANCEL
                : (returnType == boolean.class ? Kind.PREDICATE : Kind.CONSUMER);
            if (kind != Kind.CONSUMER) {
                EventBus.requireCancelable(legacyType);
            }

            boolean isGeneric = IGenericEvent.class.isAssignableFrom(legacyType);
            Type filter = isGeneric && method.getGenericParameterTypes()[0] instanceof ParameterizedType parameterized
                ? parameterized.getActualTypeArguments()[0]
                : null;
            if (filter != null && channelFilter != null && filter != channelFilter) {
                throw new IllegalArgumentException(
                    method.toGenericString() + " selects generic type " + filter +
                        " which differs by identity from the channel selector " + channelFilter
                );
            }
            Type selectedFilter = channelFilter == null ? filter : channelFilter;
            ListenerList list = registrationList;
            if (exactEventType != null && resolvedList != null) {
                list = resolvedList;
            } else if (exactEventType != null) {
                list = registrationList;
                resolvedList = list;
            }

            Object callback = createCallback(lookup, target, method, isStatic, kind);
            Consumer<Event> adapted = adaptLegacyCallback(callback, kind);
            String description = legacyType.getName() + " <- " + method.toGenericString();
            ListenerList targetList = list;
            pending.add(subscriptions -> subscriptions.install(
                description,
                () -> bus.addManagedListener(
                    legacyType, targetList, bucket, false, selectedFilter, adapted, subscriptions.owner(),
                    description
                )
            ));
        }
        return pending;
    }

    @SuppressWarnings("unchecked")
    private static Consumer<Event> adaptLegacyCallback(Object callback, Kind kind) {
        return switch (kind) {
            case CONSUMER -> (Consumer<Event>) callback;
            case ALWAYS_CANCEL -> {
                var consumer = (Consumer<Object>) callback;
                yield event -> {
                    consumer.accept(event);
                    event.setCanceled(true);
                };
            }
            case PREDICATE -> {
                var predicate = (Predicate<Object>) callback;
                yield event -> {
                    if (predicate.test(event)) {
                        event.setCanceled(true);
                    }
                };
            }
            case MONITOR -> throw new AssertionError(kind);
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EventChannel<?> channelFor(BusGroup scope, Class<?> eventType) {
        if (Cancellable.class.isAssignableFrom(eventType)) {
            return EventChannel.cancellable(scope, (Class) eventType);
        }
        return EventChannel.of(scope, eventType);
    }

    private static Class<?> typeOf(Object target) {
        return target instanceof Class<?> utility ? utility : target.getClass();
    }

    private static Method[] declaredMethods(Class<?> type, boolean failOnNoClassDef) {
        if (failOnNoClassDef) {
            return type.getDeclaredMethods();
        }
        try {
            return type.getDeclaredMethods();
        } catch (NoClassDefFoundError missingType) {
            FMLLog.log.debug(
                "{} declares a method whose types cannot be resolved ({}), scanning its public methods only",
                type.getName(), missingType.getMessage()
            );
            return Arrays.stream(type.getMethods())
                .filter(method -> method.getDeclaringClass() == type)
                .toArray(Method[]::new);
        }
    }

    private static void rejectDualAnnotation(Method method) {
        if (method.isAnnotationPresent(net.minecraftforge.fml.common.eventhandler.SubscribeEvent.class)) {
            throw new IllegalArgumentException(
                "Subscriber method " + method + " cannot declare both legacy and Forge 7 @SubscribeEvent annotations"
            );
        }
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
