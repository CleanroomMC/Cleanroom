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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;


/**
 * Base Event class that all other events are derived from
 */
public class Event
{
    @Retention(value = RUNTIME)
    @Target(value = TYPE)
    public @interface HasResult{}

    public enum Result
    {
        DENY,
        DEFAULT,
        ALLOW
    }

    private static final EventPriority[] PHASES = EventPriority.values();

    private boolean isCanceled;
    private Result result = Result.DEFAULT;
    private final ListenerList listenerList;
    private final byte properties;
    // Zero represents null. Other values are the enum ordinal plus one.
    private byte phase;

    public Event()
    {
        EventShape shape = EventShape.of(getClass());
        listenerList = shape.baseList;
        properties = shape.baseProperties;
        setup();
    }

    /**
     * Determine if this function is cancelable at all.
     * @return If access to setCanceled should be allowed
     *
     * The default implementation recognizes {@link Cancelable} on the event hierarchy.
     */
    public boolean isCancelable()
    {
        return (properties & EventShape.CANCELABLE) != 0;
    }

    /**
     * Determine if this event is canceled and should stop executing.
     * @return The current canceled state
     */
    public boolean isCanceled()
    {
        return isCanceled;
    }

    // Use the base fields only when neither cancellation getter has an override.
    static boolean isCancelableAndCanceled(Event event)
    {
        return switch (event.properties & (EventShape.CANCELABLE | EventShape.VIRTUAL_CANCELLATION))
        {
            case 0 -> false;
            case EventShape.CANCELABLE -> event.isCanceled;
            default -> event.isCancelable() && event.isCanceled();
        };
    }

    /**
     * Sets the cancel state of this event. Note, not all events are cancelable, and any attempt to
     * invoke this method on an event that is not cancelable (as determined by {@link #isCancelable}
     * will result in an {@link UnsupportedOperationException}.
     *
     * The functionality of setting the canceled state is defined on a per-event bases.
     *
     * @param cancel The new canceled value
     */
    public void setCanceled(boolean cancel)
    {
        if (!isCancelable())
        {
            throw new UnsupportedOperationException(
                "Attempted to call Event#setCanceled() on a non-cancelable event of type: "
                + this.getClass().getCanonicalName()
            );
        }
        isCanceled = cancel;
    }

    /**
     * Determines if this event expects a significant result value.
     *
     * The default implementation recognizes {@link HasResult} on the event hierarchy.
     */
    public boolean hasResult()
    {
        return (properties & EventShape.HAS_RESULT) != 0;
    }

    /**
     * Returns the value set as the result of this event
     */
    public Result getResult()
    {
        return result;
    }

    /**
     * Sets the result value for this event, not all events can have a result set, and any attempt to
     * set a result for a event that isn't expecting it will result in a IllegalArgumentException.
     *
     * The functionality of setting the result is defined on a per-event bases.
     *
     * @param value The new result
     */
    public void setResult(Result value)
    {
        result = value;
    }

    /**
     * Called by the base constructor. Events with hand-written lists can initialize them here.
     */
    protected void setup()
    {
    }

    /**
     * Returns a ListenerList object that contains all listeners
     * that are registered to this event.
     *
     * @return Listener List
     */
    public ListenerList getListenerList()
    {
        return listenerList;
    }

    @Nullable
    public EventPriority getPhase()
    {
        return phase == 0 ? null : PHASES[phase - 1];
    }

    public void setPhase(@Nonnull EventPriority value)
    {
        Preconditions.checkNotNull(value, "setPhase argument must not be null");
        int next = value.ordinal() + 1;
        if (phase >= next)
            throw new IllegalArgumentException("Attempted to set event phase to " + value + " when already " + PHASES[phase - 1]);
        phase = (byte) next;
    }
}
