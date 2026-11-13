package net.minecraftforge.registries;

import com.cleanroommc.cleanroom.common.component.IItemState;
import com.cleanroommc.cleanroom.common.component.ItemStateContainer;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.ObjectIntIdentityMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Runtime numbering of item states ({@code Item.ITEM_STATE_IDS}), mirroring {@link BlockStateIdMap}.
 * Every valid state of every registered {@link Item} gets a consecutive dense number: items in
 * registry ID order, each item's states in {@code getItemState().getValidStates()} order.
 */
public final class ItemStateIdMap extends ObjectIntIdentityMap<IItemState>
{
    private record Table(Object token, Reference2IntOpenHashMap<IItemState> ids, IItemState[] states) {}

    private final ForgeRegistry<Item> registry;
    private final boolean storeInStates;
    @Nullable
    private volatile Table table;

    ItemStateIdMap(ForgeRegistry<Item> registry, boolean storeInStates)
    {
        super(0);
        this.registry = registry;
        this.storeInStates = storeInStates;
    }

    public synchronized void invalidate()
    {
        table = null;
    }

    private Table table()
    {
        Table t = table;
        return t != null && t.token == registry.numberingToken() ? t : rebuild();
    }

    private synchronized Table rebuild()
    {
        Table t = table;
        Object token = registry.numberingToken();
        if (t != null && t.token == token)
            return t;
        Reference2IntOpenHashMap<IItemState> ids = new Reference2IntOpenHashMap<>();
        ids.defaultReturnValue(-1);
        List<IItemState> states = new ArrayList<>();
        for (Item item : registry)
        {
            for (IItemState state : item.getItemState().getValidStates())
            {
                if (ids.putIfAbsent(state, states.size()) < 0)
                    states.add(state);
            }
        }
        IItemState[] byId = states.toArray(new IItemState[0]);
        if (storeInStates)
        {
            for (int i = 0; i < byId.length; i++)
            {
                if (byId[i] instanceof ItemStateContainer.StateImplementation impl)
                    impl.runtimeStateId = i;
            }
        }
        t = new Table(token, ids, byId);
        table = t;
        return t;
    }

    @Override
    public void put(IItemState key, int value)
    {
        throw new UnsupportedOperationException("Item state ids are assigned from the item registry; register the item instead of adding states to Item.ITEM_STATE_IDS.");
    }

    @SuppressWarnings("deprecation")
    @Override
    public int get(IItemState key)
    {
        Table t = table();
        if (key == null)
            return -1;
        if (storeInStates && key instanceof ItemStateContainer.StateImplementation impl)
        {
            int id = impl.runtimeStateId;
            if (id >= 0 && id < t.states.length && t.states[id] == key)
                return id;
        }
        int id = t.ids.getInt(key);
        if (id >= 0)
            return id;
        Item item = key.getItem();
        return t.ids.getInt(item.getItemStateFromMeta(item.getMetaFromItemState(key)));
    }

    @Nullable
    @Override
    public IItemState getByValue(int value)
    {
        IItemState[] states = table().states;
        return value >= 0 && value < states.length ? states[value] : null;
    }

    @Override
    public Iterator<IItemState> iterator()
    {
        return Collections.unmodifiableList(Arrays.asList(table().states)).iterator();
    }

    @Override
    public int size()
    {
        return table().states.length;
    }
}
