package net.minecraftforge.common.crafting;

import net.minecraft.item.ItemStack;

public record SmeltingRecipe(ItemStack output, int cookTime, float experience) {}