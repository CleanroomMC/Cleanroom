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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

/** Class structure only. Instance getters and setup are still ordinary virtual calls. */
final class EventShape {
    static final int CANCELABLE = 1;
    static final int HAS_RESULT = 2;
    private static final int SETUP = 4;
    private static final int GET_LIST = 8;
    private static final int LIST_METHODS = SETUP | GET_LIST;

    private static final ClassValue<EventShape> SHAPES = new ClassValue<>() {
        @Override
        protected EventShape computeValue(Class<?> type) {
            if (type == Event.class) {
                return new EventShape(Event.class, null, null, 0, 0, null);
            }

            EventShape parent = get(type.getSuperclass());
            int methods = declaredMethods(type);
            int annotations = (type.isAnnotationPresent(Cancelable.class) ? CANCELABLE : 0)
                | (type.isAnnotationPresent(Event.HasResult.class) ? HAS_RESULT : 0);
            int conflicts = annotations & parent.handwrittenProperties & ~methods;
            String problem = parent.problem;
            EventShape listAnchor = parent.listAnchor;
            if (problem == null) {
                if (conflicts != 0) {
                    String property = (conflicts & CANCELABLE) != 0 ? "isCancelable" : "hasResult";
                    problem = type.getName() + " needs an explicit " + property
                        + "() override because it inherits a hand-written implementation";
                } else if ((methods & LIST_METHODS) != 0 && (methods & LIST_METHODS) != LIST_METHODS
                    || listAnchor != null && (methods & LIST_METHODS) != LIST_METHODS) {
                    problem = type.getName() + " must declare both setup() and getListenerList()"
                        + " at every layer of a hand-written ListenerList hierarchy";
                }
            }
            if (listAnchor == null && (methods & LIST_METHODS) == LIST_METHODS) {
                listAnchor = parent;
            }
            int handwritten = parent.handwrittenProperties | (methods & (CANCELABLE | HAS_RESULT));
            int defaults = parent.baseProperties | (annotations & ~handwritten);
            return new EventShape(type.asSubclass(Event.class), parent, listAnchor, defaults, handwritten, problem);
        }
    };

    private static final ThreadLocal<Set<Class<?>>> RESOLVING_LIST = new ThreadLocal<>();

    private final Class<? extends Event> type;
    private final EventShape parent;
    private final EventShape listAnchor;
    private final int handwrittenProperties;
    private final String problem;
    final byte baseProperties;
    private volatile ListenerList list;

    private EventShape(Class<? extends Event> type, @Nullable EventShape parent, @Nullable EventShape listAnchor,
                       int baseProperties, int handwrittenProperties, @Nullable String problem) {
        this.type = type;
        this.parent = parent;
        this.listAnchor = listAnchor;
        this.baseProperties = (byte) baseProperties;
        this.handwrittenProperties = handwrittenProperties;
        this.problem = problem;
    }

    static EventShape of(Class<? extends Event> type) {
        EventShape shape = SHAPES.get(type);
        if (shape.problem != null) {
            throw new IllegalArgumentException("Unsupported event structure: " + shape.problem);
        }
        return shape;
    }

    /** For a hand-written super call, use the last ordinary class before the first complete pair. */
    ListenerList baseList() {
        ListenerList existing = list;
        if (existing != null) {
            return existing;
        }
        return initializeBaseList();
    }

    private ListenerList initializeBaseList() {
        if (listAnchor != null) {
            ListenerList inherited = listAnchor.baseList();
            list = inherited;
            return inherited;
        }
        ListenerList parentList = parent == null ? null : parent.baseList();
        // ClassValue has selected the canonical slot. Publish only under the registry lock.
        synchronized (ListenerList.REGISTRY_LOCK) {
            if (list == null) {
                list = new ListenerList(parentList);
            }
            return list;
        }
    }

    ListenerList registrationList(@Nullable ListenerList explicitList) {
        if (listAnchor == null) {
            ListenerList canonical = baseList();
            if (explicitList != null && explicitList != canonical) {
                throw new IllegalArgumentException(type.getName() + " uses its canonical ListenerList");
            }
            return canonical;
        }
        if (explicitList != null) {
            return explicitList;
        }

        Set<Class<?>> resolving = RESOLVING_LIST.get();
        if (resolving == null) {
            resolving = new HashSet<>();
            RESOLVING_LIST.set(resolving);
        }
        if (!resolving.add(type)) {
            throw listFailure("recursive initialization", null);
        }
        try {
            if (Modifier.isAbstract(type.getModifiers())) {
                throw listFailure("abstract event requires an explicit ListenerList", null);
            }
            Event event;
            try {
                var constructor = type.getConstructor();
                if (!constructor.canAccess(null) && !constructor.trySetAccessible()) {
                    throw new IllegalAccessException("Cannot access the public constructor");
                }
                event = constructor.newInstance();
            } catch (InvocationTargetException failure) {
                throw listFailure("public no-arg constructor failed", failure.getCause());
            } catch (ReflectiveOperationException | SecurityException failure) {
                throw listFailure("provide an accessible public no-arg constructor or an explicit ListenerList", failure);
            }
            ListenerList selected;
            try {
                selected = event.getListenerList();
            } catch (RuntimeException failure) {
                throw listFailure("getter failed", failure);
            }
            if (selected == null) {
                throw listFailure("getter returned null", null);
            }
            return selected;
        } finally {
            resolving.remove(type);
            if (resolving.isEmpty()) {
                RESOLVING_LIST.remove();
            }
        }
    }

    private IllegalArgumentException listFailure(String reason, @Nullable Throwable cause) {
        return new IllegalArgumentException(type.getName() + ".getListenerList(): " + reason, cause);
    }

    private static int declaredMethods(Class<?> type) {
        int found = 0;
        for (Method method : type.getDeclaredMethods()) {
            if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Class<?> returns = method.getReturnType();
            found |= switch (method.getName()) {
                case "isCancelable" -> returns == boolean.class ? CANCELABLE : 0;
                case "hasResult" -> returns == boolean.class ? HAS_RESULT : 0;
                case "setup" -> returns == void.class ? SETUP : 0;
                case "getListenerList" -> ListenerList.class.isAssignableFrom(returns) ? GET_LIST : 0;
                default -> 0;
            };
        }
        return found;
    }
}
