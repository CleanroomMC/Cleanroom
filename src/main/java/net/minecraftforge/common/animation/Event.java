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

package net.minecraftforge.common.animation;

import com.google.common.base.MoreObjects;


/**
 * Event stored in the clip. This class represents an event in an animation clip.
 * It contains the name of the event and the time offset relative to the next event or the first query time.
 * The events are stored in a sorted manner based on their offsets.
 */
public final class Event implements Comparable<Event>
{
    /**
     * The name of the event.
     */
    private final String event;

    /**
     * The time offset of the event relative to the next event or the first query time.
     */
    private final float offset;

    /**
     * Constructs a new Event object with the given event name and offset.
     *
     * @param event The name of the event.
     * @param offset The time offset of the event.
     */
    public Event(String event, float offset)
    {
        this.event = event;
        this.offset = offset;
    }

    /**
     * Returns the name of the event.
     *
     * @return The name of the event.
     */
    public String event()
    {
        return event;
    }

    /**
     * Returns the time offset of the event relative to the next event or the first query time.
     *
     * @return The time offset of the event.
     */
    public float offset()
    {
        return offset;
    }

    /**
     * Compares this event with the specified event for order.
     * Events are ordered based on their offsets.
     *
     * @param event The event to be compared.
     * @return A negative integer, zero, or a positive integer as this event is less than, equal to, or greater than the specified event.
     */
    @Override
    public int compareTo(Event event)
    {
        return Float.compare(offset, event.offset);
    }

    /**
     * Returns a string representation of this event.
     * The string representation consists of the class name, followed by the event name and offset.
     *
     * @return A string representation of this event.
     */
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(getClass()).add("event", event).add("offset", offset).toString();
    }
}
