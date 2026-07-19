package com.cleanroommc.common.crafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record ItemKey(Item item, int metadata) {

    public static ItemKey of(ItemStack stack) {
        int metadata = stack.getMetadata();
        if (!stack.getHasSubtypes()) {
            metadata = 0;
        }
        return new ItemKey(stack.getItem(), metadata);
    }

    public ItemStack toStack() {
        return new ItemStack(item, 1, metadata);
    }
}
