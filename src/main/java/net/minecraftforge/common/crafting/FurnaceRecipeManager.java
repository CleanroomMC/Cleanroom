package net.minecraftforge.common.crafting;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;

public class FurnaceRecipeManager {
    private static final FurnaceRecipeManager INSTANCE = new FurnaceRecipeManager();
    private final Map<Item, RecipeBucket> recipeMap = new HashMap<>();
    private final Object2FloatOpenHashMap<ItemKey> experienceMap = new Object2FloatOpenHashMap<>();

    private FurnaceRecipeManager() {}

    public static FurnaceRecipeManager instance() {
        return INSTANCE;
    }

    public void registerRecipe(Block input, ItemStack output, float experience) {
        this.registerRecipe(input, output, 200, experience);
    }

    public void registerRecipe(Block input, ItemStack output, int cookTime, float experience) {
        if (input == null ) {
            return;
        }

        ItemStack stack = new ItemStack(input, 1, Short.MAX_VALUE);
        this.registerRecipe(stack, output, cookTime, experience);
    }

    public void registerRecipe(Item input, ItemStack output, float experience) {
        this.registerRecipe(input, output, 200, experience);
    }

    public void registerRecipe(Item input, ItemStack output, int cookTime, float experience) {
        if (input == null) {
            return;
        }

        ItemStack stack = new ItemStack(input, 1, Short.MAX_VALUE);
        this.registerRecipe(stack, output, cookTime, experience);
    }

    public void registerRecipe(ItemStack input, ItemStack output, float experience) {
        this.registerRecipe(input, output, 200, experience);
    }

    public void registerRecipe(ItemStack input, ItemStack output, int cookTime, float experience) {
        if (input == null || input.isEmpty() || output == null || output.isEmpty()) {
            return;
        }

        if (getRecipe(input) != null) {
            FMLLog.log.info("Ignored smelting recipe with conflicting input: {} = {}", input, output);
            return;
        }

        RecipeBucket bucket = recipeMap.computeIfAbsent(input.getItem(), _ -> new RecipeBucket());
        SmeltingRecipe recipe = new SmeltingRecipe(output, cookTime, experience);
        int metadata = input.getMetadata();
        experienceMap.put(ItemKey.of(output), experience);

        if (metadata == Short.MAX_VALUE) {
            bucket.wildcard = recipe;
        } else {
            bucket.specific.put(metadata, recipe);
        }
    }

    @Nullable
    public SmeltingRecipe getRecipe(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        RecipeBucket bucket = recipeMap.get(input.getItem());
        if (bucket == null) {
            return null;
        }

        SmeltingRecipe recipe = bucket.specific.get(input.getMetadata());
        return recipe != null ? recipe : bucket.wildcard;
    }

    public ItemStack getOutput(ItemStack input) {
        SmeltingRecipe recipe = getRecipe(input);
        return recipe != null ? recipe.output() : ItemStack.EMPTY;
    }

    public int getCookTime(ItemStack input) {
        SmeltingRecipe recipe = getRecipe(input);
        return recipe != null ? recipe.cookTime() : 200;
    }

    public float getExperience(ItemStack input) {
        SmeltingRecipe recipe = getRecipe(input);
        if (recipe == null) {
            return 0;
        }

        ItemStack output = recipe.output();
        float itemExp = output.getItem().getSmeltingExperience(output);
        if (itemExp >= 0) {
            return itemExp;
        }
        return recipe.experience();
    }

    /**
     * @deprecated Use {@link #getExperience(ItemStack)}
     */
    @Deprecated
    public float getExperienceFromOutput(ItemStack output) {
        if (output == null || output.isEmpty()) {
            return 0;
        }

        float itemExp = output.getItem().getSmeltingExperience(output);
        if (itemExp != -1) {
            return itemExp;
        }
        return experienceMap.getOrDefault(ItemKey.of(output), 0);
    }

    public Map<ItemStack, ItemStack> getInputToOutputMap() {
        Map<ItemStack, ItemStack> map = new HashMap<>();
        for (Map.Entry<Item, RecipeBucket> entry : recipeMap.entrySet()) {
            Item item = entry.getKey();
            RecipeBucket bucket = entry.getValue();

            if (bucket.wildcard != null) {
                ItemStack input = new ItemStack(item, 1, Short.MAX_VALUE);
                ItemStack output = bucket.wildcard.output();
                map.put(input, output);
            }

            for (Map.Entry<Integer, SmeltingRecipe> spec : bucket.specific.int2ObjectEntrySet()) {
                ItemStack input = new ItemStack(item, 1, spec.getKey());
                ItemStack output = spec.getValue().output();
                map.put(input, output);
            }
        }
        return map;
    }

    private static class RecipeBucket {
        final Int2ObjectMap<SmeltingRecipe> specific = new Int2ObjectOpenHashMap<>();
        SmeltingRecipe wildcard;
    }

    private record ItemKey(Item item, int metadata) {
        public static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getMetadata());
        }
    }
}
