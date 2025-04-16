package net.minecraftforge.fml.common.eventhandler;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
class EventListenerFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static IEventListener createRawListener(Method method, boolean isStatic, Object instance) {
        // no caching is applied here because in EventBus scenario, caching will only be useful
        // when two instance-based listeners of the same class are registered, which is an
        // incredibly rare usage in 1.12 Forge environment
        var listenerFactory = createListenerFactory(method, isStatic, instance);

        try {
            return isStatic
                ? (IEventListener) listenerFactory.invokeExact()
                : (IEventListener) listenerFactory.invokeExact(instance);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static MethodHandle createListenerFactory(
        Method callback,
        boolean isStatic,
        Object instance
    ) {
        try {
            var handle = LOOKUP.unreflect(callback);

            var factoryType = isStatic
                ? Constants.RETURNS_IT
                // implicit null check on "instance" via ".getClass()"
                : Constants.RETURNS_IT.insertParameterTypes(0, instance.getClass());

            var factoryHandle = LambdaMetafactory.metafactory(
                LOOKUP,
                Constants.METHOD_NAME,
                factoryType,
                Constants.METHOD_TYPE,
                handle,
                isStatic ? handle.type() : handle.type().dropParameterTypes(0, 1)
            ).getTarget();

            return isStatic
                ? factoryHandle
                : factoryHandle.asType(factoryType.changeParameterType(0, Object.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    interface Constants {
        Class<?> CLAZZ = IEventListener.class;
        Method METHOD = CLAZZ.getMethods()[0];
        String METHOD_NAME = METHOD.getName();
        MethodType METHOD_TYPE = MethodType.methodType(METHOD.getReturnType(), METHOD.getParameterTypes());
        MethodType RETURNS_IT = MethodType.methodType(CLAZZ);
    }
}
