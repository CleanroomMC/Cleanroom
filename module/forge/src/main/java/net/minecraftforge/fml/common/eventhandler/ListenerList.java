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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class ListenerList
{
    static final Object REGISTRY_LOCK = new Object();
    private static final List<ListenerList> allLists = new ArrayList<>();
    // Reverse index for listeners installed through EventBus, also updated by public
    // ListenerList registrations. The extra counts cover copies and repeated entries.
    private static final Map<IEventListener, ListenerListInst> registrations = new IdentityHashMap<>();
    private static final Map<IEventListener, Map<ListenerListInst, Integer>> extraRegistrations = new IdentityHashMap<>();
    private static int maxSize;

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
            expanded[index] = new ListenerListInst(this, index, parent == null ? null : parent.lists[index]);
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
        lists[id].register(priority, listener);
    }

    public void unregister(int id, IEventListener listener)
    {
        lists[id].unregister(listener);
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

    static boolean isIndexedListener(IEventListener listener)
    {
        return listener instanceof ASMEventHandler || listener instanceof EventBus.ContextSetterEventListener;
    }

    static void unregisterRegistered(int id, IEventListener listener, boolean rollback)
    {
        synchronized (REGISTRY_LOCK)
        {
            ListenerListInst first = registrations.get(listener);
            var extra = extraRegistrations.get(listener);
            if (extra != null)
            {
                // Removing an entry also updates the index, so visit a snapshot of the copies.
                for (ListenerListInst node : extra.keySet().toArray(ListenerListInst[]::new))
                {
                    if (node != first && node.busID == id)
                        node.unregister(listener, rollback);
                }
            }
            if (first != null && first.busID == id)
                first.unregister(listener, rollback);
        }
    }

    private static class ListenerListInst
    {
        private static final EventPriority[] PRIORITIES = EventPriority.values();
        private static final IEventListener[] EMPTY = new IEventListener[0];

        private final ListenerList owner;
        private final int busID;
        private final ArrayList<IEventListener>[] priorities;
        private ListenerListInst parent;
        private List<ListenerListInst> children;
        private volatile IEventListener[] listeners;

        @SuppressWarnings("unchecked")
        private ListenerListInst(ListenerList owner, int busID, ListenerListInst parent)
        {
            this.owner = owner;
            this.busID = busID;
            priorities = (ArrayList<IEventListener>[]) new ArrayList<?>[PRIORITIES.length];
            this.parent = parent;
            if (parent != null)
            {
                if (parent.children == null)
                    parent.children = new ArrayList<>();
                parent.children.add(this);
            }
        }

        public void dispose()
        {
            synchronized (REGISTRY_LOCK)
            {
                for (var local : priorities)
                {
                    if (local != null)
                    {
                        for (int index = 0; index < local.size(); index++)
                            forget(local.get(index));
                    }
                }
                Arrays.fill(priorities, null);
                invalidate();
                if (parent != null && parent.children != null)
                    parent.children.remove(this);
                parent = null;
                children = null;
                listeners = EMPTY;
            }
        }

        public List<IEventListener> getListeners(EventPriority priority)
        {
            synchronized (REGISTRY_LOCK)
            {
                int ordinal = priority.ordinal();
                ArrayList<IEventListener> result = null;
                for (ListenerListInst node = this; node != null; node = node.parent)
                {
                    var local = node.priorities[ordinal];
                    if (local != null && !local.isEmpty())
                    {
                        if (result == null)
                            result = new ArrayList<>(local);
                        else
                            result.addAll(local);
                    }
                }
                return result == null ? List.of() : result;
            }
        }

        public IEventListener[] getListeners()
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

        protected boolean shouldRebuild()
        {
            return listeners == null;
        }

        protected void forceRebuild()
        {
            synchronized (REGISTRY_LOCK)
            {
                invalidate();
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

        public void register(EventPriority priority, IEventListener listener)
        {
            synchronized (REGISTRY_LOCK)
            {
                int ordinal = priority.ordinal();
                var local = priorities[ordinal];
                if (local == null)
                    priorities[ordinal] = local = new ArrayList<>();
                local.add(listener);
                if (isIndexedListener(listener))
                {
                    if (registrations.putIfAbsent(listener, this) != null)
                        extraRegistrations.computeIfAbsent(listener, ignored -> new IdentityHashMap<>()).merge(this, 1, Integer::sum);
                }
                invalidate();
            }
        }

        public void unregister(IEventListener listener)
        {
            synchronized (REGISTRY_LOCK)
            {
                remove(listener);
            }
        }

        private void unregister(IEventListener listener, boolean rollback)
        {
            if (rollback)
                remove(listener);
            else
                owner.unregister(busID, listener);
        }

        private void remove(IEventListener listener)
        {
            boolean removed = false;
            for (var local : priorities)
            {
                int index = local == null ? -1 : local.indexOf(listener);
                if (index >= 0)
                {
                    forget(local.remove(index));
                    removed = true;
                }
            }
            if (removed)
                invalidate();
        }

        private void forget(IEventListener listener)
        {
            ListenerListInst first = registrations.get(listener);
            if (first == null)
                return;

            var extra = extraRegistrations.get(listener);
            if (extra == null)
            {
                if (first == this)
                    registrations.remove(listener);
                return;
            }

            Integer count = extra.get(this);
            if (count == null && first == this)
            {
                ListenerListInst replacement = extra.keySet().iterator().next();
                registrations.put(listener, replacement);
                replacement.forget(listener);
            }
            else if (count != null)
            {
                if (count == 1)
                    extra.remove(this);
                else
                    extra.put(this, count - 1);
                if (extra.isEmpty())
                    extraRegistrations.remove(listener);
            }
        }
    }
}
