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
import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;

import net.minecraftforge.fml.common.ModContainer;

import org.apache.logging.log4j.ThreadContext;
import org.objectweb.asm.Type;

public class ASMEventHandler implements IEventListener
{
    private static final boolean GETCONTEXT = Boolean.parseBoolean(System.getProperty("fml.LogContext", "false"));

    private final Consumer<Event> handler;
    private final SubscribeEvent subInfo;
    private final ModContainer owner;
    private final String readable;

    @Deprecated
    public ASMEventHandler(Object target, Method method, ModContainer owner) throws Exception
    {
        this(target, method, owner, false);
    }

    public ASMEventHandler(Object target, Method method, ModContainer owner, boolean isGeneric) throws Exception
    {
        this.owner = owner;
        subInfo = method.getAnnotation(SubscribeEvent.class);
        readable = "ASM: " + target + " " + method.getName() + Type.getMethodDescriptor(method);

        var rawHandler = EventListenerFactory.createRawListener(
            method,
            Modifier.isStatic(method.getModifiers()),
            target
        );
        if (isGeneric && method.getGenericParameterTypes()[0] instanceof ParameterizedType parameterized) {
            var filter = parameterized.getActualTypeArguments()[0];
            this.handler = event -> {
                if (filter == ((IGenericEvent<?>) event).getGenericType()) {
                    rawHandler.accept(event);
                }
            };
        } else {
            this.handler = rawHandler;
        }
    }

    @Override
    public void invoke(Event event)
    {
        if (GETCONTEXT)
            ThreadContext.put("mod", owner == null ? "" : owner.getName());
        if (handler != null)
        {
            if (!event.isCancelable() || !event.isCanceled() || subInfo.receiveCanceled())
            {
                handler.accept(event);
            }
        }
        if (GETCONTEXT)
            ThreadContext.remove("mod");
    }

    public EventPriority getPriority()
    {
        return subInfo.priority();
    }

    public String toString()
    {
        return readable;
    }
}
