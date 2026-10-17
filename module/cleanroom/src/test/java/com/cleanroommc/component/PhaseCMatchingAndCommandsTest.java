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

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class PhaseCMatchingAndCommandsTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void numericStringLookupsReturnNull() {
        assertThat(Item.getByNameOrId("minecraft:apple")).isNotNull();
        assertThat(Item.getByNameOrId("1")).isNull();
        assertThat(Item.getByNameOrId("260")).isNull();
        assertThat(Block.getBlockFromName("minecraft:stone")).isNotNull();
        assertThat(Block.getBlockFromName("1")).isNull();
        assertThat(Block.getBlockFromName("35")).isNull();
    }

    @Test
    void bracketComponentSpecParsesAndApplies() throws Exception {
        DataComponentPredicate pred = CommandBase.parseItemComponents("minecraft:diamond_sword[damage=42,!max_damage]");
        assertThat(pred.isEmpty()).isFalse();
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        pred.applyTo(sword);
        assertThat(sword.getDamageValue()).isEqualTo(42);
        assertThat(sword.has(DataComponents.MAX_DAMAGE)).isFalse();
        assertThat(pred.test(sword)).isTrue();

        ItemStack freshSword = new ItemStack(Items.DIAMOND_SWORD);
        assertThat(pred.test(freshSword)).isFalse();
    }

    @Test
    void inlineBlockStateSpecExtracts() {
        assertThat(CommandBase.extractInlineBlockStateSpec("minecraft:stone[variant=granite]"
        )).isEqualTo("variant=granite");
        assertThat(CommandBase.extractInlineBlockStateSpec("minecraft:stone")).isNull();
    }

    @Test
    void ingredientMatchesComponentPredicateAndPatchedStacks() throws Exception {
        DataComponentPredicate damaged25 = DataComponentPredicate.parseBracketSpec("[damage=25]");
        Ingredient ing = Ingredient.of(Items.DIAMOND_SWORD, damaged25);
        assertThat(ing.isSimple()).isFalse();

        ItemStack sword25 = new ItemStack(Items.DIAMOND_SWORD);
        sword25.setDamageValue(25);
        ItemStack sword10 = new ItemStack(Items.DIAMOND_SWORD);
        sword10.setDamageValue(10);

        assertThat(ing.apply(sword25)).isTrue();
        assertThat(ing.apply(sword10)).isFalse();

        ItemStack customMax = new ItemStack(Items.DIAMOND_SWORD);
        customMax.set(DataComponents.MAX_DAMAGE, 5000);
        Ingredient stackIng = Ingredient.fromStacks(customMax);
        assertThat(stackIng.apply(new ItemStack(Items.DIAMOND_SWORD))).isFalse();
        ItemStack matchingCandidate = new ItemStack(Items.DIAMOND_SWORD);
        matchingCandidate.set(DataComponents.MAX_DAMAGE, 5000);
        assertThat(stackIng.apply(matchingCandidate)).isTrue();
    }

    @Test
    void advancementItemPredicateMatchesComponentsJson() {
        String json = "{\"item\":\"minecraft:diamond_sword\",\"components\":{\"cleanroom:damage\":15,\"!cleanroom:max_damage\":{}}}";
        ItemPredicate pred = ItemPredicate.deserialize(JsonParser.parseString(json));
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        assertThat(pred.test(stack)).isFalse();
        stack.setDamageValue(15);
        assertThat(pred.test(stack)).isFalse();
        stack.remove(DataComponents.MAX_DAMAGE);
        assertThat(pred.test(stack)).isTrue();
    }

    @Test
    void lootSetComponentsFunctionApplies() {
        assertThat(LootFunctionManager.getSerializerForName(new ResourceLocation("set_components"))).isNotNull();
        DataComponentPredicate pred = DataComponentPredicate.builder()
            .expect(DataComponents.LEGACY_VARIANT, 14)
            .build();
        LootFunctionManager.SetComponents fn = new LootFunctionManager.SetComponents(new LootCondition[0], pred);
        ItemStack dye = new ItemStack(Items.DYE);
        fn.apply(dye, new Random(0L), (LootContext) null);
        assertThat(dye.get(DataComponents.LEGACY_VARIANT)).isEqualTo(Integer.valueOf(14));
        assertThat(dye.getItem().getModelKey(dye)).isEqualTo(14);
    }

    @Test
    void itemMapUsesMapIdComponentAndSyncsLegacyDamage() throws Exception {
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        net.minecraft.item.ItemMap.setMapId(map, 123456);
        assertThat(net.minecraft.item.ItemMap.getMapId(map)).isEqualTo(123456);
        assertThat(map.get(DataComponents.MAP_ID)).isEqualTo(Integer.valueOf(123456));
        ItemStack reloaded = new ItemStack(map.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
        assertThat(net.minecraft.item.ItemMap.getMapId(reloaded)).isEqualTo(123456);
        java.util.List<String> summary = com.cleanroommc.ids.CommandCleanroom.summarizeAll();
        assertThat(summary.isEmpty()).isFalse();
        assertThat(summary.get(0).contains("block states")).isTrue();
        assertThat(summary.get(0).contains("item states")).isTrue();
        ItemStack redWool = net.minecraftforge.fml.common.registry.GameRegistry.makeItemStack("minecraft:wool[color=red]",
            0, 1, null
        );
        assertThat(redWool.getMetadata()).isEqualTo(14);
        assertThat(DataComponentPredicate.parseBracketSpec("[color=red]").test(redWool)).isTrue();
        ItemStack charcoal = net.minecraftforge.fml.common.registry.GameRegistry.makeItemStack("minecraft:coal[type=charcoal]",
            0, 1, null
        );
        assertThat(charcoal.getMetadata()).isEqualTo(1);
        assertThat(DataComponentPredicate.parseBracketSpec("[type=charcoal]").test(charcoal)).isTrue();
    }

}
