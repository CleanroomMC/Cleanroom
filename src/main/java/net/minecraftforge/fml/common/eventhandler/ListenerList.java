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

import java.util.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;


public class ListenerList
{
    private static ImmutableList<ListenerList> allLists = ImmutableList.of();
    private static int maxSize = 0;

    @Nullable
    private final ListenerList parent;
    private ListenerListInst[] lists = new ListenerListInst[0];

    public ListenerList()
    {
        this(null);
    }

    public ListenerList(@Nullable ListenerList parent)
    {
        // parent needs to be set before resize !
        this.parent = parent;
        extendMasterList(this);
        resizeLists(maxSize);
    }

    private synchronized static void extendMasterList(ListenerList inst)
    {
        ImmutableList.Builder<ListenerList> builder = ImmutableList.builder();
        builder.addAll(allLists);
        builder.add(inst);
        allLists = builder.build();
    }

    public static void resize(int max)
    {
        if (max <= maxSize)
        {
            return;
        }
        for (ListenerList list : allLists)
        {
            list.resizeLists(max);
        }
        maxSize = max;
    }

    public void resizeLists(int max)
    {
        if (parent != null)
        {
            parent.resizeLists(max);
        }

        if (lists.length >= max)
        {
            return;
        }

        ListenerListInst[] newList = new ListenerListInst[max];
        int x = 0;
        for (; x < lists.length; x++)
        {
            newList[x] = lists[x];
        }
        for(; x < max; x++)
        {
            if (parent != null)
            {
                newList[x] = new ListenerListInst(parent.getInstance(x));
            }
            else
            {
                newList[x] = new ListenerListInst();
            }
        }
        lists = newList;
    }

    public static void clearBusID(int id)
    {
        for (ListenerList list : allLists)
        {
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
        for (ListenerList list : allLists)
        {
            list.unregister(id, listener);
        }
    }

    private static class ListenerListInst
    {
        private static final EventPriority[] PRIORITIES = EventPriority.values();
        private static final IEventListener[] EMPTY_LISTENER_ARRAY = new IEventListener[0];

        private boolean rebuild = true;
        private IEventListener[] listeners;
        private final ArrayList<IEventListener>[] priorities;
        private ListenerListInst parent;
        private List<ListenerListInst> children;


        private ListenerListInst()
        {
            priorities = new ArrayList[PRIORITIES.length];
        }

        public void dispose()
        {
            Arrays.fill(priorities, null);
            parent = null;
            listeners = null;
            if (children != null)
                children.clear();
        }

        private ListenerListInst(ListenerListInst parent)
        {
            this();
            this.parent = parent;
            this.parent.addChild(this);
        }

        /**
         * Returns a ArrayList containing all listeners for this event,
         * and all parent events for the specified priority.
         *
         * The list is returned with the listeners for the children events first.
         *
         * @param priority The Priority to get
         * @return a list containing listeners for this event, with no guarantee on the mutability of ths list
         */
        public List<IEventListener> getListeners(EventPriority priority)
        {
            if (parent == null) {
                var byPriority = priorities[priority.ordinal()];
                return byPriority != null ? byPriority : List.of();
            }

            var fromParent = parent.getListeners(priority);
            var fromThis = priorities[priority.ordinal()];

            if (fromThis == null) {
                return fromParent;
            }

            var merged = new ArrayList<IEventListener>(fromParent.size() + fromThis.size());
            merged.addAll(fromThis);
            merged.addAll(fromParent);
            return merged;
        }

        /**
         * Returns a full list of all listeners for all priority levels.
         * Including all parent listeners.
         *
         * List is returned in proper priority order.
         *
         * Automatically rebuilds the internal Array cache if its information is out of date.
         *
         * @return Array containing listeners
         */
        public IEventListener[] getListeners()
        {
            if (shouldRebuild()) buildCache();
            return listeners;
        }

        protected boolean shouldRebuild()
        {
            return rebuild;// || (parent != null && parent.shouldRebuild());
        }

        protected void forceRebuild()
        {
            this.rebuild = true;
            if (this.children != null)
            {
                for (ListenerListInst child : this.children)
                    child.forceRebuild();
            }
        }

        private void addChild(ListenerListInst child)
        {
            if (this.children == null)
                this.children = new ArrayList<>();
            this.children.add(child);
        }

        /**
         * Rebuild the local Array of listeners, returns early if there is no work to do.
         */
        private void buildCache()
        {
            if(parent != null && parent.shouldRebuild())
            {
                parent.buildCache();
            }

            ArrayList<IEventListener> ret = new ArrayList<IEventListener>();
            for (EventPriority value : PRIORITIES)
            {
                List<IEventListener> listeners = getListeners(value);
                if (!listeners.isEmpty())
                {
                    ret.add(value); //Add the priority to notify the event of it's current phase.
                    ret.addAll(listeners);
                }
            }
            listeners = ret.toArray(EMPTY_LISTENER_ARRAY);
            rebuild = false;
        }

        public void register(EventPriority priority, IEventListener listener)
        {
            var byPriority = priorities[priority.ordinal()];

            if (byPriority == null) {
                synchronized (priorities) {
                    byPriority = priorities[priority.ordinal()]; // in case another thread already triggered initialization
                    if (byPriority == null) {
                        byPriority = new ArrayList<>();
                        priorities[priority.ordinal()] = byPriority;
                    }
                }
            }

            byPriority.add(listener);
            this.forceRebuild();
        }

        public void unregister(IEventListener listener)
        {
            for(ArrayList<IEventListener> list : priorities)
            {
                if (list != null && list.remove(listener))
                {
                    this.forceRebuild();
                }
            }
        }
    }
}
