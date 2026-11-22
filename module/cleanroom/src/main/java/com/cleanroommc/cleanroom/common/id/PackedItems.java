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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/*
 * RecipeItemHelper packs (item id, damage) into one int as id << 16 | damage & 0xFFFF. That collides once item
 * ids pass 65535, overflows into the negative sign bit at item id 32768, and truncates damage past 65535. Pairs
 * that fit the positive 31-bit vanilla layout (0 <= id < 32768 and 0 <= meta <= 65535) keep it verbatim; every
 * other pair gets a session-local negative key (Integer.MIN_VALUE | n) so positive and negative encodings never
 * overlap and 0 stays reserved for ItemStack.EMPTY.
 */
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
        return pack(item, meta);
    }

    public static int pack(Item item, int meta) {
        int id = Item.REGISTRY.getIDForObject(item);
        if (id >= 0 && id < 32768 && meta >= 0 && meta <= 0xFFFF) {
            return id << 16 | meta;
        }
        return packKey(new Key(item, meta));
    }

    /* Negative-range key (Integer.MIN_VALUE | n) for OreDictionary entries past item id 65535 or damage 32766. */
    public static int oreKey(Item item, int meta) {
        return packKey(new Key(item, meta));
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
        return key == null ? ItemStack.EMPTY : new ItemStack(key.item(), 1, key.meta());
    }

    private record Key(Item item, int meta) { }

}
