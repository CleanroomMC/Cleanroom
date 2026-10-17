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

package net.minecraftforge.registries;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLContainer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;

/**
 * INTERNAL ONLY
 * MODDERS SHOULD HAVE NO REASON TO USE THIS CLASS
 */
public class GameData
{
    public static final ResourceLocation BLOCKS       = new ResourceLocation("minecraft:blocks");
    public static final ResourceLocation ITEMS        = new ResourceLocation("minecraft:items");
    public static final ResourceLocation POTIONS      = new ResourceLocation("minecraft:potions");
    public static final ResourceLocation BIOMES       = new ResourceLocation("minecraft:biomes");
    public static final ResourceLocation SOUNDEVENTS  = new ResourceLocation("minecraft:soundevents");
    public static final ResourceLocation POTIONTYPES  = new ResourceLocation("minecraft:potiontypes");
    public static final ResourceLocation ENCHANTMENTS = new ResourceLocation("minecraft:enchantments");
    public static final ResourceLocation ENTITIES     = new ResourceLocation("minecraft:entities");
    public static final ResourceLocation RECIPES      = new ResourceLocation("minecraft:recipes");
    public static final ResourceLocation PROFESSIONS  = new ResourceLocation("minecraft:villagerprofessions");
    public static final ResourceLocation SERIALIZERS  = new ResourceLocation("minecraft:dataserializers");
    public static final ResourceLocation DATA_COMPONENT_TYPES = new ResourceLocation("cleanroom:data_component_types");

    private static final ResourceLocation BLOCK_TO_ITEM         = new ResourceLocation("minecraft:blocktoitemmap");
    private static final ResourceLocation BLOCKSTATE_TO_ID      = new ResourceLocation("minecraft:blockstatetoid");
    private static final ResourceLocation ITEMSTATE_TO_ID       = new ResourceLocation("minecraft:itemstatetoid");

    private static final ResourceLocation ENTITY_CLASS_TO_ENTRY = new ResourceLocation("forge:entity_class_to_entry");
    private static final ResourceLocation SERIALIZER_TO_ENTRY   = new ResourceLocation("forge:serializer_to_entry");

    private static boolean hasInit = false;
    private static final boolean DISABLE_VANILLA_REGISTRIES = Boolean.parseBoolean(System.getProperty("forge.disableVanillaGameData", "false")); // Use for unit tests/debugging
    private static final BiConsumer<ResourceLocation, ForgeRegistry<?>> LOCK_VANILLA = (name, reg) -> reg.slaves.values().stream().filter(o -> o instanceof ILockableRegistry).forEach(o -> ((ILockableRegistry)o).lock());

    static {
        init();
    }

    public static void init()
    {
        if ( DISABLE_VANILLA_REGISTRIES)
        {
            FMLLog.bigWarning("DISABLING VANILLA REGISTRY CREATION AS PER SYSTEM VARIABLE SETTING! forge.disableVanillaGameData");
            return;
        }
        if (hasInit)
            return;
        hasInit = true;
        makeRegistry(BLOCKS,       Block.class,       new ResourceLocation("air")).addCallback(BlockCallbacks.INSTANCE).create();
        makeRegistry(ITEMS,        Item.class).addCallback(ItemCallbacks.INSTANCE).create();
        makeRegistry(POTIONS,      Potion.class).create();
        makeRegistry(BIOMES,       Biome.class).addCallback(BiomeCallbacks.INSTANCE).create();
        makeRegistry(SOUNDEVENTS,  SoundEvent.class).create();
        makeRegistry(POTIONTYPES,  PotionType.class, new ResourceLocation("empty")).create();
        makeRegistry(ENCHANTMENTS, Enchantment.class).create();
        makeRegistry(RECIPES,      IRecipe.class).disableSaving().allowModification().addCallback(RecipeCallbacks.INSTANCE).create();
        makeRegistry(PROFESSIONS,  VillagerProfession.class).create();
        makeRegistry(SERIALIZERS,  DataSerializerEntry.class).disableSaving().disableOverrides().addCallback(SerializerCallbacks.INSTANCE).create();
        entityRegistry = (ForgeRegistry<EntityEntry>)makeRegistry(ENTITIES, EntityEntry.class).addCallback(EntityCallbacks.INSTANCE).create();
        makeRegistry(DATA_COMPONENT_TYPES, com.cleanroommc.component.DataComponentType.class).disableSaving().create();
    }

    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type)
    {
        return new RegistryBuilder<T>().setName(name).setType(type).addCallback(new NamespacedWrapper.Factory<T>());
    }
    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type, ResourceLocation _default)
    {
        return new RegistryBuilder<T>().setName(name).setType(type).addCallback(new NamespacedDefaultedWrapper.Factory<T>()).setDefaultKey(_default);
    }

    public static <V extends IForgeRegistryEntry<V>> RegistryNamespacedDefaultedByKey<ResourceLocation, V> getWrapperDefaulted(Class<V> cls)
    {
        IForgeRegistry<V> reg = GameRegistry.findRegistry(cls);
        Validate.notNull(reg, "Attempted to get vanilla wrapper for unknown registry: " + cls.toString());
        @SuppressWarnings("unchecked")
        RegistryNamespacedDefaultedByKey<ResourceLocation, V> ret = reg.getSlaveMap(NamespacedDefaultedWrapper.Factory.ID, NamespacedDefaultedWrapper.class);
        Validate.notNull(ret, "Attempted to get vanilla wrapper for registry created incorrectly: " + cls.toString());
        return ret;
    }

    public static <V extends IForgeRegistryEntry<V>> RegistryNamespaced<ResourceLocation, V> getWrapper(Class<V> cls)
    {
        IForgeRegistry<V> reg = GameRegistry.findRegistry(cls);
        Validate.notNull(reg, "Attempted to get vanilla wrapper for unknown registry: " + cls.toString());
        @SuppressWarnings("unchecked")
        RegistryNamespaced<ResourceLocation, V> ret = reg.getSlaveMap(NamespacedWrapper.Factory.ID, NamespacedWrapper.class);
        Validate.notNull(ret, "Attempted to get vanilla wrapper for registry created incorrectly: " + cls.toString());
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static BiMap<Block,Item> getBlockItemMap()
    {
        return GameRegistry.findRegistry(Item.class).getSlaveMap(BLOCK_TO_ITEM, BiMap.class);
    }

    /** Runtime block state numbers, see {@link BlockStateIdMap}. */
    @SuppressWarnings("unchecked")
    public static ObjectIntIdentityMap<IBlockState> getBlockStateIDMap()
    {
        return GameRegistry.findRegistry(Block.class).getSlaveMap(BLOCKSTATE_TO_ID, ObjectIntIdentityMap.class);
    }

    /** Runtime item state numbers, see {@link ItemStateIdMap}. */
    @SuppressWarnings("unchecked")
    public static ObjectIntIdentityMap<com.cleanroommc.component.IItemState> getItemStateIDMap()
    {
        return GameRegistry.findRegistry(Item.class).getSlaveMap(ITEMSTATE_TO_ID, ObjectIntIdentityMap.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<Class<? extends Entity>, EntityEntry> getEntityClassMap()
    {
        return GameRegistry.findRegistry(EntityEntry.class).getSlaveMap(ENTITY_CLASS_TO_ENTRY, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<DataSerializer<?>, DataSerializerEntry> getSerializerMap()
    {
        return GameRegistry.findRegistry(DataSerializerEntry.class).getSlaveMap(SERIALIZER_TO_ENTRY, Map.class);
    }

    public static <K extends IForgeRegistryEntry<K>> K register_impl(K value)
    {
        Validate.notNull(value, "Attempted to register a null object");
        Validate.notNull(value.getRegistryName(), String.format("Attempt to register object without having set a registry name %s (type %s)", value, value.getClass().getName()));
        final IForgeRegistry<K> registry = GameRegistry.findRegistry(value.getRegistryType());
        Validate.notNull(registry, "Attempted to registry object without creating registry first: " + value.getRegistryType().getName());
        registry.register(value);
        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void vanillaSnapshot()
    {
        FMLLog.log.debug("Capturing vanilla baseline");
        // Every name registered so far is vanilla content: remember its current number as the baseline
        // that derived numbering pins these entries to, then lock the vanilla entries against override.
        for (Map.Entry<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> r : RegistryManager.ACTIVE.registries.entrySet())
        {
            ForgeRegistry reg = r.getValue();
            Map<ResourceLocation, Integer> baseline = new LinkedHashMap<ResourceLocation, Integer>();
            for (Object name : reg.getKeys())
                baseline.put((ResourceLocation) name, reg.getIDRaw((ResourceLocation) name));
            reg.captureVanillaBaseline(baseline);
            copyVanillaEntries(r.getKey(), reg, baseline);
        }
        RegistryManager.VANILLA.registries.forEach(LOCK_VANILLA);
        RegistryManager.ACTIVE.registries.forEach(LOCK_VANILLA);
        FMLLog.log.debug("Vanilla baseline captured");
    }


    /**
     * Builds the {@link RegistryManager#VANILLA} copy of one registry: only the baseline (vanilla)
     * names, on their baseline numbers, frozen.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends IForgeRegistryEntry<T>> void copyVanillaEntries(ResourceLocation name, ForgeRegistry<T> active, Map<ResourceLocation, Integer> baseline)
    {
        ForgeRegistry<T> vanilla = RegistryManager.VANILLA.getRegistry(name, RegistryManager.ACTIVE);
        for (Map.Entry<ResourceLocation, Integer> e : baseline.entrySet())
            vanilla.add(e.getValue().intValue(), active.getValue(e.getKey()));
        vanilla.captureVanillaBaseline(baseline);
        vanilla.freeze();
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void freezeData()
    {
        FMLLog.log.debug("Freezing registries");
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.validateContent(name));
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.freeze());

        // freeze() replaced the registration-time seeds with derived numbering: report every id that moved so listeners can re-bake
        Map<ResourceLocation, Map<ResourceLocation, Integer[]>> remaps = Maps.newHashMap();
        RegistryManager.ACTIVE.registries.forEach((name, reg) ->
        {
            if (!reg.lastRenumber().isEmpty())
                remaps.put(name, reg.lastRenumber());
        });
        Loader.instance().fireRemapEvent(remaps, true);

        FMLLog.log.debug("All registries frozen");
    }

    /**
     * Restores locally derived numbering after a connection adopted a remote order. Numbering is a pure
     * function of the registered name set, so this re-derives every ACTIVE registry in place, reports the
     * ids that moved via a remap event, and reapplies object holders.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void restoreLocalNumbering()
    {
        FMLLog.log.debug("Restoring locally derived numbering.");
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.resetDelegates());
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.renumber());

        Map<ResourceLocation, Map<ResourceLocation, Integer[]>> remaps = Maps.newHashMap();
        RegistryManager.ACTIVE.registries.forEach((name, reg) ->
        {
            if (!reg.lastRenumber().isEmpty())
                remaps.put(name, reg.lastRenumber());
        });
        Loader.instance().fireRemapEvent(remaps, true);

        ObjectHolderRegistry.INSTANCE.applyObjectHolders();
        FMLLog.log.debug("Local numbering restored.");
    }

    private static class BlockCallbacks implements IForgeRegistry.AddCallback<Block>, IForgeRegistry.ClearCallback<Block>, IForgeRegistry.CreateCallback<Block>
    {
        static final BlockCallbacks INSTANCE = new BlockCallbacks();

        @SuppressWarnings("deprecation")
        @Override
        public void onAdd(IForgeRegistryInternal<Block> owner, RegistryManager stage, int id, Block block, @Nullable Block oldBlock)
        {
            owner.getSlaveMap(BLOCKSTATE_TO_ID, BlockStateIdMap.class).invalidate();

            if (oldBlock != null)
            {
                @SuppressWarnings("unchecked")
                BiMap<Block, Item> blockToItem = owner.getSlaveMap(BLOCK_TO_ITEM, BiMap.class);
                Item item = blockToItem.get(oldBlock);
                if (item != null)
                    blockToItem.forcePut(block, item);
            }
        }

        @Override
        public void onClear(IForgeRegistryInternal<Block> owner, RegistryManager stage)
        {
            owner.getSlaveMap(BLOCKSTATE_TO_ID, BlockStateIdMap.class).invalidate();
        }

        @Override
        public void onCreate(IForgeRegistryInternal<Block> owner, RegistryManager stage)
        {
            // Only the active registry's numbers are stored in the states themselves
            owner.setSlaveMap(BLOCKSTATE_TO_ID, new BlockStateIdMap((ForgeRegistry<Block>) owner, stage == RegistryManager.ACTIVE));
            owner.setSlaveMap(BLOCK_TO_ITEM, HashBiMap.create());
        }

    }

    private static final Object BIOME_LOW_BYTE_LOCK = new Object();
    private record BiomeLowByteTable(int[][] ids, Object token) {}
    @Nullable
    private static volatile BiomeLowByteTable biomeIdsByLowByte;

    /**
     * Ids of the active biome registry whose low byte is {@code lowByte}, ascending: the biomes a byte written into
     * {@code Chunk.getBiomeArray()} can mean (one, until there are more than 256 biomes).
     */
    public static int[] getBiomeIdsWithLowByte(int lowByte)
    {
        ForgeRegistry<Biome> biomes = RegistryManager.ACTIVE.getRegistry(BIOMES);
        BiomeLowByteTable cached = biomeIdsByLowByte;
        if (cached == null || cached.token != biomes.numberingToken())
        {
            synchronized (BIOME_LOW_BYTE_LOCK)
            {
                cached = biomeIdsByLowByte;
                Object token = biomes.numberingToken();
                if (cached == null || cached.token != token)
                {
                    List<List<Integer>> lists = new ArrayList<>(256);
                    for (int i = 0; i < 256; i++)
                        lists.add(new ArrayList<>());
                    for (Biome biome : biomes) // in id order, so each list is ascending
                        lists.get(biomes.getID(biome) & 0xFF).add(biomes.getID(biome));
                    int[][] table = new int[256][];
                    for (int i = 0; i < 256; i++)
                        table[i] = lists.get(i).stream().mapToInt(Integer::intValue).toArray();
                    cached = new BiomeLowByteTable(table, token);
                    biomeIdsByLowByte = cached;
                }
            }
        }
        return cached.ids[lowByte & 0xFF];
    }

    private static void invalidateBiomeIdsByLowByte()
    {
        synchronized (BIOME_LOW_BYTE_LOCK)
        {
            biomeIdsByLowByte = null;
        }
    }

    private static class BiomeCallbacks implements IForgeRegistry.AddCallback<Biome>, IForgeRegistry.ClearCallback<Biome>
    {
        static final BiomeCallbacks INSTANCE = new BiomeCallbacks();

        @Override
        public void onAdd(IForgeRegistryInternal<Biome> owner, RegistryManager stage, int id, Biome obj, @Nullable Biome oldObj)
        {
            invalidateBiomeIdsByLowByte();
        }

        @Override
        public void onClear(IForgeRegistryInternal<Biome> owner, RegistryManager stage)
        {
            invalidateBiomeIdsByLowByte();
        }
    }

    private static class ItemCallbacks implements IForgeRegistry.AddCallback<Item>, IForgeRegistry.ClearCallback<Item>, IForgeRegistry.CreateCallback<Item>
    {
        static final ItemCallbacks INSTANCE = new ItemCallbacks();

        @Override
        public void onAdd(IForgeRegistryInternal<Item> owner, RegistryManager stage, int id, Item item, @Nullable Item oldItem)
        {
            owner.getSlaveMap(ITEMSTATE_TO_ID, ItemStateIdMap.class).invalidate();

            if (oldItem instanceof ItemBlock)
            {
                @SuppressWarnings("unchecked")
                BiMap<Block, Item> blockToItem = owner.getSlaveMap(BLOCK_TO_ITEM, BiMap.class);
                blockToItem.remove(((ItemBlock)oldItem).getBlock());
            }
            if (item instanceof ItemBlock)
            {
                @SuppressWarnings("unchecked")
                BiMap<Block, Item> blockToItem = owner.getSlaveMap(BLOCK_TO_ITEM, BiMap.class);
                blockToItem.forcePut(((ItemBlock)item).getBlock(), item);
            }
        }

        @Override
        public void onClear(IForgeRegistryInternal<Item> owner, RegistryManager stage)
        {
            owner.getSlaveMap(ITEMSTATE_TO_ID, ItemStateIdMap.class).invalidate();
            owner.getSlaveMap(BLOCK_TO_ITEM, BiMap.class).clear();
        }

        @Override
        public void onCreate(IForgeRegistryInternal<Item> owner, RegistryManager stage)
        {
            owner.setSlaveMap(ITEMSTATE_TO_ID, new ItemStateIdMap((ForgeRegistry<Item>) owner, stage == RegistryManager.ACTIVE));
            // We share the blockItem map between items and blocks registries
            BiMap<?, ?> map = stage.getRegistry(BLOCKS).getSlaveMap(BLOCK_TO_ITEM, BiMap.class);
            owner.setSlaveMap(BLOCK_TO_ITEM, map);
        }
    }

    private static class RecipeCallbacks implements IForgeRegistry.ValidateCallback<IRecipe>
    {
        static final RecipeCallbacks INSTANCE = new RecipeCallbacks();

        @Override
        public void onValidate(IForgeRegistryInternal<IRecipe> owner, RegistryManager stage, int id, ResourceLocation key, IRecipe obj)
        {
            if (stage != RegistryManager.ACTIVE) return;
            // verify the recipe output yields a registered item
            Item item = obj.getRecipeOutput().getItem();
            if (!stage.getRegistry(Item.class).containsValue(item))
            {
                throw new IllegalStateException(String.format("Recipe %s (%s) produces unregistered item %s (%s)", key, obj, item.getRegistryName(), item));
            }
        }

    }


    private static ForgeRegistry<EntityEntry> entityRegistry;
    public static ForgeRegistry<EntityEntry> getEntityRegistry() { return entityRegistry; }
    public static void registerEntity(int id, ResourceLocation key, Class<? extends Entity> clazz, String oldName)
    {
        RegistryNamespaced<ResourceLocation, EntityEntry> reg = getWrapper(EntityEntry.class);
        reg.register(id, key, new EntityEntry(clazz, oldName));
    }

    private static class EntityCallbacks implements IForgeRegistry.AddCallback<EntityEntry>, IForgeRegistry.ClearCallback<EntityEntry>, IForgeRegistry.CreateCallback<EntityEntry>
    {
        static final EntityCallbacks INSTANCE = new EntityCallbacks();

        @Override
        public void onAdd(IForgeRegistryInternal<EntityEntry> owner, RegistryManager stage, int id, EntityEntry entry, @Nullable EntityEntry oldEntry)
        {
            if (entry instanceof EntityEntryBuilder.BuiltEntityEntry)
            {
                ((EntityEntryBuilder.BuiltEntityEntry) entry).addedToRegistry();
            }
            if (entry.getEgg() != null)
            {
                EntityList.ENTITY_EGGS.put(entry.getRegistryName(), entry.getEgg());
            }
            @SuppressWarnings("unchecked")
            Map<Class<? extends Entity>, EntityEntry> map = owner.getSlaveMap(ENTITY_CLASS_TO_ENTRY, Map.class);
            if (oldEntry != null)
            {
                map.remove(oldEntry.getEntityClass());
            }
            map.put(entry.getEntityClass(), entry);
        }

        @Override
        public void onClear(IForgeRegistryInternal<EntityEntry> owner, RegistryManager stage)
        {
            owner.getSlaveMap(ENTITY_CLASS_TO_ENTRY, Map.class).clear();
        }

        @Override
        public void onCreate(IForgeRegistryInternal<EntityEntry> owner, RegistryManager stage)
        {
            owner.setSlaveMap(ENTITY_CLASS_TO_ENTRY, new IdentityHashMap<>());
        }
    }

    private static class SerializerCallbacks implements IForgeRegistry.AddCallback<DataSerializerEntry>, IForgeRegistry.ClearCallback<DataSerializerEntry>, IForgeRegistry.CreateCallback<DataSerializerEntry>
    {
        static final SerializerCallbacks INSTANCE = new SerializerCallbacks();

        @Override
        public void onAdd(IForgeRegistryInternal<DataSerializerEntry> owner, RegistryManager stage, int id, DataSerializerEntry entry, @Nullable DataSerializerEntry oldEntry)
        {
            @SuppressWarnings("unchecked")
            Map<DataSerializer<?>, DataSerializerEntry> map = owner.getSlaveMap(SERIALIZER_TO_ENTRY, Map.class);
            if (oldEntry != null) map.remove(oldEntry.getSerializer());
            map.put(entry.getSerializer(), entry);
        }

        @Override
        public void onClear(IForgeRegistryInternal<DataSerializerEntry> owner, RegistryManager stage)
        {
            owner.getSlaveMap(SERIALIZER_TO_ENTRY, Map.class).clear();
        }

        @Override
        public void onCreate(IForgeRegistryInternal<DataSerializerEntry> owner, RegistryManager stage)
        {
            owner.setSlaveMap(SERIALIZER_TO_ENTRY, new IdentityHashMap<>());
        }
    }

    /**
     * Loads a world-save registry snapshot into this instance. This changes nothing about the active
     * contents or numbering: for each registry it fires {@link RegistryEvent.MissingMappings} for the
     * saved names that ACTIVE lacks, applies REMAP as an alias ({@code getValue(oldName)} and saved
     * palette entries resolve to the target), logs WARN/IGNORE at the right level, aborts on FAIL, and
     * leaves the rest to name-based retention. Old {@code dummied} names are treated as missing too.
     *
     * @return the failed mappings (empty unless a mapping was FAILed, which aborts first)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Multimap<ResourceLocation, ResourceLocation> loadWorldSnapshot(Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot)
    {
        FMLLog.log.info("Loading world registry data into this {} instance", FMLCommonHandler.instance().getEffectiveSide().isServer() ? "server" : "client");
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.validateContent(name));
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.resetDelegates());

        for (Map.Entry<ResourceLocation, ForgeRegistry.Snapshot> e : snapshot.entrySet())
        {
            ResourceLocation name = e.getKey();
            ForgeRegistry<?> reg = RegistryManager.ACTIVE.registries.get(name);
            if (reg == null)
            {
                FMLLog.log.warn("Save has registry {} which no longer exists; {} saved name(s) left to retention", name, e.getValue().ids.size());
                continue;
            }
            applySnapshotAliases(name, reg, e.getValue());
        }

        return ArrayListMultimap.create();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T extends IForgeRegistryEntry<T>> void applySnapshotAliases(ResourceLocation name, ForgeRegistry<T> reg, ForgeRegistry.Snapshot snap)
    {
        snap.aliases.forEach(reg::addAlias);

        Map<ResourceLocation, Integer> missing = Maps.newLinkedHashMap();
        snap.ids.forEach((rl, id) ->
        {
            if (!reg.containsKey(rl))
                missing.put(rl, id);
        });
        snap.dummied.forEach(rl ->
        {
            if (!reg.containsKey(rl))
                missing.putIfAbsent(rl, -1);
        });
        if (missing.isEmpty())
            return;

        MissingMappings<T> event = (MissingMappings<T>) reg.getMissingEvent(name, missing);
        MinecraftForge.EVENT_BUS.post(event);

        Multimap<ResourceLocation, ResourceLocation> failed = ArrayListMultimap.create();
        int retained = 0;
        for (MissingMappings.Mapping<T> mapping : event.getAllMappings())
        {
            switch (mapping.getAction())
            {
            case REMAP:
                T target = (T) mapping.getTarget();
                ResourceLocation newName = reg.getKey(target);
                reg.addAlias(mapping.key, newName);
                FMLLog.log.debug("Registry {} remapped {} -> {}", name, mapping.key, newName);
                break;
            case IGNORE:
                FMLLog.log.debug("Registry {} ignoring {}", name, mapping.key);
                retained++;
                break;
            case FAIL:
                FMLLog.log.error("Registry {} failed on missing {}", name, mapping.key);
                failed.put(name, mapping.key);
                break;
            case WARN:
            case DEFAULT:
            default:
                FMLLog.log.warn("Registry {}: {} is missing locally, retained by name", name, mapping.key);
                retained++;
                break;
            }
        }
        if (!failed.isEmpty())
        {
            failed.values().forEach(rl -> FMLLog.log.error("    {}", rl));
            StartupQuery.abort();
        }
        if (retained > 0)
            FMLLog.log.info("Registry {}: {} saved name(s) left to retention", name, retained);
    }

    /**
     * Removes every recipe not in {@code codeRecipes} — i.e. everything a previous JSON load added —
     * then re-freezes. Numbering is re-derived on freeze; there is no frozen copy to revert to.
     */
    /**
     * @deprecated replaced by {@link #restoreLocalNumbering()}; ids are derived, so this re-derives
     * them rather than restoring a frozen copy. Kept so old mods still link.
     */
    @Deprecated
    public static void revertToFrozen()
    {
        restoreLocalNumbering();
    }

    /**
     * @deprecated there is no frozen copy to revert to; only the recipe revert still exists, as
     * {@link CraftingHelper#revertToCodeRecipes()}. Other registries' contents never change. Kept so
     * old mods still link.
     */
    @Deprecated
    public static void revert(RegistryManager state, ResourceLocation registry, boolean lock)
    {
        if (RECIPES.equals(registry))
            CraftingHelper.revertToCodeRecipes();
        else
            FMLLog.log.warn("GameData.revert({}, {}) is a no-op: contents are never replaced", state.getName(), registry);
    }

    /**
     * @deprecated replaced by {@link #loadWorldSnapshot(Map)}; snapshots only fire MissingMappings and
     * install aliases now — nothing is injected or renumbered. Kept so old mods still link.
     */
    @Deprecated
    public static Multimap<ResourceLocation, ResourceLocation> injectSnapshot(Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot, boolean injectFrozenData, boolean isLocalWorld)
    {
        return loadWorldSnapshot(snapshot);
    }

    public static void revertRecipesToCode(Set<ResourceLocation> codeRecipes)
    {
        ForgeRegistry<IRecipe> reg = RegistryManager.ACTIVE.getRegistry(RECIPES);
        reg.unfreeze();
        for (ResourceLocation name : Lists.newArrayList(reg.getKeys()))
        {
            if (codeRecipes == null || !codeRecipes.contains(name))
                reg.remove(name);
        }
        reg.freeze();
    }

    public static void fireCreateRegistryEvents()
    {
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.NewRegistry());
    }

    public static void fireRegistryEvents()
    {
        fireRegistryEvents(rl -> true);
    }
    public static void fireRegistryEvents(Predicate<ResourceLocation> filter)
    {
        List<ResourceLocation> keys = Lists.newArrayList(RegistryManager.ACTIVE.registries.keySet());
        Collections.sort(keys, (o1, o2) -> o1.toString().compareToIgnoreCase(o2.toString()));
        /*
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> {
            if (filter.test(name))
                ((ForgeRegistry<?>)reg).unfreeze();
        });
        */

        if (filter.test(BLOCKS))
        {
            List<ResourceLocation> open = keys.stream().filter(filter).collect(Collectors.toList());
            MinecraftForge.EVENT_BUS.post(new RegistryEvent.RegisterAll(open));
            ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject anything registered through RegisterAll

            MinecraftForge.EVENT_BUS.post(RegistryManager.ACTIVE.getRegistry(BLOCKS).getRegisterEvent(BLOCKS));
            ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject any blocks
        }
        if (filter.test(ITEMS))
        {
            MinecraftForge.EVENT_BUS.post(RegistryManager.ACTIVE.getRegistry(ITEMS).getRegisterEvent(ITEMS));
            ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject any items
        }
        for (ResourceLocation rl : keys)
        {
            if (!filter.test(rl)) continue;
            if (rl == BLOCKS || rl == ITEMS) continue;
            MinecraftForge.EVENT_BUS.post(RegistryManager.ACTIVE.getRegistry(rl).getRegisterEvent(rl));
        }
        ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject everything else


        /*
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> {
            if (filter.test(name))
                ((ForgeRegistry<?>)reg).freeze();
        });
        */
    }

    /**
     * @deprecated Use {@link #checkPrefix(String, boolean)}.
     */
    @Deprecated
    public static ResourceLocation checkPrefix(String name)
    {
        return checkPrefix(name, true);
    }

    /**
     * Check a name for a domain prefix, and if not present infer it from the
     * current active mod container.
     * 
     * @param name          The name or resource location
     * @param warnOverrides If true, logs a warning if domain differs from that of
     *                      the currently currently active mod container
     * 
     * @return The {@link ResourceLocation} with given or inferred domain
     */
    public static ResourceLocation checkPrefix(String name, boolean warnOverrides)
    {
        int index = name.lastIndexOf(':');
        String oldPrefix = index == -1 ? "" : name.substring(0, index).toLowerCase(Locale.ROOT);
        name = index == -1 ? name : name.substring(index + 1);
        ModContainer mc = Loader.instance().activeModContainer();
        String prefix = mc == null || (mc instanceof InjectedModContainer && ((InjectedModContainer)mc).wrappedContainer instanceof FMLContainer) ? "minecraft" : mc.getModId().toLowerCase(Locale.ROOT);
        if (warnOverrides && !oldPrefix.equals(prefix) && oldPrefix.length() > 0)
        {
            FMLLog.log.warn("Potentially Dangerous alternative prefix `{}` for name `{}`, expected `{}`. This could be a intended override, but in most cases indicates a broken mod.", oldPrefix, name, prefix);
            prefix = oldPrefix;
        }
        return new ResourceLocation(prefix, name);
    }

    private static Field regName;
    private static void forceRegistryName(IForgeRegistryEntry<?> entry, ResourceLocation name)
    {
        if (regName == null)
        {
            try
            {
                regName = IForgeRegistryEntry.Impl.class.getDeclaredField("registryName");
                regName.setAccessible(true);
            }
            catch (NoSuchFieldException | SecurityException e)
            {
                FMLLog.log.error("Could not get `registryName` field from IForgeRegistryEntry.Impl");
                FMLLog.log.throwing(Level.ERROR, e);
                throw new RuntimeException(e);
            }
        }
        try
        {
            regName.set(entry, name);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            FMLLog.log.error("Could not set `registryName` field in IForgeRegistryEntry.Impl to `{}`", name.toString());
            FMLLog.log.throwing(Level.ERROR, e);
            throw new RuntimeException(e);
        }

    }
}
