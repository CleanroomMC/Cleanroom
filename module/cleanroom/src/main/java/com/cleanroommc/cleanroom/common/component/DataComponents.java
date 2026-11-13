/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.component;

import com.cleanroommc.util.CleanroomLog;

import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The data component types the engine itself uses. Registered in {@code cleanroom:data_component_types}
 * during the normal {@link RegistryEvent.Register} sequence (the class is put on the event bus by
 * {@code IdFormatContainer} at construction).
 * <p>
 * There is deliberately no {@code custom_data}: {@code ItemStack.stackTagCompound} is already 1.12's
 * free-form stack NBT and stays exactly as it is.
 */
public final class DataComponents {

    private static final NbtCodec<Integer> INT_NBT = new NbtCodec<Integer>() {

        @Override
        public NBTBase write(Integer value) {
            return new NBTTagInt(value);
        }

        @Override
        public Integer read(NBTBase nbt) throws ComponentFormatException {
            if (nbt instanceof NBTPrimitive) {
                return ((NBTPrimitive) nbt).getInt();
            }
            throw new ComponentFormatException("expected a numeric tag for an integer component, got " +
                    (nbt == null ? "null" : nbt.getClass().getSimpleName())
            );
        }

    };

    private static final NetCodec<Integer> INT_NET = new NetCodec<Integer>() {

        @Override
        public void write(PacketBuffer buf, Integer value) {
            buf.writeVarInt(value);
        }

        @Override
        public Integer read(PacketBuffer buf) {
            return buf.readVarInt();
        }

    };

    private static final NbtCodec<IBlockState> STATE_NBT = new NbtCodec<IBlockState>() {

        @Override
        public NBTBase write(IBlockState value) {
            return NBTUtil.writeBlockState(new NBTTagCompound(), value);
        }

        @Override
        public IBlockState read(NBTBase nbt) throws ComponentFormatException {
            if (nbt instanceof NBTTagCompound compound) {
                return NBTUtil.readBlockState(compound);
            }
            throw new ComponentFormatException("expected a compound tag for a block-state component, got " +
                    (nbt == null ? "null" : nbt.getClass().getSimpleName())
            );
        }

    };

    private static final NetCodec<IBlockState> STATE_NET = new NetCodec<IBlockState>() {

        @Override
        public void write(PacketBuffer buf, IBlockState value) {
            buf.writeVarInt(value.getStateId());
        }

        @Override
        public IBlockState read(PacketBuffer buf) {
            return Block.getStateById(buf.readVarInt());
        }

    };

    private static final NbtCodec<IItemState> ITEM_STATE_NBT = new NbtCodec<IItemState>() {

        @Override
        public NBTBase write(IItemState value) {
            return ItemStateContainer.writeToNBT(new NBTTagCompound(), value);
        }

        @Override
        public IItemState read(NBTBase nbt) throws ComponentFormatException {
            if (nbt instanceof NBTTagCompound compound) {
                return ItemStateContainer.readFromNBT(compound);
            }
            throw new ComponentFormatException("expected a compound tag for an item-state component, got " +
                    (nbt == null ? "null" : nbt.getClass().getSimpleName())
            );
        }

    };

    private static final NetCodec<IItemState> ITEM_STATE_NET = new NetCodec<IItemState>() {

        @Override
        public void write(PacketBuffer buf, IItemState value) {
            buf.writeVarInt(value.getStateId());
        }

        @Override
        public IItemState read(PacketBuffer buf) {
            return Item.getStateById(buf.readVarInt());
        }

    };

    /* Item durability damage; also the legacy {@code Damage} tag's destination on damageable items. */
    public static final DataComponentType<Integer> DAMAGE = register("damage",
        DataComponentType.<Integer>builder().persistent(INT_NBT).networkSynchronized(INT_NET)
    );
    /* Maximum durability; a damageable item's prototype default. */
    public static final DataComponentType<Integer> MAX_DAMAGE = register("max_damage",
        DataComponentType.<Integer>builder().persistent(INT_NBT).networkSynchronized(INT_NET)
    );
    /* Maximum stack size; every item's prototype default. */
    public static final DataComponentType<Integer> MAX_STACK_SIZE = register("max_stack_size",
        DataComponentType.<Integer>builder().persistent(INT_NBT).networkSynchronized(INT_NET)
    );
    /* A non-damageable item's subtype number ΓÇö the legacy {@code Damage} tag's destination there. */
    public static final DataComponentType<Integer> LEGACY_VARIANT = register("legacy_variant",
        DataComponentType.<Integer>builder().persistent(INT_NBT).networkSynchronized(INT_NET)
    );
    /* Standalone {@link IBlockState} carried by an {@code ItemBlock} stack when the state is not expressible by legacy metadata. */
    public static final DataComponentType<IBlockState> BLOCK_STATE = register("block_state",
        DataComponentType.<IBlockState>builder().persistent(STATE_NBT).networkSynchronized(STATE_NET)
    );
    /* Standalone {@link IItemState} carried by an {@code ItemStack}. */
    public static final DataComponentType<IItemState> ITEM_STATE = register("item_state",
        DataComponentType.<IItemState>builder().persistent(ITEM_STATE_NBT).networkSynchronized(ITEM_STATE_NET)
    );
    /* Map data number ({@code map_<id>}) for {@link net.minecraft.item.ItemMap}. */
    public static final DataComponentType<Integer> MAP_ID = register("map_id",
        DataComponentType.<Integer>builder().persistent(INT_NBT).networkSynchronized(INT_NET)
    );

    private DataComponents() { }

    private static <T> DataComponentType<T> register(String name, DataComponentType.Builder<T> builder) {
        DataComponentType<T> type = builder.build();
        type.setRegistryName(new ResourceLocation("cleanroom", name));
        return type;
    }

    @SubscribeEvent
    public static void registerComponents(RegistryEvent.Register<DataComponentType<?>> event) {
        event.getRegistry()
            .registerAll(DAMAGE, MAX_DAMAGE, MAX_STACK_SIZE, LEGACY_VARIANT, BLOCK_STATE, ITEM_STATE, MAP_ID);
    }

    private static final Set<String> COMPONENT_WARNINGS = ConcurrentHashMap.newKeySet();

    public static PatchedDataComponentMap newMap(@Nullable Item item) {
        return new PatchedDataComponentMap(item == null ? DataComponentMap.EMPTY : item.getComponents());
    }

    public static void applyAll(PatchedDataComponentMap target, DataComponentMap source) {
        for (DataComponentType<?> type : source.keySet()) {
            applyComponent(target, type, source);
        }
    }

    private static <T> void applyComponent(PatchedDataComponentMap target, DataComponentType<T> type,
        DataComponentMap patch
    ) {
        target.set(type, patch.get(type));
    }

    public static void writeNbt(PatchedDataComponentMap components, @Nullable NBTTagCompound unknownTags,
        NBTTagCompound nbt
    ) {
        NBTTagCompound componentTags = new NBTTagCompound();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.asPatch().entrySet()) {
            DataComponentType<?> type = entry.getKey();
            NbtCodec<?> codec = type.nbtCodec();
            if (codec == null || type.getRegistryName() == null) {
                continue;
            }
            if (entry.getValue().isPresent()) {
                componentTags.setTag(type.getRegistryName().toString(),
                    writeComponentValue(codec, entry.getValue().get())
                );
            } else {
                componentTags.setTag("!" + type.getRegistryName(), new NBTTagCompound());
            }
        }
        if (unknownTags != null) {
            for (String key : unknownTags.getKeySet()) {
                componentTags.setTag(key, unknownTags.getTag(key).copy());
            }
        }
        if (!componentTags.isEmpty()) {
            nbt.setTag("Components", componentTags);
        }
    }

    @Nullable
    public static NBTTagCompound readNbt(NBTTagCompound compound, PatchedDataComponentMap target,
        @Nullable NBTTagCompound unknownTags
    ) {
        IForgeRegistry<DataComponentType<?>> registry = componentRegistry();
        Reference2ObjectArrayMap<DataComponentType<?>, Optional<?>> patch = new Reference2ObjectArrayMap<>();
        NBTTagCompound unknown = unknownTags;
        for (String key : compound.getKeySet()) {
            boolean removed = key.startsWith("!");
            String name = removed ? key.substring(1) : key;
            DataComponentType<?> type = registry == null ? null : registry.getValue(new ResourceLocation(name));
            if (type == null) {
                type = DataComponentPredicate.resolveType(name);
            }
            if (type == null ||
                type == DAMAGE ||
                type == LEGACY_VARIANT ||
                isUnresolvedStateTag(type, compound.getTag(key))) {
                if (unknown == null) {
                    unknown = new NBTTagCompound();
                }
                unknown.setTag(key, compound.getTag(key).copy());
            } else if (removed) {
                patch.put(type, Optional.empty());
            } else if (type.nbtCodec() == null) {
                if (unknown == null) {
                    unknown = new NBTTagCompound();
                }
                unknown.setTag(key, compound.getTag(key).copy());
            } else {
                try {
                    patch.put(type, Optional.of(readComponentValue(type.nbtCodec(), compound.getTag(key))));
                } catch (Exception e) {
                    if (COMPONENT_WARNINGS.add(name)) {
                        CleanroomLog.get().warn("Dropping data component {}: its NbtCodec could not read the saved value",
                            name
                        );
                    }
                }
            }
        }
        target.restorePatch(patch);
        return unknown;
    }

    public static void writePatch(ItemStack stack, PacketBuffer buf) {
        ForgeRegistry<DataComponentType<?>> registry = componentRegistry();
        int written = 0;
        int removed = 0;
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : stack.getComponents().asPatch().entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (type.netCodec() == null || (registry != null && registry.getID(type) < 0)) {
                continue;
            }
            if (entry.getValue().isPresent()) {
                written++;
            } else {
                removed++;
            }
        }
        buf.writeVarInt(written);
        if (written > 0) {
            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : stack.getComponents().asPatch().entrySet()) {
                DataComponentType<?> type = entry.getKey();
                if (type.netCodec() != null &&
                    entry.getValue().isPresent() &&
                    (registry == null || registry.getID(type) >= 0)) {
                    buf.writeVarInt(registry.getID(type));
                    writeNetComponent(type.netCodec(), buf, entry.getValue().get());
                }
            }
        }
        buf.writeVarInt(removed);
        if (removed > 0) {
            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : stack.getComponents().asPatch().entrySet()) {
                DataComponentType<?> type = entry.getKey();
                if (type.netCodec() != null &&
                    !entry.getValue().isPresent() &&
                    (registry == null || registry.getID(type) >= 0)) {
                    buf.writeVarInt(registry.getID(type));
                }
            }
        }
    }

    public static void readPatch(ItemStack stack, PacketBuffer buf) throws IOException {
        ForgeRegistry<DataComponentType<?>> registry = componentRegistry();
        Reference2ObjectArrayMap<DataComponentType<?>, Optional<?>> patch = new Reference2ObjectArrayMap<>();
        int written = buf.readVarInt();
        for (int n = 0; n < written; n++) {
            DataComponentType<?> type = componentType(registry, buf.readVarInt());
            patch.put(type, Optional.of(readNetComponent(type.netCodec(), buf)));
        }
        int removed = buf.readVarInt();
        for (int n = 0; n < removed; n++) {
            patch.put(componentType(registry, buf.readVarInt()), Optional.empty());
        }
        stack.getComponents().restorePatch(patch);
    }

    public static boolean matchesPatchedComponents(ItemStack template, ItemStack candidate) {
        return matchesPatchedComponents(template, candidate, false);
    }

    public static boolean matchesMerchantBuyItem(ItemStack offered, ItemStack required) {
        return ItemStack.areItemsEqual(offered, required) &&
            matchesPatchedComponents(required, offered) &&
            (!required.hasTagCompound() ||
                offered.hasTagCompound() &&
                    NBTUtil.areNBTEquals(required.getTagCompound(), offered.getTagCompound(), false));
    }

    @SuppressWarnings("deprecation")
    public static boolean matchesPatchedComponents(ItemStack template, ItemStack candidate,
        boolean ignoreStateComponents
    ) {
        if (!ignoreStateComponents && template.getMetadata() != 32767) {
            if (!Objects.equals(template.getComponents().get(BLOCK_STATE), candidate.getComponents().get(BLOCK_STATE)
                ) ||
                !Objects.equals(template.getComponents().get(ITEM_STATE), candidate.getComponents().get(ITEM_STATE))) {
                return false;
            }
        }
        Map<DataComponentType<?>, Optional<?>> patch = template.getComponents().asPatch();
        if (patch.isEmpty()) {
            return true;
        }
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (ignoreStateComponents && (type == BLOCK_STATE || type == ITEM_STATE)) {
                continue;
            }
            Optional<?> value = entry.getValue();
            if (value.isPresent()) {
                if (!candidate.has(type) || !Objects.equals(candidate.get(type), value.get())) {
                    return false;
                }
            } else if (candidate.has(type)) {
                return false;
            }
        }
        return true;
    }

    private static DataComponentType<?> componentType(@Nullable ForgeRegistry<DataComponentType<?>> registry, int id)
        throws IOException {
        DataComponentType<?> type = registry == null ? null : registry.getValue(id);
        if (type == null || type.netCodec() == null) {
            throw new DecoderException("Unknown or unsynchronised data component type id " + id);
        }
        return type;
    }

    @Nullable
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ForgeRegistry<DataComponentType<?>> componentRegistry() {
        return (ForgeRegistry) RegistryManager.ACTIVE.getRegistry(GameData.DATA_COMPONENT_TYPES);
    }

    private static <T> Object readComponentValue(NbtCodec<T> codec, NBTBase tag) throws ComponentFormatException {
        return codec.read(tag);
    }

    @SuppressWarnings("unchecked")
    private static <T> NBTBase writeComponentValue(NbtCodec<T> codec, Object value) {
        return codec.write((T) value);
    }

    @SuppressWarnings("unchecked")
    private static <T> void writeNetComponent(NetCodec<T> codec, PacketBuffer buf, Object value) {
        codec.write(buf, (T) value);
    }

    private static <T> Object readNetComponent(NetCodec<T> codec, PacketBuffer buf) {
        return codec.read(buf);
    }

    @SuppressWarnings("rawtypes")
    private static boolean isUnresolvedStateTag(DataComponentType<?> type, NBTBase tag) {
        if (type == BLOCK_STATE && tag instanceof NBTTagCompound compound && compound.hasKey("Name", 8)) {
            String blockName = compound.getString("Name");
            ResourceLocation rl = new ResourceLocation(blockName);
            if (!"minecraft:air".equals(blockName) && !Block.REGISTRY.containsKey(rl)) {
                return true;
            }
            if (compound.hasKey("Properties", 10)) {
                Block block = Block.REGISTRY.getObject(rl);
                NBTTagCompound props = compound.getCompoundTag("Properties");
                for (String propName : props.getKeySet()) {
                    IProperty prop = block.getBlockState().getProperty(propName);
                    if (prop == null || !prop.parseValue(props.getString(propName)).isPresent()) {
                        return true;
                    }
                }
            }
        } else if (type == ITEM_STATE && tag instanceof NBTTagCompound compound && compound.hasKey("Name", 8)) {
            String itemName = compound.getString("Name");
            ResourceLocation rl = new ResourceLocation(itemName);
            if (!"minecraft:air".equals(itemName) && !Item.REGISTRY.containsKey(rl)) {
                return true;
            }
            if (compound.hasKey("Properties", 10)) {
                Item item = Item.REGISTRY.getObject(rl);
                if (item == null) {
                    return true;
                }
                NBTTagCompound props = compound.getCompoundTag("Properties");
                for (String propName : props.getKeySet()) {
                    IProperty prop = item.getItemState().getProperty(propName);
                    if (prop == null || !prop.parseValue(props.getString(propName)).isPresent()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
