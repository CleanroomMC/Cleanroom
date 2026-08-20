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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NonNull;

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

    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> handler)
    {
        addListener(eventType, EventPriority.NORMAL, false, handler);
    }

    public <T extends Event> void addListener(Class<T> eventType, EventPriority priority, Consumer<T> handler)
    {
        addListener(eventType, priority, false, handler);
    }

    public <T extends Event> void addListener(
        Class<T> eventType,
        EventPriority priority,
        boolean receiveCanceled,
        Consumer<T> handler
    )
    {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(handler, "handler");
        if (!Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException("Not an event type: " + eventType);
        }
        if (listeners.containsKey(handler)) {
            return;
        }

        @SuppressWarnings("unchecked")
        IEventListener listener = receiveCanceled
            ? ((Consumer<Event>) handler)::accept
            : event -> {
                if (!event.isCancelable() || !event.isCanceled()) {
                    handler.accept((T) event);
                }
            };

        ModContainer activeModContainer = Loader.instance().activeModContainer();
        if (activeModContainer == null) {
            FMLLog.log.error("Unable to determine registrant mod for {}. This is a critical error and should be impossible", handler, new Throwable());
            activeModContainer = Loader.instance().getMinecraftModContainer();
        }
        listenerOwners.put(handler, activeModContainer);

        register0(eventType, EventProperties.LISTENER_LIST.get(eventType), handler, listener, priority, activeModContainer);
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

        Collection<Method> methods;
        if (target instanceof Class<?> clazz) {
            // static listener: subscribed methods must be declared by the class
            methods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers())
                             && m.isAnnotationPresent(SubscribeEvent.class))
                .toList();
        } else {
            // instance listener: methods overriding a subscribed method is also valid
            // In this case, we will register the subscribed parent method instead. JVM will
            // handle it if a subclass overrides subscribed method
            methods = TypeToken.of(target.getClass())
                .getTypes()
                .rawTypes()
                // get self & superclass & interface
                .stream()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(m -> !m.isSynthetic()
                             && !Modifier.isStatic(m.getModifiers())
                             // private not allowed because it does not participate in inheritance
                             && !Modifier.isPrivate(m.getModifiers())
                             && m.isAnnotationPresent(SubscribeEvent.class))
                // deduplicate by signature
                .collect(Collectors.toMap(
                    m -> Map.entry(m.getName(), Arrays.asList(m.getParameterTypes())),
                    Function.identity(),
                    (a, b) -> a,
                    LinkedHashMap::new
                ))
                .values();
        }

        for (Method method : methods)
        {
            var parameterTypes = method.getParameterTypes();
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

            register(eventType, target, method, activeModContainer);
        }
    }

    /// Some mods will intentionally call this method with illegal `method` object (whose param type is `Object` instead of subclass of `Event`), and we have to allow this stupid move
    private void register(Class<?> eventType, Object target, Method method, final ModContainer owner)
    {
        try
        {
            ASMEventHandler asm = new ASMEventHandler(target, method, owner, IGenericEvent.class.isAssignableFrom(eventType));

            register0(eventType, EventProperties.LISTENER_LIST.get(eventType), target, asm, asm.getPriority(), owner);
        }
        catch (Exception e)
        {
            FMLLog.log.error("Error registering event handler: {} {} {}", owner, eventType, method, e);
        }
    }

    private void register0(
        Class<?> eventType,
        ListenerList listenerList,
        Object key,
        IEventListener listener,
        EventPriority priority,
        ModContainer owner
    )
    {
        if (IContextSetter.class.isAssignableFrom(eventType))
        {
            listener = new ContextSetterEventListener(owner, listener);
        }

        listenerList.register(busID, priority, listener);
        listeners.computeIfAbsent(key, _ -> new ArrayList<>()).add(listener);
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
        IEventListener asm
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
