/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MapPopulator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Cartesian;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Container of all valid {@link IItemState}s for a template {@link Item}, mirroring
 * {@link net.minecraft.block.state.BlockStateContainer}.
 */
public class ItemStateContainer {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");

    private final Item item;
    private final ImmutableSortedMap<String, IProperty<?>> properties;
    private final ImmutableList<IItemState> validStates;

    public ItemStateContainer(Item itemIn, IProperty<?>... propertiesIn) {
        this.item = itemIn;
        Map<String, IProperty<?>> map = Maps.newHashMap();
        for (IProperty<?> prop : propertiesIn) {
            validateProperty(itemIn, prop);
            map.put(prop.getName(), prop);
        }
        this.properties = ImmutableSortedMap.copyOf(map);

        Map<Map<IProperty<?>, Comparable<?>>, StateImplementation> stateMap = Maps.newLinkedHashMap();
        List<StateImplementation> list = Lists.newArrayList();
        for (List<Comparable<?>> combo : Cartesian.cartesianProduct(this.getAllowedValues())) {
            Map<IProperty<?>, Comparable<?>> propValues = MapPopulator.createMap(this.properties.values(), combo);
            StateImplementation state = new StateImplementation(itemIn, ImmutableMap.copyOf(propValues));
            state.ordinal = list.size();
            stateMap.put(propValues, state);
            list.add(state);
        }
        for (StateImplementation state : list) {
            state.buildPropertyValueTable(stateMap);
        }
        this.validStates = ImmutableList.copyOf(list);
    }

    public static <T extends Comparable<T>> String validateProperty(Item item, IProperty<T> property) {
        String s = property.getName();
        if (!NAME_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException("Item: " + item.getClass() + " has invalidly named property: " + s);
        }
        for (T t : property.getAllowedValues()) {
            String s1 = property.getName(t);
            if (!NAME_PATTERN.matcher(s1).matches()) {
                throw new IllegalArgumentException("Item: " + item.getClass() + " has property: " + s +
                        " with invalidly named value: " + s1
                );
            }
        }
        return s;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static NBTTagCompound writeToNBT(NBTTagCompound tag, IItemState state) {
        ResourceLocation rl = Item.REGISTRY.getNameForObject(state.getItem());
        tag.setString("Name", rl == null ? "minecraft:air" : rl.toString());
        if (!state.getProperties().isEmpty()) {
            NBTTagCompound props = new NBTTagCompound();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
                IProperty prop = entry.getKey();
                props.setString(prop.getName(), prop.getName(entry.getValue()));
            }
            tag.setTag("Properties", props);
        }
        return tag;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IItemState readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("Name", 8)) {
            return Items.AIR.getDefaultItemState();
        }
        Item item = Item.REGISTRY.getObject(new ResourceLocation(tag.getString("Name")));
        if (item == null) {
            return Items.AIR.getDefaultItemState();
        }
        IItemState state = item.getDefaultItemState();
        if (tag.hasKey("Properties", 10)) {
            NBTTagCompound props = tag.getCompoundTag("Properties");
            ItemStateContainer container = item.getItemState();
            for (String key : props.getKeySet()) {
                IProperty prop = container.getProperty(key);
                if (prop != null) {
                    Optional<?> parsed = prop.parseValue(props.getString(key));
                    if (parsed.isPresent()) {
                        state = state.withProperty(prop, (Comparable) parsed.get());
                    }
                }
            }
        }
        return state;
    }

    private List<Iterable<Comparable<?>>> getAllowedValues() {
        List<Iterable<Comparable<?>>> list = Lists.newArrayList();
        for (IProperty<?> prop : this.properties.values()) {
            list.add(((IProperty) prop).getAllowedValues());
        }
        return list;
    }

    public ImmutableList<IItemState> getValidStates() {
        return this.validStates;
    }

    public IItemState getBaseState() {
        return this.validStates.get(0);
    }

    public Item getItem() {
        return this.item;
    }

    public Collection<IProperty<?>> getProperties() {
        return this.properties.values();
    }

    @Nullable
    public IProperty<?> getProperty(String propertyName) {
        return this.properties.get(propertyName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("item", Item.REGISTRY.getNameForObject(this.item))
            .add("properties", this.properties.values())
            .toString();
    }

    public static class StateImplementation implements IItemState {

        private final Item item;
        private final ImmutableMap<IProperty<?>, Comparable<?>> properties;

        public int runtimeStateId = -1;
        public int ordinal = -1;
        protected ImmutableTable<IProperty<?>, Comparable<?>, IItemState> propertyValueTable;

        protected StateImplementation(Item itemIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn) {
            this.item = itemIn;
            this.properties = propertiesIn;
        }

        private static <T> T cyclePropertyValue(Collection<T> values, T currentValue) {
            Iterator<T> iterator = values.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(currentValue)) {
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                    return values.iterator().next();
                }
            }
            return iterator.next();
        }

        @Override
        public int getOrdinal() {
            return this.ordinal;
        }

        @Override
        public int getStateId() {
            return Item.getStateId(this);
        }

        @Override
        public Collection<IProperty<?>> getPropertyKeys() {
            return Collections.unmodifiableCollection(this.properties.keySet());
        }

        @Override
        public <T extends Comparable<T>> T getValue(IProperty<T> property) {
            Comparable<?> comparable = this.properties.get(property);
            if (comparable == null) {
                throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " +
                        this.item.getItemState()
                );
            }
            return property.getValueClass().cast(comparable);
        }

        @Override
        public <T extends Comparable<T>,
        V extends T> IItemState withProperty(IProperty<T> property, V value) {
            Comparable<?> comparable = this.properties.get(property);
            if (comparable == null) {
                throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " +
                        this.item.getItemState()
                );
            }
            if (comparable == value) {
                return this;
            }
            IItemState next = this.propertyValueTable.get(property, value);
            if (next == null) {
                throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on item " +
                        Item.REGISTRY.getNameForObject(this.item) + ", it is not an allowed value"
                );
            }
            return next;
        }

        @Override
        public <T extends Comparable<T>> IItemState cycleProperty(IProperty<T> property) {
            return this.withProperty(property, cyclePropertyValue(property.getAllowedValues(), this.getValue(property))
            );
        }

        @Override
        public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
            return this.properties;
        }

        @Override
        public Item getItem() {
            return this.item;
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return this.properties.hashCode();
        }

        public void buildPropertyValueTable(Map<Map<IProperty<?>, Comparable<?>>, StateImplementation> map) {
            if (this.propertyValueTable != null) {
                throw new IllegalStateException();
            }
            Table<IProperty<?>, Comparable<?>, IItemState> table = HashBasedTable.create();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : this.properties.entrySet()) {
                IProperty<?> prop = entry.getKey();
                for (Comparable<?> val : prop.getAllowedValues()) {
                    if (val != entry.getValue()) {
                        Map<IProperty<?>, Comparable<?>> nextMap = Maps.newHashMap(this.properties);
                        nextMap.put(prop, val);
                        table.put(prop, val, map.get(nextMap));
                    }
                }
            }
            this.propertyValueTable = ImmutableTable.copyOf(table);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(Item.REGISTRY.getNameForObject(this.item));
            if (!this.getProperties().isEmpty()) {
                sb.append('[');
                boolean first = true;
                for (Map.Entry<IProperty<?>, Comparable<?>> entry : this.getProperties().entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    IProperty prop = entry.getKey();
                    sb.append(prop.getName()).append('=').append(prop.getName(entry.getValue()));
                }
                sb.append(']');
            }
            return sb.toString();
        }

    }

}
