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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Event bus that allows exceptions thrown by the exception handler to propagate.
 */
public class FMLThrowingEventBus extends EventBus implements AutoCloseable
{
    @Nullable
    private final ModContainer owner;
    @Nullable
    private final ForgeLifecycleDispatch lifecycleDispatch;
    private final Set<Object> registrations = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean closed;

    /** Creates a lifecycle bus owned by LoadController. */
    public static FMLThrowingEventBus forLifecycle(SubscriberExceptionHandler exceptionHandler, @Nullable ModContainer owner)
    {
        return new FMLThrowingEventBus(exceptionHandler, owner, true);
    }

    public FMLThrowingEventBus(final SubscriberExceptionHandler exceptionHandler)
    {
        this(exceptionHandler, null);
    }

    public FMLThrowingEventBus(final SubscriberExceptionHandler exceptionHandler, @Nullable ModContainer owner)
    {
        this(exceptionHandler, owner, false);
    }

    private FMLThrowingEventBus(SubscriberExceptionHandler exceptionHandler, @Nullable ModContainer owner, boolean lifecycle)
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
        lifecycleDispatch = lifecycle ? new ForgeLifecycleDispatch(this) : null;
    }

    @Override
    public void register(Object target)
    {
        synchronized (registrations)
        {
            if (closed)
                throw new IllegalStateException("FML event bus is closed");
            if (lifecycleDispatch == null)
                super.register(target);
            else
                lifecycleDispatch.register(target);
            if (registrations.add(target) && owner != null)
                FMLLog.log.debug("Registered lifecycle subscriber {} for {}", target.getClass().getName(), owner.getModId());
        }
    }

    @Override
    public void unregister(Object target)
    {
        synchronized (registrations)
        {
            unregisterSubscriber(target);
            registrations.remove(target);
        }
    }

    @Override
    public void post(Object event)
    {
        if (owner == null)
        {
            dispatch(event);
            return;
        }

        ModContainer previous = ModEventContext.swapOwner(owner);
        try
        {
            String previousLog = ModEventContext.swapLogMod(owner.getModId());
            try
            {
                dispatch(event);
            }
            finally
            {
                ModEventContext.restoreLogMod(previousLog);
            }
        }
        finally
        {
            ModEventContext.setOwner(previous);
        }
    }

    private void dispatch(Object event)
    {
        if (lifecycleDispatch == null)
            super.post(event);
        else
            lifecycleDispatch.post(event);
    }

    private void unregisterSubscriber(Object target)
    {
        if (lifecycleDispatch == null)
            super.unregister(target);
        else
            lifecycleDispatch.unregister(target);
    }

    @Override
    public void close()
    {
        synchronized (registrations)
        {
            closed = true;
            var iterator = registrations.iterator();
            while (iterator.hasNext())
            {
                unregisterSubscriber(iterator.next());
                iterator.remove();
            }
        }
    }
}
