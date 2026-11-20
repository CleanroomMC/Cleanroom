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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraftforge.fml.common.ModContainer;
import javax.annotation.Nullable;

public class ASMEventHandler implements IEventListener
{
    private static final EventPriority[] PRIORITIES = EventPriority.values();

    private final byte priority;
    private final boolean receiveCanceled;
    private final Consumer<Event> handler;
    @Nullable
    private final Type genericFilter;
    private final ModContainer owner;
    private final String readable;

    @Deprecated
    public ASMEventHandler(Object target, Method method, ModContainer owner) throws Exception
    {
        this(target, method, owner, false);
    }

    public ASMEventHandler(Object target, Method method, ModContainer owner, boolean isGeneric) throws Exception
    {
        this(target, method, method, owner, isGeneric, null);
    }

    ASMEventHandler(Object target, Method annotationSource, Method callback, ModContainer owner, boolean isGeneric,
                    @Nullable MethodHandles.Lookup lookup)
    {
        this(EventListenerFactory.createRawListener(callback, target, lookup),
            Objects.requireNonNull(annotationSource.getAnnotation(SubscribeEvent.class)),
            owner, isGeneric && annotationSource.getGenericParameterTypes()[0] instanceof ParameterizedType parameterized
                ? parameterized.getActualTypeArguments()[0] : null,
            "ASM: " + callback.toGenericString());
    }

    private ASMEventHandler(Consumer<Event> handler, SubscribeEvent annotation, ModContainer owner,
                            @Nullable Type genericFilter, String readable)
    {
        this(handler, annotation.priority(), annotation.receiveCanceled(), owner, genericFilter, readable);
    }

    ASMEventHandler(Consumer<Event> handler, EventPriority priority, boolean receiveCanceled, ModContainer owner,
                    @Nullable Type genericFilter, String readable)
    {
        this.handler = handler;
        this.genericFilter = genericFilter;
        this.priority = (byte) priority.ordinal();
        this.receiveCanceled = receiveCanceled;
        this.owner = owner;
        this.readable = readable;
    }

    @Override
    public void invoke(Event event)
    {
        if (!ModEventContext.LOG_LISTENER_CONTEXT)
        {
            invokeIfMatching(event);
            return;
        }
        String previous = ModEventContext.swapLogMod(owner == null ? "" : owner.getName());
        try
        {
            invokeIfMatching(event);
        }
        finally
        {
            ModEventContext.restoreLogMod(previous);
        }
    }

    private void invokeIfMatching(Event event)
    {
        if ((!event.isCancelable() || !event.isCanceled() || receiveCanceled)
            && (genericFilter == null || genericFilter == ((IGenericEvent<?>) event).getGenericType()))
        {
            handler.accept(event);
        }
    }

    public EventPriority getPriority()
    {
        return PRIORITIES[priority];
    }

    public String toString()
    {
        return readable;
    }
}
