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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

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
        Set<? extends Class<?>> supers;
        Class<?> scanTarget;
        if (target instanceof Class<?> clazz) {
            isStatic = true;
            supers = Set.of(clazz);
            scanTarget = clazz;
        } else {
            isStatic = false;
            supers = TypeToken.of(target.getClass()).getTypes().rawTypes();
            scanTarget = target.getClass();
        }

        for (Method method : scanTarget.getMethods())
        {
            if (isStatic != Modifier.isStatic(method.getModifiers()))
                continue;

            try {
                // do `.getDeclaredMethod(...)` to force JVM to walk through declared methods and load their parameter
                // types. This is for preventing shortcut below from skipping classloading
                //
                // mod developers should be responsible for not loading non-existent class, but :(
                // related issue: https://github.com/CleanroomMC/Cleanroom/issues/349
                method.getDeclaringClass().getDeclaredMethod("forceClassLoadingForDeclaredMethods", Event.class);
            } catch (NoSuchMethodException e) {
                // swallow this specific exception, other exceptions, like ClassNotFoundException, will fall through
            }

            var parameterTypes = method.getParameterTypes();
            var matched = supers.stream()
                .map(cls -> {
                    if (cls == method.getDeclaringClass()) {
                        // shortcut for most event handler classes with no explicit superclass
                        return method;
                    }
                    try {
                        return cls.getDeclaredMethod(method.getName(), parameterTypes);
                    } catch (NoSuchMethodException e) {
                        // Eat the error, this is not unexpected
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(m -> m.isAnnotationPresent(SubscribeEvent.class))
                .findFirst()
                .orElse(null);

            if (matched == null)
            {
                continue;
            }

            if (parameterTypes.length != 1)
            {
                throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                        " arguments.  Event handler methods must require a single argument."
                );
            }

            Class<?> eventType = parameterTypes[0];

            if (!Event.class.isAssignableFrom(eventType))
            {
                throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
            }

            // the method to be registered here is "matched", not "method", it should be a bug of
            // the original event bus, since the exceptions above are all referencing "method"
            register(eventType, target, matched, activeModContainer);
        }
    }

    /// Some mods will intentionally call this method with illegal `method` object (whose param type is `Object` instead of subclass of `Event`), and we have to allow this stupid move
    private void register(Class<?> eventType, Object target, Method method, final ModContainer owner)
    {
        try
        {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event)ctr.newInstance();
            final ASMEventHandler asm = new ASMEventHandler(target, method, owner, IGenericEvent.class.isAssignableFrom(eventType));

            IEventListener listener = asm;
            if (IContextSetter.class.isAssignableFrom(eventType))
            {
                listener = e -> {
                    var loader = Loader.instance();
                    var old = loader.activeModContainer();

                    loader.setActiveModContainer(owner);
                    ((IContextSetter) e).setModContainer(owner);

                    asm.invoke(e);

                    loader.setActiveModContainer(old);
                };
            }

            event.getListenerList().register(busID, asm.getPriority(), listener);

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
}
