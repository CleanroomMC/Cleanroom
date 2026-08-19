package net.minecraftforge.fml.common.eventhandler;

import net.lenni0451.reflect.accessor.UnsafeAccess;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

abstract class EventProperties {
    /// @see Event#getListenerList()
    static final ClassValue<ListenerList> LISTENER_LIST = new ClassValue<>() {
        @Override
        protected ListenerList computeValue(@NonNull Class<?> type) {
            if (!Event.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Not event class: " + type);
            }

            if (type == Event.class) {
                return new ListenerList();
            }

            // assignable to Event.class & not Event.class -> subclass of Event
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) type;

            ListenerList result = null;
            try {
                var method_getListenerList = eventType.getMethod("getListenerList");

                if (method_getListenerList.getDeclaringClass() != Event.class) {
                    // This event implements its own .getListenerList()
                    var instance = UnsafeAccess.allocateInstance(eventType);
                    result = (ListenerList) method_getListenerList.invoke(instance);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // fall through
            }

            if (result == null) {
                ListenerList parent = get(type.getSuperclass());
                result = new ListenerList(parent);
            }

            return result;
        }
    };

    /// @see net.minecraftforge.fml.common.eventhandler.Event.HasResult
    /// @see Event#hasResult()
    static final ClassValue<Boolean> HAS_RESULT = checkChainedAnnotation(Event.HasResult.class, "hasResult");

    /// @see Cancelable
    /// @see Event#isCancelable()
    static final ClassValue<Boolean> CANCELLABLE = checkChainedAnnotation(Cancelable.class, "isCancelable");

    private static ClassValue<Boolean> checkChainedAnnotation(Class<? extends Annotation> target, String methodName) {
        return new ClassValue<>() {
            @Override
            protected Boolean computeValue(@NonNull Class<?> type) {
                if (!Event.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException("Not event class: " + type);
                }

                try {
                    var method = type.getMethod(methodName);

                    // walks superclass, until (exclusive) the first method implementation
                    Class<?> checkEnd = method.getDeclaringClass();
                    for (var c = type; c != checkEnd; c = c.getSuperclass()) {
                        if (type.isAnnotationPresent(target)) {
                            return true;
                        }
                    }

                    // The first method implementation is not from Event -> custom impl
                    if (method.getDeclaringClass() != Event.class) {
                        // respect custom impl
                        var instance = UnsafeAccess.allocateInstance(type);
                        if ((boolean) method.invoke(instance)) {
                            return true;
                        }
                    }

                    // fall through to default
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    // fall through to default
                }

                return false;
            }
        };
    }
}
