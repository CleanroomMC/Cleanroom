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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** ItemStack data components: virtual DAMAGE/LEGACY_VARIANT, patch NBT, equality and copy. */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class ItemStackComponentsTest {

    private static Item pick; // damageable
    private static Item wool; // subtype item (non-damageable)
    private static DataComponentType<String> modComponent;

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
        if (pick == null) {
            pick = new Item().setMaxDamage(250).setMaxStackSize(1);
            pick.setRegistryName(new ResourceLocation("test", "pick"));
            ForgeRegistries.ITEMS.register(pick);
            wool = new Item().setHasSubtypes(true);
            wool.setRegistryName(new ResourceLocation("test", "wool"));
            ForgeRegistries.ITEMS.register(wool);
            modComponent = DataComponentType.<String>builder().persistent(new NbtCodec<String>() {

                @Override
                public net.minecraft.nbt.NBTBase write(String value) {
                    return new NBTTagString(value);
                }

                @Override
                public String read(net.minecraft.nbt.NBTBase nbt) throws ComponentFormatException {
                    if (nbt instanceof NBTTagString) {
                        return ((NBTTagString) nbt).getString();
                    }
                    throw new ComponentFormatException("expected a string");
                }

            }).build();
            modComponent.setRegistryName(new ResourceLocation("test", "charge"));
            IForgeRegistry<DataComponentType<?>> registry = componentRegistry();
            if (registry.getValue(new ResourceLocation("cleanroom", "damage")) == null) {
                registry.registerAll(DataComponents.DAMAGE, DataComponents.MAX_DAMAGE, DataComponents.MAX_STACK_SIZE,
                    DataComponents.LEGACY_VARIANT
                );
            }
            registry.register(modComponent);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static IForgeRegistry<DataComponentType<?>> componentRegistry() {
        return (IForgeRegistry) RegistryManager.ACTIVE.getRegistry(GameData.DATA_COMPONENT_TYPES);
    }

    private static NBTTagCompound saved(String id, int damage) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("id", id);
        compound.setByte("Count", (byte) 1);
        compound.setInteger("Damage", damage);
        return compound;
    }

    @Test
    public void legacyDamageRoutesToDamageOrVariant() {
        ItemStack tool = new ItemStack(saved("test:pick", 1234));
        assertThat((int) tool.get(DataComponents.DAMAGE)).isEqualTo(1234);
        assertThat(tool.has(DataComponents.DAMAGE)).isTrue();
        assertThat(tool.has(DataComponents.LEGACY_VARIANT)).isFalse();
        assertThat(tool.getItemDamage()).isEqualTo(1234);

        ItemStack red = new ItemStack(saved("test:wool", 14));
        assertThat(red.get(DataComponents.DAMAGE)).isNull();
        assertThat((int) red.get(DataComponents.LEGACY_VARIANT)).isEqualTo(14);
        assertThat(red.has(DataComponents.LEGACY_VARIANT)).isTrue();
        assertThat(red.getMetadata()).isEqualTo(14);
    }

    @Test
    public void virtualComponentsAreNotInThePatch() {
        ItemStack tool = new ItemStack(pick, 1, 40);
        tool.set(DataComponents.DAMAGE, 50);
        assertThat(tool.getItemDamage()).isEqualTo(50);
        assertThat(tool.getComponents().asPatch().isEmpty()).isTrue();
        assertThat(tool.getComponents().isEmpty() || !tool.getComponents().asPatch().containsKey(DataComponents.DAMAGE)
        ).isTrue();

        // a saved stack with no real patch writes no Components compound
        NBTTagCompound nbt = tool.writeToNBT(new NBTTagCompound());
        assertThat(nbt.hasKey("Components")).isFalse();
        assertThat(nbt.getInteger("Damage")).isEqualTo(50);
    }

    @Test
    public void virtualSetAndRemoveValidateRouting() {
        ItemStack tool = new ItemStack(pick, 1);
        assertThatThrownBy(() -> tool.set(DataComponents.LEGACY_VARIANT, 3)).isInstanceOf(IllegalArgumentException.class
        );
        assertThatThrownBy(() -> tool.remove(DataComponents.DAMAGE)).isInstanceOf(IllegalArgumentException.class);

        ItemStack dye = new ItemStack(wool, 1);
        assertThatThrownBy(() -> dye.set(DataComponents.DAMAGE, 3)).isInstanceOf(IllegalArgumentException.class);
        dye.set(DataComponents.LEGACY_VARIANT, 9);
        assertThat(dye.getItemDamage()).isEqualTo(9);
    }

    @Test
    public void damageValueRoutesThroughTheItemHook() {
        ItemStack tool = new ItemStack(pick, 1);
        tool.setDamageValue(42);
        assertThat(tool.getDamageValue()).isEqualTo(42);
        assertThat(tool.getItemDamage()).isEqualTo(42);
    }

    @Test
    public void modComponentAndRemovedDefaultRoundTrip() {
        ItemStack stack = new ItemStack(pick, 1, 5);
        stack.set(modComponent, "charged");
        stack.remove(DataComponents.MAX_STACK_SIZE); // removal of a prototype default

        NBTTagCompound nbt = stack.writeToNBT(new NBTTagCompound());
        NBTTagCompound components = nbt.getCompoundTag("Components");
        assertThat(components.getString("test:charge")).isEqualTo("charged");
        assertThat(components.hasKey("!cleanroom:max_stack_size")).isTrue();

        ItemStack back = new ItemStack(nbt);
        assertThat(back.get(modComponent)).isEqualTo("charged");
        assertThat(back.has(DataComponents.MAX_STACK_SIZE)).isFalse();
        assertThat(back.getDamageValue()).isEqualTo(5);
        assertThat(ItemStack.areItemStacksEqual(stack, back)).isTrue();
    }

    @Test
    public void unknownComponentNamesSurviveARoundTrip() {
        ItemStack stack = new ItemStack(pick, 1);
        NBTTagCompound nbt = stack.writeToNBT(new NBTTagCompound());
        NBTTagCompound components = new NBTTagCompound();
        components.setInteger("gone_mod:flux", 99);
        components.setTag("gone_mod:removed_default", new NBTTagCompound());
        nbt.setTag("Components", components);

        ItemStack back = new ItemStack(nbt);
        assertThat(back.getComponents().get(modComponent)).isNull();
        NBTTagCompound out = back.writeToNBT(new NBTTagCompound());
        assertThat(out.getCompoundTag("Components")).isEqualTo(components);
    }

    @Test
    public void badCodecValueDropsOnlyThatComponent() {
        ItemStack stack = new ItemStack(pick, 1);
        NBTTagCompound nbt = stack.writeToNBT(new NBTTagCompound());
        NBTTagCompound components = new NBTTagCompound();
        components.setInteger("test:charge", 7); // charge expects a string -> read fails
        components.setInteger("cleanroom:damage", 4); // virtual: kept verbatim, never read into the patch
        nbt.setTag("Components", components);

        ItemStack back = new ItemStack(nbt);
        assertThat(back.get(modComponent)).isNull(); // dropped, not fatal
        assertThat(back).isNotNull();
    }

    @Test
    public void equalityMatrix() {
        ItemStack a = new ItemStack(pick, 1, 10);
        ItemStack b = new ItemStack(pick, 1, 20);
        assertThat(ItemStack.areItemsEqual(a, b)).isFalse();
        assertThat(ItemStack.areItemsEqualIgnoreDurability(a, b)).isTrue();
        assertThat(ItemStack.areItemStacksEqual(a, b)).isFalse();

        ItemStack red = new ItemStack(wool, 1, 14);
        ItemStack blue = new ItemStack(wool, 1, 11);
        assertThat(ItemStack.areItemsEqual(red, blue)).isFalse();
        assertThat(ItemStack.areItemStacksEqual(red, blue)).isFalse();

        ItemStack charged = new ItemStack(pick, 1, 10);
        charged.set(modComponent, "x");
        assertThat(ItemStack.areItemStacksEqual(a, charged)).isFalse();
        assertThat(ItemStack.areItemStackTagsEqual(a, charged)).isFalse();

        ItemStack named = new ItemStack(pick, 1, 10);
        named.setStackDisplayName("Fancy");
        assertThat(ItemStack.areItemStacksEqual(a, named)).isFalse();
        assertThat(ItemStack.areItemStackTagsEqual(a, named)).isFalse();

        ItemStack a2 = new ItemStack(pick, 1, 10);
        assertThat(ItemStack.areItemsEqual(a, a2)).isTrue();
        assertThat(ItemStack.areItemStacksEqual(a, a2)).isTrue();
        assertThat(ItemStack.areItemStackTagsEqual(a, a2)).isTrue();
    }

    @Test
    public void copiesHaveIndependentPatches() {
        ItemStack stack = new ItemStack(pick, 1, 10);
        stack.set(modComponent, "a");
        ItemStack copy = stack.copy();
        assertThat(ItemStack.areItemStacksEqual(stack, copy)).isTrue();

        copy.set(modComponent, "b");
        copy.setDamageValue(99);
        assertThat(stack.get(modComponent)).isEqualTo("a");
        assertThat(stack.getItemDamage()).isEqualTo(10);
        assertThat(ItemStack.areItemStacksEqual(stack, copy)).isFalse();
    }

    @Test
    public void patchConstructorAppliesComponents() {
        DataComponentMap patch = DataComponentMap.builder().set(modComponent, "zap").build();
        ItemStack stack = new ItemStack(pick, 1, patch);
        assertThat(stack.get(modComponent)).isEqualTo("zap");
        assertThat(stack.getItemDamage()).isEqualTo(0);
    }

}
