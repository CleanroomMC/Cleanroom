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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class EventBus implements IEventExceptionHandler
{
    private final Map<Object, List<Binding>> listeners = new HashMap<>();
    private final int busID = ListenerList.allocateBusID();
    private final IEventExceptionHandler exceptionHandler;
    private volatile boolean shutdown;

    public EventBus()
    {
        exceptionHandler = this;
    }

    public EventBus(@Nonnull IEventExceptionHandler handler)
    {
        Preconditions.checkNotNull(handler, "EventBus exception handler can not be null");
        exceptionHandler = handler;
    }

    public void register(Object target)
    {
        Objects.requireNonNull(target, "target");
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            if (listeners.containsKey(target))
                return;
        }

        ModContainer owner = ModEventContext.registrationOwner();
        List<Binding> bindings = prepare(target, owner, null);
        if (bindings.isEmpty())
            return;
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            if (!listeners.containsKey(target))
            {
                listeners.put(target, bindings);
                try
                {
                    install(bindings);
                }
                catch (RuntimeException | Error failure)
                {
                    listeners.remove(target);
                    throw failure;
                }
            }
        }
    }

    /** Internal entry point for Cleanroom's managed legacy annotation registration. */
    public Runnable registerManaged(MethodHandles.Lookup lookup, Object target, ModContainer owner)
    {
        List<Binding> bindings = prepare(target, owner, Objects.requireNonNull(lookup, "lookup"));
        if (bindings.isEmpty())
            throw new IllegalArgumentException("No legacy event subscribers found on " + SubscriberMethod.typeOf(target).getName());
        return installManaged(bindings);
    }

    /** Internal entry point for Cleanroom's managed legacy programmatic registration. */
    public <E extends Event> Runnable addManagedListener(Class<E> eventType, @Nullable ListenerList registrationList,
            EventPriority priority, boolean receiveCanceled, @Nullable Type genericType, Consumer<? super E> listener,
            ModContainer owner, String source)
    {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(priority, "priority");
        if (genericType != null && !IGenericEvent.class.isAssignableFrom(eventType))
            throw new IllegalArgumentException(eventType.getName() + " does not implement IGenericEvent");
        ListenerList list = EventShape.of(eventType).registrationList(registrationList);
        var handler = new ASMEventHandler(event -> listener.accept(eventType.cast(event)), priority, receiveCanceled,
            owner, genericType, source);
        return installManaged(List.of(bind(list, eventType, handler, owner, true)));
    }

    private static List<Binding> prepare(Object target, ModContainer owner, @Nullable MethodHandles.Lookup lookup)
    {
        List<SubscriberMethod> subscribers = SubscriberMethod.forTarget(target);
        var bindings = new ArrayList<Binding>(subscribers.size());
        for (SubscriberMethod subscriber : subscribers)
        {
            Method source = subscriber.source();
            if (Modifier.isPrivate(source.getModifiers()))
            {
                String message = "Cannot register private @SubscribeEvent method " + source;
                if (lookup != null)
                    throw new IllegalArgumentException(message);
                FMLLog.log.warn(message);
                continue;
            }
            Class<?>[] parameters = source.getParameterTypes();
            if (parameters.length != 1 || !Event.class.isAssignableFrom(parameters[0]))
                throw new IllegalArgumentException("Expected one Event argument on @SubscribeEvent method " + source);
            Class<? extends Event> eventType = parameters[0].asSubclass(Event.class);
            ListenerList list = EventShape.of(eventType).registrationList(null);
            var handler = new ASMEventHandler(target, source, subscriber.callback(), owner,
                IGenericEvent.class.isAssignableFrom(eventType), lookup);
            bindings.add(bind(list, eventType, handler, owner, lookup != null));
        }
        return bindings;
    }

    private static Binding bind(ListenerList list, Class<?> eventType, ASMEventHandler handler, ModContainer owner,
                                boolean managed)
    {
        boolean setEventOwner = IContextSetter.class.isAssignableFrom(eventType);
        IEventListener listener = managed || setEventOwner ? new ContextSetterEventListener(owner, handler, setEventOwner) : handler;
        return new Binding(list, handler.getPriority(), listener);
    }

    // Called with the registry lock held. Preparation may run user constructors or getters.
    private void install(List<Binding> bindings)
    {
        int index = 0;
        try
        {
            for (; index < bindings.size(); index++)
            {
                Binding binding = bindings.get(index);
                binding.list().register(busID, binding.priority(), binding.listener());
            }
        }
        catch (RuntimeException | Error failure)
        {
            for (int rollback = index; rollback >= 0; rollback--)
            {
                try
                {
                    remove(bindings.get(rollback));
                }
                catch (RuntimeException | Error cleanup)
                {
                    if (cleanup != failure)
                        failure.addSuppressed(cleanup);
                }
            }
            throw failure;
        }
    }

    private Runnable installManaged(List<Binding> bindings)
    {
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            install(bindings);
        }
        return () -> {
            synchronized (ListenerList.REGISTRY_LOCK)
            {
                for (Binding binding : bindings)
                    remove(binding);
            }
        };
    }

    private void remove(Binding binding)
    {
        binding.list().unregister(busID, binding.listener());
    }

    private record Binding(ListenerList list, EventPriority priority, IEventListener listener) {}

    // Some mods call this signature reflectively with a callback that accepts Object.
    private void register(Class<?> eventType, Object target, Method method, final ModContainer owner)
    {
        var type = eventType.asSubclass(Event.class);
        ListenerList list = EventShape.of(type).registrationList(null);
        var handler = new ASMEventHandler(target, method, method, owner, IGenericEvent.class.isAssignableFrom(type), null);
        Binding binding = bind(list, type, handler, owner, false);
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            List<Binding> bindings = listeners.computeIfAbsent(target, ignored -> new ArrayList<>());
            install(List.of(binding));
            bindings.add(binding);
        }
    }

    public void unregister(Object object)
    {
        Objects.requireNonNull(object, "object");
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            List<Binding> bindings = listeners.remove(object);
            if (bindings == null)
                return;
            for (Binding binding : bindings)
                ListenerList.unregisterAll(busID, binding.listener());
        }
    }

    public boolean post(Event event)
    {
        if (shutdown) return false;

        // Hold the array for this post, but read each slot only when dispatch reaches it.
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

    private record ContextSetterEventListener(ModContainer owner, ASMEventHandler handler, boolean setEventOwner)
        implements IEventListener
    {
        @Override
        public void invoke(Event event)
        {
            ModContainer previous = ModEventContext.swapOwner(owner);
            try
            {
                if (setEventOwner)
                    ((IContextSetter) event).setModContainer(owner);
                handler.invoke(event);
            }
            finally
            {
                ModEventContext.setOwner(previous);
            }
        }

        @Override
        public String toString()
        {
            return "ContextSetter[owner=" + owner + ", listener=" + handler + "]";
        }
    }
}
