package net.minecraftforge.fml.common.eventhandler;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
final class EventListenerFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodType CONSUMER = MethodType.methodType(void.class, Object.class);
    private static final MethodType FACTORY = MethodType.methodType(Consumer.class);
    private static final MethodType EVENT_CALLBACK = MethodType.methodType(void.class, Event.class);
    private static final ClassValue<ConcurrentHashMap<Method, Factory>> FACTORIES = new ClassValue<>() {
        @Override
        protected ConcurrentHashMap<Method, Factory> computeValue(Class<?> owner) {
            return new ConcurrentHashMap<>();
        }
    };

    private EventListenerFactory() {}

    public static Consumer<Event> createRawListener(Method method, boolean isStatic, Object instance) {
        if (isStatic != Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Static flag does not match event subscriber " + method);
        }
        return createRawListener(method, instance, null);
    }

    static Consumer<Event> createRawListener(Method method, Object instance, MethodHandles.Lookup caller) {
        // A supplied Lookup is a capability. Never make its handles available to a different caller.
        Factory factory = caller == null
            ? FACTORIES.get(method.getDeclaringClass()).computeIfAbsent(method, EventListenerFactory::createLegacyFactory)
            : createFactory(method, caller, unreflect(method, caller));
        return factory.bind(instance);
    }

    private static Factory createLegacyFactory(Method method) {
        try {
            var lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), LOOKUP);
            return createFactory(method, lookup, lookup.unreflect(method));
        } catch (IllegalAccessException privateAccess) {
            return createFactory(method, LOOKUP, unreflect(method, LOOKUP));
        }
    }

    private static MethodHandle unreflect(Method method, MethodHandles.Lookup lookup) {
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException inaccessible) {
            throw new IllegalArgumentException("Cannot access event subscriber " + method
                + "; supply a Lookup with access to its declaring class", inaccessible);
        }
    }

    private static Factory createFactory(Method method, MethodHandles.Lookup lookup, MethodHandle handle) {
        Class<?> declaring = method.getDeclaringClass();
        boolean isStatic = Modifier.isStatic(method.getModifiers());

        if (lookup.hasFullPrivilegeAccess() && lookup.lookupClass().getClassLoader() == declaring.getClassLoader()) {
            Class<?> receiver = isStatic ? null : handle.type().parameterType(0);
            MethodType factoryType = isStatic ? FACTORY : FACTORY.insertParameterTypes(0, receiver);
            MethodType signature = MethodType.methodType(void.class, method.getParameterTypes());
            try {
                var target = LambdaMetafactory.metafactory(lookup, "accept", factoryType, CONSUMER, handle, signature)
                    .getTarget();
                return new Factory(isStatic ? target : target.asType(FACTORY.insertParameterTypes(0, Object.class)),
                    isStatic, true);
            } catch (LambdaConversionException unsupportedCaller) {
                // A legal direct handle still works when the caller cannot define an LMF class.
            }
        }
        return new Factory(handle, isStatic, false);
    }

    private record Factory(MethodHandle handle, boolean isStatic, boolean lambda) {
        @SuppressWarnings("unchecked")
        Consumer<Event> bind(Object instance) {
            if (!isStatic) {
                Objects.requireNonNull(instance, "target");
            }
            if (!lambda) {
                MethodHandle callback = (isStatic ? handle : handle.bindTo(instance)).asType(EVENT_CALLBACK);
                return event -> {
                    try {
                        callback.invokeExact(event);
                    } catch (Throwable failure) {
                        EventListenerFactory.<RuntimeException>rethrow(failure);
                    }
                };
            }
            try {
                return isStatic ? (Consumer<Event>) handle.invokeExact() : (Consumer<Event>) handle.invokeExact(instance);
            } catch (Throwable failure) {
                return EventListenerFactory.<RuntimeException, Consumer<Event>>fail(failure);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> void rethrow(Throwable failure) throws X {
        throw (X) failure;
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable, T> T fail(Throwable failure) throws X {
        throw (X) failure;
    }
}
