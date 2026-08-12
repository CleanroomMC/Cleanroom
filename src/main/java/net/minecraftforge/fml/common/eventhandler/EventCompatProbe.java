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

import org.jspecify.annotations.NonNull;

import java.lang.annotation.Annotation;

/**
 * Runtime replacement for the class-load-time ASM injection formerly performed by
 * {@code EventSubscriptionTransformer}. Every event class now derives its listener list and
 * its cancel/result capability from {@link ClassValue} caches instead of injected
 * {@code LISTENER_LIST} fields and constant-returning methods.
 *
 * <p>All lookups are virtual-call safe: the base implementations in {@link Event} delegate
 * here, so a handwritten override anywhere in the class hierarchy is naturally preferred by
 * the JVM's virtual dispatch and this probe never runs for it. The {@link ClassValue} fast
 * path (no allocation, no locking) keeps the steady-state cost of a post comparable to the
 * old injected {@code GETSTATIC} reads.</p>
 */
public final class EventCompatProbe
{
    private EventCompatProbe() {}

    /**
     * One {@link ListenerList} per event class, chained to the superclass list so that posting
     * a subclass event also fires listeners registered to superclass events. The base
     * {@link Event} class gets a root list with no parent. Lists are created lazily on first
     * access, and the {@link ListenerList} constructor registers each new list with the master
     * list, so {@link ListenerList#unregisterAll} and {@link ListenerList#resize} keep working
     * unchanged.
     */
    private static final ClassValue<ListenerList> LISTENER_LISTS = new ClassValue<>()
    {
        @Override
        protected ListenerList computeValue(Class<?> type)
        {
            Class<?> superclass = type.getSuperclass();
            ListenerList parent = (superclass != null && Event.class.isAssignableFrom(superclass))
                ? get(superclass)
                : null;
            return new ListenerList(parent);
        }
    };

    /**
     * Whether the given event type is cancelable, decided by walking the class hierarchy for a
     * direct {@link Cancelable} annotation. This mirrors the old transformer, which injected a
     * constant-true {@code isCancelable()} only into classes directly annotated with
     * {@code @Cancelable}; since the annotation is not {@code @Inherited}, neither was the old
     * injection, and a subclass without the annotation inherited the nearest annotated
     * supertype's constant just as this chain walk resolves it.
     */
    private static final ClassValue<Boolean> CANCELABLE = chainAnnotation(Cancelable.class);

    /**
     * Same as {@link #CANCELABLE}, but for the {@link Event.HasResult} annotation.
     */
    private static final ClassValue<Boolean> HAS_RESULT = chainAnnotation(Event.HasResult.class);

    private static ClassValue<Boolean> chainAnnotation(Class<? extends Annotation> annotation)
    {
        return new ClassValue<>()
        {
            @Override
            protected Boolean computeValue(@NonNull Class<?> type)
            {
                for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
                {
                    if (c.isAnnotationPresent(annotation))
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ListenerList listenerList(Class<?> eventType)
    {
        return LISTENER_LISTS.get(eventType);
    }

    public static boolean isCancelable(Class<?> eventType)
    {
        return CANCELABLE.get(eventType);
    }

    public static boolean hasResult(Class<?> eventType)
    {
        return HAS_RESULT.get(eventType);
    }
}
