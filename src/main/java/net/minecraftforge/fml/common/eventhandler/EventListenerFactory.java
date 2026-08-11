package net.minecraftforge.fml.common.eventhandler;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

import net.lenni0451.reflect.JavaBypass;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Creates event listeners from {@link SubscribeEvent} methods without requiring the methods to
 * be public. The lookup is anchored on the declaring class of the callback, so members of any
 * visibility (private/protected/package-private) can be accessed directly, without relying on
 * {@code EventSubscriberTransformer} publicising {@code @SubscribeEvent} methods at class load.
 *
 * @author ZZZank
 */
class EventListenerFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static Consumer<Event> createRawListener(Method method, boolean isStatic, Object instance) {
        // no caching is applied here because in EventBus scenario, caching will only be useful
        // when two instance-based listeners of the same class are registered, which is an
        // incredibly rare usage in 1.12 Forge environment

        MethodHandle handle;
        MethodHandles.Lookup lookup = resolveLookup(method);
        try {
            handle = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access listener method " + method, e);
        }

        try {
            var listenerFactory = createListenerFactory(handle, lookup, isStatic, instance);
            @SuppressWarnings("unchecked")
            var rawListener = isStatic
                ? (Consumer<Event>) listenerFactory.invokeExact()
                : (Consumer<Event>) listenerFactory.invokeExact(instance);
            return rawListener;
        } catch (Throwable t) {
            // LambdaMetafactory can be inapplicable when the declaring class lives in a
            // different classloader (the generated lambda class would end up in the wrong
            // loader). Composing the MethodHandle directly carries no such restriction.
            return createRawListenerDirect(handle, isStatic, instance);
        }
    }

    /**
     * Resolves a lookup anchored on the declaring class of the method, granting access to
     * members of any visibility. In a classpath (unnamed module) environment this always
     * succeeds; the trusted lookup is kept as a last-resort fallback.
     */
    private static MethodHandles.Lookup resolveLookup(Method method) {
        try {
            return MethodHandles.privateLookupIn(method.getDeclaringClass(), LOOKUP);
        } catch (IllegalAccessException e) {
            return JavaBypass.TRUSTED_LOOKUP;
        }
    }

    private static MethodHandle createListenerFactory(
        MethodHandle handle,
        MethodHandles.Lookup lookup,
        boolean isStatic,
        Object instance
    ) {
        try {
            var factoryType = isStatic
                ? Constants.RETURNS_IT
                // implicit null check on "instance" via ".getClass()"
                : Constants.RETURNS_IT.insertParameterTypes(0, instance.getClass());

            var factoryHandle = LambdaMetafactory.metafactory(
                lookup,
                Constants.METHOD_NAME,
                factoryType,
                Constants.METHOD_TYPE,
                handle,
                MethodType.methodType(void.class, handle.type().parameterType(isStatic ? 0 : 1))
            ).getTarget();

            return isStatic
                ? factoryHandle
                : factoryHandle.asType(factoryType.changeParameterType(0, Object.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fallback that composes the method handle directly, bypassing {@link LambdaMetafactory}
     * entirely. Works across classloaders and for members of any visibility.
     */
    private static Consumer<Event> createRawListenerDirect(MethodHandle handle, boolean isStatic, Object instance) {
        try {
            MethodHandle call = handle;
            if (!isStatic) {
                call = call.bindTo(instance);
            }
            call = call.asType(MethodType.methodType(void.class, Event.class));
            final MethodHandle target = call;
            return event -> {
                try {
                    target.invokeExact(event);
                } catch (Throwable t) {
                    throw ExceptionUtils.<RuntimeException>rethrow(t);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    interface Constants {
        Class<?> CLAZZ = Consumer.class;
        Method METHOD = Arrays.stream(CLAZZ.getMethods())
            .filter(m -> "accept".equals(m.getName()))
            .findFirst()
            .orElseThrow();
        String METHOD_NAME = METHOD.getName();
        MethodType METHOD_TYPE = MethodType.methodType(METHOD.getReturnType(), METHOD.getParameterTypes());
        MethodType RETURNS_IT = MethodType.methodType(CLAZZ);
    }
}
