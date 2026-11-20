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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;


public class ListenerList
{
    static final Object REGISTRY_LOCK = new Object();
    private static final List<ListenerList> allLists = new ArrayList<>();
    private static int maxSize;
    private static int nextBusID;

    @Nullable
    private final ListenerList parent;
    private volatile ListenerListInst[] lists = new ListenerListInst[0];

    public ListenerList()
    {
        this(null);
    }

    public ListenerList(@Nullable ListenerList parent)
    {
        this.parent = parent;
        synchronized (REGISTRY_LOCK)
        {
            ensureCapacity(maxSize);
            allLists.add(this);
        }
    }

    static int allocateBusID()
    {
        synchronized (REGISTRY_LOCK)
        {
            int id = nextBusID;
            resize(id + 1);
            nextBusID++;
            return id;
        }
    }

    public static void resize(int max)
    {
        synchronized (REGISTRY_LOCK)
        {
            if (max <= maxSize)
                return;
            for (ListenerList list : allLists)
                list.ensureCapacity(max);
            maxSize = max;
        }
    }

    public void resizeLists(int max)
    {
        synchronized (REGISTRY_LOCK)
        {
            ensureCapacity(max);
        }
    }

    private void ensureCapacity(int max)
    {
        ListenerListInst[] old = lists;
        if (old.length >= max)
            return;
        if (parent != null)
            parent.ensureCapacity(max);

        ListenerListInst[] expanded = Arrays.copyOf(old, max);
        for (int index = old.length; index < max; index++)
            expanded[index] = new ListenerListInst(parent == null ? null : parent.lists[index]);
        lists = expanded;
    }

    public static void clearBusID(int id)
    {
        synchronized (REGISTRY_LOCK)
        {
            for (ListenerList list : allLists)
                list.lists[id].dispose();
        }
    }

    protected ListenerListInst getInstance(int id)
    {
        return lists[id];
    }

    public IEventListener[] getListeners(int id)
    {
        return lists[id].getListeners();
    }

    public void register(int id, EventPriority priority, IEventListener listener)
    {
        synchronized (REGISTRY_LOCK)
        {
            lists[id].register(priority, listener);
        }
    }

    public void unregister(int id, IEventListener listener)
    {
        synchronized (REGISTRY_LOCK)
        {
            lists[id].unregister(listener);
        }
    }

    public static void unregisterAll(int id, IEventListener listener)
    {
        synchronized (REGISTRY_LOCK)
        {
            int size = allLists.size();
            for (int index = 0; index < size; index++)
                allLists.get(index).unregister(id, listener);
        }
    }

    private static class ListenerListInst
    {
        private static final EventPriority[] PRIORITIES = EventPriority.values();
        private static final IEventListener[] EMPTY = new IEventListener[0];

        private final ArrayList<IEventListener>[] priorities;
        private ListenerListInst parent;
        private List<ListenerListInst> children;
        private volatile IEventListener[] listeners;

        @SuppressWarnings("unchecked")
        private ListenerListInst(ListenerListInst parent)
        {
            priorities = (ArrayList<IEventListener>[]) new ArrayList<?>[PRIORITIES.length];
            this.parent = parent;
            if (parent != null)
            {
                if (parent.children == null)
                    parent.children = new ArrayList<>();
                parent.children.add(this);
            }
        }

        private void dispose()
        {
            Arrays.fill(priorities, null);
            parent = null;
            children = null;
            listeners = EMPTY;
        }

        private IEventListener[] getListeners()
        {
            IEventListener[] snapshot = listeners;
            if (snapshot != null)
                return snapshot;

            synchronized (REGISTRY_LOCK)
            {
                if (listeners == null)
                    listeners = buildCache();
                return listeners;
            }
        }

        private void invalidate()
        {
            listeners = null;
            if (children != null)
            {
                for (int index = 0; index < children.size(); index++)
                    children.get(index).invalidate();
            }
        }

        private IEventListener[] buildCache()
        {
            int size = 0;
            for (int priority = 0; priority < PRIORITIES.length; priority++)
            {
                int count = 0;
                for (ListenerListInst node = this; node != null; node = node.parent)
                {
                    var local = node.priorities[priority];
                    if (local != null)
                        count += local.size();
                }
                if (count != 0)
                    size += count + 1;
            }
            if (size == 0)
                return EMPTY;

            var snapshot = new IEventListener[size];
            int output = 0;
            for (int priority = 0; priority < PRIORITIES.length; priority++)
            {
                boolean first = true;
                for (ListenerListInst node = this; node != null; node = node.parent)
                {
                    var local = node.priorities[priority];
                    if (local == null || local.isEmpty())
                        continue;
                    if (first)
                    {
                        snapshot[output++] = PRIORITIES[priority];
                        first = false;
                    }
                    for (int index = 0; index < local.size(); index++)
                        snapshot[output++] = local.get(index);
                }
            }
            return snapshot;
        }

        private void register(EventPriority priority, IEventListener listener)
        {
            int ordinal = priority.ordinal();
            var local = priorities[ordinal];
            if (local == null)
                priorities[ordinal] = local = new ArrayList<>();
            local.add(listener);
            invalidate();
        }

        private void unregister(IEventListener listener)
        {
            boolean removed = false;
            for (var local : priorities)
            {
                if (local != null && local.remove(listener))
                    removed = true;
            }
            if (removed)
                invalidate();
        }
    }
}
