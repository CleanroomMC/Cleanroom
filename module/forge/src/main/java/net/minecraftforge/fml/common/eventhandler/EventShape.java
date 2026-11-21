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

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.annotation.Nullable;

/** Class structure only. Instance getters and setup are still ordinary virtual calls. */
final class EventShape {
    static final int CANCELABLE = 1;
    static final int HAS_RESULT = 2;
    private static final int SETUP = 4;
    private static final int GET_LIST = 8;
    private static final int LIST_METHODS = SETUP | GET_LIST;
    static final int VIRTUAL_CANCELLATION = 16;
    private static final int PROPERTY_METHODS = CANCELABLE | HAS_RESULT | VIRTUAL_CANCELLATION;

    // A bounded fast path for frequently constructed types. The ClassValue remains canonical,
    // and a cache slot cannot keep a mod class or its loader alive.
    private static final int CACHE_SIZE = 256;
    private static final AtomicReferenceArray<WeakReference<EventShape>> RECENT_SHAPES =
        new AtomicReferenceArray<>(CACHE_SIZE);

    // ClassValue may compute a value concurrently. Only the selected slot may add
    // a ListenerList to the registry.
    private static final ClassValue<ShapeSlot> SLOTS = new ClassValue<>() {
        @Override
        protected ShapeSlot computeValue(Class<?> type) {
            return new ShapeSlot();
        }
    };

    private static final ClassValue<EventShape> SHAPES = new ClassValue<>() {
        @Override
        protected EventShape computeValue(Class<?> type) {
            if (!Event.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(type.getName() + " is not an Event");
            }
            EventShape shape = describe(type);
            if (shape.problem != null) {
                throw new IllegalArgumentException("Unsupported event structure: " + shape.problem);
            }
            return shape;
        }
    };

    private static EventShape describe(Class<?> type) {
        ShapeSlot slot = SLOTS.get(type);
        EventShape existing = slot.shape;
        if (existing != null) {
            return existing;
        }

        EventShape parent = type == Event.class ? null : describe(type.getSuperclass());
        int methods = parent == null ? 0 : declaredMethods(type);
        int annotations = (type.isAnnotationPresent(Cancelable.class) ? CANCELABLE : 0)
            | (type.isAnnotationPresent(Event.HasResult.class) ? HAS_RESULT : 0);
        synchronized (ListenerList.REGISTRY_LOCK) {
            if (slot.shape == null) {
                slot.shape = new EventShape(type, parent, methods, annotations);
            }
            return slot.shape;
        }
    }

    private static final class ShapeSlot {
        private volatile EventShape shape;
    }

    private static final ThreadLocal<Set<Class<?>>> RESOLVING_LIST = new ThreadLocal<>();

    private final Class<?> type;
    private final WeakReference<EventShape> cacheEntry = new WeakReference<>(this);
    private final boolean handwrittenList;
    private final int handwrittenProperties;
    private final String problem;
    final byte baseProperties;
    final ListenerList baseList;

    private EventShape(Class<?> type, @Nullable EventShape parent, int methods, int annotations) {
        this.type = type;
        if (parent == null) {
            handwrittenList = false;
            handwrittenProperties = 0;
            problem = null;
            baseProperties = 0;
            baseList = new ListenerList();
            return;
        }

        handwrittenList = parent.handwrittenList || (methods & LIST_METHODS) != 0;
        handwrittenProperties = parent.handwrittenProperties | (methods & PROPERTY_METHODS);
        baseProperties = (byte) (parent.baseProperties | (annotations & ~handwrittenProperties)
            | (handwrittenProperties & VIRTUAL_CANCELLATION));
        problem = structureProblem(type, parent, methods, annotations);
        // A hand-written super call uses the last ordinary class before the first complete pair.
        baseList = handwrittenList || problem != null ? parent.baseList : new ListenerList(parent.baseList);
    }

    private static String structureProblem(Class<?> type, EventShape parent, int methods, int annotations) {
        if (parent.problem != null) {
            return parent.problem;
        }
        int conflicts = annotations & parent.handwrittenProperties & ~methods;
        if (conflicts != 0) {
            String property = (conflicts & CANCELABLE) != 0 ? "isCancelable" : "hasResult";
            return type.getName() + " needs an explicit " + property
                + "() override because it inherits a hand-written implementation";
        }
        if ((parent.handwrittenList || (methods & LIST_METHODS) != 0) && (methods & LIST_METHODS) != LIST_METHODS) {
            return type.getName() + " must declare both setup() and getListenerList()"
                + " at every layer of a hand-written ListenerList hierarchy";
        }
        return null;
    }

    static EventShape of(Class<?> type) {
        int index = System.identityHashCode(type) & (CACHE_SIZE - 1);
        WeakReference<EventShape> entry = RECENT_SHAPES.getAcquire(index);
        if (entry != null) {
            EventShape cached = entry.get();
            if (cached != null && cached.type == type) {
                return cached;
            }
        }
        return resolve(type, index);
    }

    private static EventShape resolve(Class<?> type, int index) {
        EventShape shape = SHAPES.get(type);
        RECENT_SHAPES.setRelease(index, shape.cacheEntry);
        return shape;
    }

    boolean isPotentiallyCancelable() {
        return (baseProperties & (CANCELABLE | VIRTUAL_CANCELLATION)) != 0;
    }

    @Nullable
    ListenerList probeList(@Nullable ListenerList explicitList) {
        return handwrittenList && explicitList == null ? null : registrationList(explicitList);
    }

    ListenerList registrationList(@Nullable ListenerList explicitList) {
        if (!handwrittenList) {
            if (explicitList != null && explicitList != baseList) {
                throw new IllegalArgumentException(type.getName() + " uses its canonical ListenerList");
            }
            return baseList;
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
                event = Event.class.cast(constructor.newInstance());
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
        Method[] methods;
        try {
            methods = type.getDeclaredMethods();
        } catch (NoClassDefFoundError unresolved) {
            return declaredMethodsFromClassFile(type, unresolved);
        }
        int found = 0;
        for (Method method : methods) {
            if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Class<?> returns = method.getReturnType();
            found |= switch (method.getName()) {
                case "isCancelable" -> returns == boolean.class ? CANCELABLE | VIRTUAL_CANCELLATION : 0;
                case "isCanceled" -> returns == boolean.class ? VIRTUAL_CANCELLATION : 0;
                case "hasResult" -> returns == boolean.class ? HAS_RESULT : 0;
                case "setup" -> returns == void.class ? SETUP : 0;
                case "getListenerList" -> ListenerList.class.isAssignableFrom(returns) ? GET_LIST : 0;
                default -> 0;
            };
        }
        return found;
    }

    private static int declaredMethodsFromClassFile(Class<?> type, NoClassDefFoundError unresolved) {
        // A class resource can predate bytecode transformations, so use it only if reflection fails.
        String internalName = type.getName().replace('.', '/');
        try {
            var model = ClassFile.of().parse(readClassBytes(type, internalName));
            if (!model.thisClass().asInternalName().equals(internalName)) {
                throw new IOException("Class resource declares " + model.thisClass().asInternalName());
            }

            int found = 0;
            for (var method : model.methods()) {
                if (Modifier.isStatic(method.flags().flagsMask())) {
                    continue;
                }
                var descriptor = method.methodType();
                found |= switch (method.methodName().stringValue()) {
                    case "isCancelable" -> descriptor.equalsString("()Z") ? CANCELABLE | VIRTUAL_CANCELLATION : 0;
                    case "isCanceled" -> descriptor.equalsString("()Z") ? VIRTUAL_CANCELLATION : 0;
                    case "hasResult" -> descriptor.equalsString("()Z") ? HAS_RESULT : 0;
                    case "setup" -> descriptor.equalsString("()V") ? SETUP : 0;
                    case "getListenerList" -> isListenerListReturn(type, descriptor.stringValue()) ? GET_LIST : 0;
                    default -> 0;
                };
            }
            return found;
        } catch (IOException | IllegalArgumentException | ClassNotFoundException failure) {
            var error = new NoClassDefFoundError("Cannot resolve event shape for " + type.getName()
                + " (" + failure.getMessage() + ')');
            error.initCause(unresolved);
            error.addSuppressed(failure);
            throw error;
        }
    }

    private static byte[] readClassBytes(Class<?> type, String internalName) throws IOException {
        byte[] bytes;
        if (type.getClassLoader() instanceof LaunchClassLoader loader) {
            bytes = loader.getClassBytes(type.getName());
        } else {
            try (var resource = type.getResourceAsStream('/' + internalName + ".class")) {
                bytes = resource == null ? null : resource.readAllBytes();
            }
        }
        if (bytes == null) {
            throw new IOException("Class resource is unavailable");
        }
        return bytes;
    }

    private static boolean isListenerListReturn(Class<?> type, String descriptor) throws ClassNotFoundException {
        if (!descriptor.startsWith("()L")) {
            return false;
        }
        String returnName = descriptor.substring(3, descriptor.length() - 1).replace('/', '.');
        return ListenerList.class.isAssignableFrom(Class.forName(returnName, false, type.getClassLoader()));
    }
}
