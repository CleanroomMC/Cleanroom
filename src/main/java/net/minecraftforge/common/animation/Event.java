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
    private final String event;
    private final float offset;

    public Event(String event, float offset)
    {
        this.event = event;
        this.offset = offset;
    }

    public String event()
    {
        return event;
    }

    public float offset()
    {
        return offset;
    }

    @Override
    public int compareTo(Event event)
    {
        return Float.compare(offset, event.offset);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(getClass()).add("event", event).add("offset", offset).toString();
    }
}
