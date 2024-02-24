package net.minecraftforge.event;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/**
 * @Project Cleanroom
 * @Author Hileb
 * @Date 2024/2/24 15:54
 **/
public class CraftingFactoriesRegisterEvent extends Event {
    public CraftingFactoriesRegisterEvent(Map<ResourceLocation, IConditionFactory> m1, Map<ResourceLocation, IIngredientFactory> m2, Map<ResourceLocation, IRecipeFactory> m3){
        conditions=m1;
        ingredients=m2;
        recipes=m3;
    }
    private final Map<ResourceLocation, IConditionFactory> conditions;
    private final Map<ResourceLocation, IIngredientFactory> ingredients;
    private final Map<ResourceLocation, IRecipeFactory> recipes;
    public void register(ResourceLocation name, IConditionFactory fac) {
        conditions.put(name, fac);
    }
    public IConditionFactory unregisterC(ResourceLocation name) {
        return conditions.remove(name);
    }
    public void register(ResourceLocation name, IRecipeFactory fac) {
        recipes.put(name, fac);
    }
    public IRecipeFactory unregisterR(ResourceLocation name) {
        return recipes.remove(name);
    }
    public void register(ResourceLocation name, IIngredientFactory fac) {
        ingredients.put(name, fac);
    }
    public IIngredientFactory unregisterI(ResourceLocation name) {
        return ingredients.remove(name);
    }
}
