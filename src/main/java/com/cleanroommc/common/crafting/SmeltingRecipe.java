package com.cleanroommc.common.crafting;

import net.minecraft.item.ItemStack;

public record SmeltingRecipe(ItemStack output, int cookTime, float experience) {}