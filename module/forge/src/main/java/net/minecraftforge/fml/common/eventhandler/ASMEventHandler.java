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

import static org.objectweb.asm.Type.getMethodDescriptor;

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

    @Nullable
    private final SubscribeEvent subInfo;
    private final byte priority;
    private final boolean receiveCanceled;
    private final Consumer<Event> handler;
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
        this(target, annotationSource, callback, owner, isGeneric, lookup, null);
    }

    ASMEventHandler(Object target, Method annotationSource, Method callback, ModContainer owner, boolean isGeneric,
                    @Nullable MethodHandles.Lookup lookup, @Nullable Type channelFilter)
    {
        this(Objects.requireNonNull(annotationSource.getDeclaredAnnotation(SubscribeEvent.class),
                "Subscriber method must declare legacy @SubscribeEvent"),
            EventListenerFactory.createRawListener(callback, target, lookup),
            owner, annotationFilter(annotationSource, isGeneric, channelFilter),
            "ASM: " + target + " " + callback.getName() + getMethodDescriptor(callback));
    }

    @Nullable
    private static Type annotationFilter(Method source, boolean isGeneric, @Nullable Type channelFilter)
    {
        Type filter = isGeneric && source.getGenericParameterTypes()[0] instanceof ParameterizedType parameterized
            ? parameterized.getActualTypeArguments()[0] : null;
        if (filter != null && channelFilter != null && filter != channelFilter)
            throw new IllegalArgumentException(source.toGenericString() + " selects generic type " + filter
                + " which differs by identity from the channel selector " + channelFilter);
        return channelFilter == null ? filter : channelFilter;
    }

    private ASMEventHandler(SubscribeEvent annotation, Consumer<Event> handler, ModContainer owner,
                            @Nullable Type genericFilter, String readable)
    {
        this(handler, annotation.priority(), annotation.receiveCanceled(), owner, genericFilter, readable, annotation);
    }

    ASMEventHandler(Consumer<Event> handler, EventPriority priority, boolean receiveCanceled, ModContainer owner,
                    @Nullable Type genericFilter, String readable)
    {
        this(handler, priority, receiveCanceled, owner, genericFilter, readable, null);
    }

    private ASMEventHandler(Consumer<Event> handler, EventPriority priority, boolean receiveCanceled,
                            ModContainer owner, @Nullable Type genericFilter, String readable,
                            @Nullable SubscribeEvent annotation)
    {
        this.handler = filter(handler, genericFilter);
        this.subInfo = annotation;
        this.priority = (byte) priority.ordinal();
        this.receiveCanceled = receiveCanceled;
        this.owner = owner;
        this.readable = readable;
    }

    private static Consumer<Event> filter(Consumer<Event> handler, @Nullable Type genericFilter)
    {
        if (genericFilter == null)
            return handler;
        return event -> {
            if (genericFilter == ((IGenericEvent<?>) event).getGenericType())
                handler.accept(event);
        };
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
        if (!Event.isCancelableAndCanceled(event) || receiveCanceled)
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
