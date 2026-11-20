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

import com.cleanroommc.hidefromuserdev.HideFromUserdev;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Event bus that allows exceptions thrown by the exception handler to propagate.
 */
public class FMLThrowingEventBus extends EventBus implements AutoCloseable
{
    private static final String LIFECYCLE_PREFIX = "cleanroom:fml-" + UUID.randomUUID() + '-';
    private static final AtomicLong NEXT_LIFECYCLE_ID = new AtomicLong();

    @Nullable
    private final ModContainer owner;
    @Nullable
    private BusGroup lifecycleGroup;
    private final Set<Object> registrations = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean closed;

    public FMLThrowingEventBus(final SubscriberExceptionHandler exceptionHandler)
    {
        this(exceptionHandler, null);
    }

    public FMLThrowingEventBus(final SubscriberExceptionHandler exceptionHandler, @Nullable ModContainer owner)
    {
        super((exception, context) -> {
            try
            {
                exceptionHandler.handleException(exception, context);
            }
            catch (final Throwable t)
            {
                // this is a hack to break out of the catch statement in EventBus.handleSubscriberException
                throw new RuntimeException()
                {
                    @Override
                    public String toString()
                    {
                        throw t;
                    }
                };
            }
        });
        this.owner = owner;
    }

    @Override
    public void register(Object target)
    {
        synchronized (registrations)
        {
            requireOpen();
            super.register(target);
            if (registrations.add(target) && owner != null)
                FMLLog.log.debug("Registered lifecycle subscriber {} for {}", target.getClass().getName(), owner.getModId());
        }
    }

    @Override
    public void unregister(Object target)
    {
        synchronized (registrations)
        {
            super.unregister(target);
            registrations.remove(target);
        }
    }

    @Override
    public void post(Object event)
    {
        if (owner == null)
        {
            super.post(event);
            return;
        }

        var frame = ModEventContext.currentFrame();
        var previous = ModEventContext.pushDispatchOwner(frame, owner);
        try
        {
            String previousLog = ModEventContext.swapLogMod(owner.getModId());
            try
            {
                super.post(event);
            }
            finally
            {
                ModEventContext.restoreLogMod(previousLog);
            }
        }
        finally
        {
            ModEventContext.restoreDispatchOwner(frame, previous);
        }
    }

    /** Installs the owning FMLModContainer's already discovered @Mod.EventHandler callbacks. */
    @HideFromUserdev
    public <E extends RecordEvent> net.minecraftforge.eventbus.api.bus.EventBus<E> installLifecycleHandlers(
        Class<E> eventType, List<Consumer<E>> handlers)
    {
        synchronized (registrations)
        {
            requireOpen();
            BusGroup group = BusGroup.create(LIFECYCLE_PREFIX + NEXT_LIFECYCLE_ID.getAndIncrement());
            try
            {
                var bus = net.minecraftforge.eventbus.api.bus.EventBus.create(group, eventType);
                handlers.forEach(bus::addListener);
                if (lifecycleGroup != null)
                    lifecycleGroup.dispose();
                lifecycleGroup = group;
                return bus;
            }
            catch (RuntimeException | Error failure)
            {
                try
                {
                    group.dispose();
                }
                catch (RuntimeException | Error cleanup)
                {
                    if (failure != cleanup)
                        failure.addSuppressed(cleanup);
                }
                throw failure;
            }
        }
    }

    private void requireOpen()
    {
        if (closed)
            throw new IllegalStateException("FML event bus is closed");
    }

    @Override
    public void close()
    {
        synchronized (registrations)
        {
            closed = true;
            Throwable failure = null;
            var iterator = registrations.iterator();
            while (iterator.hasNext())
            {
                try
                {
                    super.unregister(iterator.next());
                    iterator.remove();
                }
                catch (RuntimeException | Error cleanup)
                {
                    failure = merge(failure, cleanup);
                }
            }
            if (lifecycleGroup != null)
            {
                try
                {
                    lifecycleGroup.dispose();
                    lifecycleGroup = null;
                }
                catch (RuntimeException | Error cleanup)
                {
                    failure = merge(failure, cleanup);
                }
            }
            if (failure != null)
                Throwables.throwIfUnchecked(failure);
        }
    }

    private static Throwable merge(@Nullable Throwable failure, Throwable cleanup)
    {
        if (failure == null)
            return cleanup;
        if (failure != cleanup)
            failure.addSuppressed(cleanup);
        return failure;
    }
}
