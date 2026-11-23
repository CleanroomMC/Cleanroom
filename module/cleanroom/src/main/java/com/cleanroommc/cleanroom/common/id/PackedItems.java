/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

import com.cleanroommc.cleanroom.common.component.PatchedDataComponentMap;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The recipe book's packed item ints ({@code RecipeItemHelper.pack/unpack}, {@code Ingredient.getValidItemStacksPacked}).
 * These ints are only compared and counted inside one game session: no packet or save carries them.
 * <p>
 * Vanilla's {@code item id << 16 | meta} is an exact encoding while it fits in 31 bits (item id below 32768, meta
 * below 65536) and the stack carries no non-legacy state component, costs nothing, and keeps the values mods may
 * already compute, so it is used there. The sign bit is the only thing that decides it: it is not a limit on ids.
 * Every other stack or pair is numbered exactly, the first time it is packed, as {@code Integer.MIN_VALUE | n}.
 * Both ranges identify an item and state/meta exactly, with no collisions.
 */
@SuppressWarnings("deprecation") // Cleanroom: legacy-number uses in this file are the legacy view
public final class PackedItems {

    private static final Object2IntOpenHashMap<Key> NUMBERS = new Object2IntOpenHashMap<>();
    private static final List<Key> KEYS = new ArrayList<>();

    static {
        NUMBERS.defaultReturnValue(-1);
    }

    private PackedItems() { }

    public static int pack(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        int meta = item.getHasSubtypes() ? stack.getMetadata() : 0;
        if (stack.getComponents().asPatch().isEmpty()) {
            return pack(item, meta);
        }
        return packKey(new Key(item, meta, stack.getComponents().copy()));
    }

    public static int pack(Item item, int meta) {
        int id = Item.REGISTRY.getIDForObject(item);
        if (id >= 0 && id < 32768 && meta >= 0 && meta <= 0xFFFF) {
            return id << 16 | meta;
        }
        return packKey(new Key(item, meta, null));
    }

    /* Negative-range key (Integer.MIN_VALUE | n) for OreDictionary entries past item id 65535 or damage 32766. */
    public static int oreKey(Item item, int meta) {
        return packKey(new Key(item, meta, null));
    }

    public static void clear() {
        synchronized (KEYS) {
            NUMBERS.clear();
            KEYS.clear();
        }
    }

    private static int packKey(Key key) {
        synchronized (KEYS) {
            int n = NUMBERS.getInt(key);
            if (n < 0) {
                n = KEYS.size();
                KEYS.add(key);
                NUMBERS.put(key, n);
            }
            return Integer.MIN_VALUE | n;
        }
    }

    public static ItemStack unpack(int packed) {
        if (packed == 0) {
            return ItemStack.EMPTY;
        }
        if (packed > 0) {
            return new ItemStack(Item.getItemById(packed >> 16 & 0xFFFF), 1, packed & 0xFFFF);
        }
        Key key;
        synchronized (KEYS) {
            int n = packed & Integer.MAX_VALUE;
            key = n < KEYS.size() ? KEYS.get(n) : null;
        }
        if (key == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(key.item(), 1, key.meta());
        if (key.components() != null) {
            stack.getComponents().restorePatch(key.components().asPatch());
        }
        return stack;
    }

    private record Key(Item item, int meta, @Nullable PatchedDataComponentMap components) { }

}
