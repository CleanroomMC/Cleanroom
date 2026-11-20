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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;

import com.cleanroommc.hidefromuserdev.HideFromUserdev;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;

public class EventBus implements IEventExceptionHandler
{
    private static int maxID;

    private final ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Object, ModContainer> listenerOwners = new MapMaker().weakKeys().weakValues().makeMap();
    private final int busID = allocateBusID();
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

    private static int allocateBusID()
    {
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            int id = maxID++;
            ListenerList.resize(id + 1);
            return id;
        }
    }

    public void register(Object target)
    {
        if (listeners.containsKey(target))
            return;

        ModContainer owner = ModEventContext.registrationOwner();
        listenerOwners.put(target, owner);
        ArrayList<IEventListener> entries = null;
        for (SubscriberMethod subscriber : SubscriberMethod.forTarget(target))
        {
            Method source = subscriber.source();
            if (Modifier.isPrivate(source.getModifiers()))
                continue;
            Class<?> eventType = eventParameter(source);
            try
            {
                Binding binding = prepare(eventType, target, source, subscriber.callback(), owner, null);
                synchronized (ListenerList.REGISTRY_LOCK)
                {
                    // A concurrent operation may have registered or removed this target during preparation.
                    if (listeners.get(target) != entries)
                        return;
                    entries = installLegacy(target, entries, binding);
                }
            }
            catch (Exception failure)
            {
                logRegistrationFailure(owner, eventType, source, failure);
            }
        }
    }

    /** Internal entry point for Cleanroom's managed legacy annotation registration. */
    @HideFromUserdev
    public Runnable registerManaged(MethodHandles.Lookup lookup, Object target, ModContainer owner)
    {
        Objects.requireNonNull(lookup, "lookup");
        List<SubscriberMethod> subscribers = SubscriberMethod.forTarget(target);
        var bindings = new ArrayList<Binding>(subscribers.size());
        for (SubscriberMethod subscriber : subscribers)
        {
            Method source = subscriber.source();
            bindings.add(prepare(eventParameter(source), target, source, subscriber.callback(), owner, lookup));
        }
        if (bindings.isEmpty())
            throw new IllegalArgumentException("No legacy event subscribers found on " + SubscriberMethod.typeOf(target).getName());
        return installManaged(bindings);
    }

    /**
     * Registers {@code target} like {@link #register(Object)}, but under {@code owner} and with a removal handle.
     * <p>
     * Discovery, arity validation, private-method skipping, and per-subscriber failure logging are identical to
     * {@code register(Object)}.
     */
    @HideFromUserdev
    public Runnable registerManagedLenient(MethodHandles.Lookup lookup, Object target, ModContainer owner)
    {
        Objects.requireNonNull(lookup, "lookup");
        ArrayList<IEventListener> entries = null;
        var installed = new ArrayList<Binding>();
        for (SubscriberMethod subscriber : SubscriberMethod.forTarget(target))
        {
            Method source = subscriber.source();
            if (Modifier.isPrivate(source.getModifiers()))
                continue;
            Class<?> eventType = eventParameter(source);
            try
            {
                Binding binding = prepare(eventType, target, source, subscriber.callback(), owner, lookup);
                synchronized (ListenerList.REGISTRY_LOCK)
                {
                    entries = installLegacy(target, entries, binding);
                }
                installed.add(binding);
            }
            catch (Exception failure)
            {
                logRegistrationFailure(owner, eventType, source, failure);
            }
        }
        ArrayList<IEventListener> registered = entries;
        List<Binding> bindings = List.copyOf(installed);
        return () -> {
            if (registered == null)
                return;
            synchronized (ListenerList.REGISTRY_LOCK)
            {
                for (Binding binding : bindings)
                    remove(binding);
                listeners.remove(target, registered);
            }
        };
    }

    @HideFromUserdev
    public record ManagedListener(String source, Supplier<Runnable> install) {}

    @HideFromUserdev
    public List<ManagedListener> prepareManagedListeners(Class<? extends Event> eventType, @Nullable ListenerList list,
            @Nullable Type genericType, MethodHandles.Lookup lookup, Object target, ModContainer owner)
    {
        Objects.requireNonNull(lookup, "lookup");
        boolean isGeneric = IGenericEvent.class.isAssignableFrom(eventType);
        if (genericType != null && !isGeneric)
            throw new IllegalArgumentException(eventType.getName() + " does not implement IGenericEvent");

        var prepared = new ArrayList<ManagedListener>();
        ListenerList registrationList = null;
        for (SubscriberMethod subscriber : SubscriberMethod.forTarget(target))
        {
            Method source = subscriber.source();
            Class<?>[] parameters = source.getParameterTypes();
            if (parameters.length != 0 && parameters[0] != eventType)
                continue;
            eventParameter(source, parameters);
            if (registrationList == null)
                registrationList = EventShape.of(eventType).registrationList(list);

            Method callback = subscriber.callback();
            var handler = new ASMEventHandler(target, source, callback, owner, isGeneric, lookup, genericType);
            Binding binding = bind(registrationList, eventType, handler, owner, true);
            prepared.add(new ManagedListener(callback.toGenericString(), () -> installManaged(List.of(binding))));
        }
        if (prepared.isEmpty())
            throw new IllegalArgumentException("No legacy subscribers for " + eventType.getName()
                + " on " + SubscriberMethod.typeOf(target).getName());
        return List.copyOf(prepared);
    }

    /** Internal entry point for Cleanroom's managed legacy programmatic registration. */
    @HideFromUserdev
    public <E extends Event> Runnable addManagedListener(Class<E> eventType, @Nullable ListenerList registrationList,
            EventPriority priority, boolean receiveCanceled, @Nullable Type genericType, Consumer<? super E> listener,
            ModContainer owner, String source)
    {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(priority, "priority");
        EventShape shape = EventShape.of(eventType);
        if (genericType != null && !IGenericEvent.class.isAssignableFrom(eventType))
            throw new IllegalArgumentException(eventType.getName() + " does not implement IGenericEvent");
        ListenerList list = shape.registrationList(registrationList);
        var handler = new ASMEventHandler(event -> listener.accept(eventType.cast(event)), priority, receiveCanceled,
            owner, genericType, source);
        return installManaged(List.of(bind(list, eventType, handler, owner, true)));
    }

    private static Class<?> eventParameter(Method method)
    {
        return eventParameter(method, method.getParameterTypes());
    }

    private static Class<?> eventParameter(Method method, Class<?>[] parameters)
    {
        if (parameters.length != 1)
        {
            throw new IllegalArgumentException(
                "Method " + method + " has @SubscribeEvent annotation, but requires " + parameters.length
                    + " arguments.  Event handler methods must require a single argument."
            );
        }
        Class<?> eventType = parameters[0];
        if (!Event.class.isAssignableFrom(eventType))
            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
        return eventType;
    }

    private static Binding prepare(Class<?> eventType, Object target, Method source, Method callback,
                                   ModContainer owner, @Nullable MethodHandles.Lookup lookup)
    {
        ListenerList list = EventShape.of(eventType).registrationList(null);
        var handler = new ASMEventHandler(target, source, callback, owner,
            IGenericEvent.class.isAssignableFrom(eventType), lookup);
        return bind(list, eventType, handler, owner, lookup != null);
    }

    private static Binding bind(ListenerList list, Class<?> eventType, ASMEventHandler handler, ModContainer owner,
                                boolean managed)
    {
        boolean setEventOwner = IContextSetter.class.isAssignableFrom(eventType);
        IEventListener listener = managed || setEventOwner ? new ContextSetterEventListener(owner, handler, setEventOwner) : handler;
        return new Binding(list, handler.getPriority(), listener);
    }

    // Called with the registry lock held. Prepare outside this lock because it can run user code.
    private ArrayList<IEventListener> installLegacy(Object target, @Nullable ArrayList<IEventListener> entries, Binding binding)
    {
        try
        {
            install(binding);
            if (entries == null)
            {
                entries = new ArrayList<>();
                entries.add(binding.listener());
                listeners.put(target, entries);
            }
            else
            {
                entries.add(binding.listener());
            }
            return entries;
        }
        catch (Exception | Error failure)
        {
            rollback(binding, failure);
            throw failure;
        }
    }

    private void install(Binding binding)
    {
        binding.list().register(busID, binding.priority(), binding.listener());
    }

    private void install(List<Binding> bindings)
    {
        int index = 0;
        try
        {
            for (; index < bindings.size(); index++)
                install(bindings.get(index));
        }
        catch (Exception | Error failure)
        {
            for (int rollback = index; rollback >= 0; rollback--)
                rollback(bindings.get(rollback), failure);
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
        ListenerList.unregisterRegistered(busID, binding.listener(), false);
    }

    private void rollback(Binding binding, Throwable failure)
    {
        try
        {
            ListenerList.unregisterRegistered(busID, binding.listener(), true);
        }
        catch (Exception | Error cleanup)
        {
            if (cleanup != failure)
                failure.addSuppressed(cleanup);
        }
    }

    private static void logRegistrationFailure(ModContainer owner, Class<?> eventType, Method method, Exception failure)
    {
        FMLLog.log.error("Error registering event handler: {} {} {}", owner, eventType, method, failure);
    }

    private record Binding(ListenerList list, EventPriority priority, IEventListener listener) {}

    // Some mods call this signature reflectively with a callback that accepts Object.
    private void register(Class<?> eventType, Object target, Method method, final ModContainer owner)
    {
        try
        {
            Objects.requireNonNull(target, "target");
            Binding binding = prepare(eventType, target, method, method, owner, null);
            synchronized (ListenerList.REGISTRY_LOCK)
            {
                installLegacy(target, listeners.get(target), binding);
            }
        }
        catch (Exception failure)
        {
            logRegistrationFailure(owner, eventType, method, failure);
        }
    }

    public void unregister(Object object)
    {
        Objects.requireNonNull(object, "object");
        synchronized (ListenerList.REGISTRY_LOCK)
        {
            ArrayList<IEventListener> entries = listeners.get(object);
            if (entries == null)
                return;
            for (IEventListener listener : entries)
            {
                if (!ListenerList.isIndexedListener(listener))
                    throw new UnsupportedOperationException("Custom IEventListener entries in EventBus.listeners are unsupported");
            }
            listeners.remove(object);
            for (IEventListener listener : entries)
                ListenerList.unregisterRegistered(busID, listener, false);
        }
    }

    @HideFromUserdev
    public boolean hasManagedListeners(Class<? extends Event> eventType, @Nullable ListenerList list)
    {
        if (shutdown)
            return false;
        ListenerList selected = EventShape.of(eventType).probeList(list);
        return selected == null || selected.getListeners(busID).length != 0;
    }

    @HideFromUserdev
    public boolean hasManagedListeners(Event event)
    {
        return !shutdown && event.getListenerList().getListeners(busID).length != 0;
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
        return Event.isCancelableAndCanceled(event);
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

    record ContextSetterEventListener(ModContainer owner, ASMEventHandler handler, boolean setEventOwner)
        implements IEventListener
    {
        @Override
        public void invoke(Event event)
        {
            var frame = ModEventContext.currentFrame();
            var previous = ModEventContext.pushDispatchOwner(frame, owner);
            try
            {
                if (setEventOwner)
                    ((IContextSetter) event).setModContainer(owner);
                handler.invoke(event);
            }
            finally
            {
                ModEventContext.restoreDispatchOwner(frame, previous);
            }
        }

        @Override
        public String toString()
        {
            return "ContextSetter[owner=" + owner + ", listener=" + handler + "]";
        }
    }
}
