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



/**
 * Handler for animation events. This interface is used to define a contract for classes that
 * will handle animation events.
 *
 * @param <T> The type of the instance that will be passed to the handleEvents method.
 */
public interface IEventHandler<T>
{
    /**
     * Handles animation events for a specific instance.
     *
     * @param instance The instance for which the events are being handled.
     * @param time The current time in the animation.
     * @param pastEvents An iterable collection of events that have occurred in the past.
     */
    void handleEvents(T instance, float time, Iterable<Event> pastEvents);
}
