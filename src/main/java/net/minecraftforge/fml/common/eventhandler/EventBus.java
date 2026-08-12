/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import net.lenni0451.reflect.accessor.UnsafeAccess;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import org.jspecify.annotations.NonNull;

import org.objectweb.asm.Type;

public class EventBus implements IEventExceptionHandler
{
    private static int maxID = 0;

    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = new ConcurrentHashMap<Object, ArrayList<IEventListener>>();
    private Map<Object,ModContainer> listenerOwners = new MapMaker().weakKeys().weakValues().makeMap();
    private final int busID = maxID++;
    private IEventExceptionHandler exceptionHandler;
    private boolean shutdown;

    public EventBus()
    {
        ListenerList.resize(busID + 1);
        exceptionHandler = this;
    }

    public EventBus(@Nonnull IEventExceptionHandler handler)
    {
        this();
        Preconditions.checkNotNull(handler, "EventBus exception handler can not be null");
        exceptionHandler = handler;
    }

    public void register(Object target)
    {
        if (listeners.containsKey(target))
        {
            return;
        }

        ModContainer activeModContainer = Loader.instance().activeModContainer();
        if (activeModContainer == null)
        {
            FMLLog.log.error("Unable to determine registrant mod for {}. This is a critical error and should be impossible", target, new Throwable());
            activeModContainer = Loader.instance().getMinecraftModContainer();
        }
        listenerOwners.put(target, activeModContainer);

        boolean isStatic;
        Class<?> scanTarget;
        if (target instanceof Class<?> clazz) {
            isStatic = true;
            scanTarget = clazz;
        } else {
            isStatic = false;
            scanTarget = target.getClass();
        }

        for (Method matched : collectHandlers(scanTarget, isStatic))
        {
            if (isStatic != Modifier.isStatic(matched.getModifiers()))
                continue;

            Class<?> eventType = getEventType(matched);

            register(eventType, target, matched, activeModContainer);
        }
    }

    private static @NonNull Class<?> getEventType(Method matched) {
        var parameterTypes = matched.getParameterTypes();
        if (parameterTypes.length != 1)
        {
            throw new IllegalArgumentException(
                "Method " + matched + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                    " arguments.  Event handler methods must require a single argument."
            );
        }

        Class<?> eventType = parameterTypes[0];

        if (!Event.class.isAssignableFrom(eventType))
        {
            throw new IllegalArgumentException("Method " + matched + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
        }
        return eventType;
    }

    /**
     * Collects listener handlers by walking the class hierarchy from the given class upwards,
     * subclass declarations first. For each signature only annotated declarations matter and
     * the first one encountered (in subclass-first order) wins, so an override without
     * {@code @SubscribeEvent} naturally falls through to the annotated supertype declaration.
     * Interfaces are walked after the class chain. When {@code declaredOnly} is true (static
     * registration) only methods declared on the class itself are considered, so a subclass
     * never inherits its superclass' static listeners.
     */
    private static List<Method> collectHandlers(Class<?> clazz, boolean declaredOnly) {
        Map<String, Method> handlers = new LinkedHashMap<>();
        if (declaredOnly) {
            collectLayer(clazz, handlers);
        } else {
            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                collectLayer(c, handlers);
            }
            for (Class<?> itf : allInterfaces(clazz)) {
                collectLayer(itf, handlers);
            }
        }
        return new ArrayList<>(handlers.values());
    }

    private static void collectLayer(Class<?> type, Map<String, Method> handlers) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.isBridge() || m.isSynthetic()) continue;
            if (m.isAnnotationPresent(SubscribeEvent.class)) {
                handlers.putIfAbsent(signatureKey(m), m);
            }
        }
    }

    private static String signatureKey(Method m) {
        return m.getName() + Type.getMethodDescriptor(m);
    }

    /**
     * All interfaces of the class hierarchy, including super-interfaces, deduplicated.
     */
    private static List<Class<?>> allInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Class<?> itf : c.getInterfaces()) {
                collectInterfaces(itf, interfaces);
            }
        }
        return new ArrayList<>(interfaces);
    }

    private static void collectInterfaces(Class<?> itf, Set<Class<?>> out) {
        if (out.add(itf)) {
            for (Class<?> parent : itf.getInterfaces()) {
                collectInterfaces(parent, out);
            }
        }
    }

    /// Some mods will intentionally call this method with illegal `method` object (whose param type is `Object` instead of subclass of `Event`), and we have to allow this stupid move
    private void register(Class<?> eventType, Object target, Method method, final ModContainer owner)
    {
        try
        {
            ListenerList listenerList;
            try
            {
                // Check if it has a constructor first
                Constructor<?> ctr = eventType.getConstructor();
                ctr.setAccessible(true);
                Event event = (Event)ctr.newInstance();
                listenerList = event.getListenerList();
            }
            catch (NoSuchMethodException e)
            {
                // Use Unsafe hack later
                try
                {
                    UnsafeAccess.ensureClassInitialized(eventType);
                    Event event = (Event) UnsafeAccess.allocateInstance(eventType);
                    listenerList = event.getListenerList();
                }
                catch (Throwable t)
                {
                    // last resort: resolve the per-class list from the probe cache, which builds
                    // the same list chained to the superclass list that the injected code built
                    listenerList = EventCompatProbe.listenerList(eventType);
                }
            }

            final ASMEventHandler asm = new ASMEventHandler(target, method, owner, IGenericEvent.class.isAssignableFrom(eventType));

            IEventListener listener = asm;
            if (IContextSetter.class.isAssignableFrom(eventType))
            {
                listener = new ContextSetterEventListener(owner, asm);
            }

            listenerList.register(busID, asm.getPriority(), listener);

            ArrayList<IEventListener> others = listeners.computeIfAbsent(target, k -> new ArrayList<>());
            others.add(listener);
        }
        catch (Exception e)
        {
            FMLLog.log.error("Error registering event handler: {} {} {}", owner, eventType, method, e);
        }
    }

    public void unregister(Object object)
    {
        ArrayList<IEventListener> list = listeners.remove(object);
        if(list == null)
            return;
        for (IEventListener listener : list)
        {
            ListenerList.unregisterAll(busID, listener);
        }
    }

    public boolean post(Event event)
    {
        if (shutdown) return false;

        IEventListener[] listeners = event.getListenerList().getListeners(busID);
        int index = 0;
        try
        {
            for (; index < listeners.length; index++)
            {
                listeners[index].invoke(event);
            }
        }
        catch (Throwable throwable)
        {
            exceptionHandler.handleException(this, event, listeners, index, throwable);
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }
        return event.isCancelable() && event.isCanceled();
    }

    public void shutdown()
    {
        FMLLog.log.warn("EventBus {} shutting down - future events will not be posted.", busID);
        shutdown = true;
    }

    @Override
    public void handleException(EventBus bus, Event event, IEventListener[] listeners, int index, Throwable throwable)
    {
        FMLLog.log.error("Exception caught during firing event {}:", event, throwable);
        FMLLog.log.error("Index: {} Listeners:", index);
        for (int x = 0; x < listeners.length; x++)
        {
            FMLLog.log.error("{}: {}", x, listeners[x]);
        }
    }

    private record ContextSetterEventListener(
        ModContainer owner,
        ASMEventHandler asm
    ) implements IEventListener {

        @Override
        public void invoke(Event e) {
            var loader = Loader.instance();
            var old = loader.activeModContainer();

            loader.setActiveModContainer(owner);
            ((IContextSetter) e).setModContainer(owner);

            asm.invoke(e);

            loader.setActiveModContainer(old);
        }

        @Override
        @NonNull
        public String toString() {
            return "ContextSetter[mod=" + owner.getModId() + ", listener=" + asm + "]";
        }
    }
}
