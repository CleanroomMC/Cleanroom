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
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import static org.assertj.core.api.Assertions.assertThat;

/** Item component prototypes: MAX_DAMAGE only on damageable items, MAX_STACK_SIZE always, setters rebuild. */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class ItemComponentsTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void plainItemPrototypeIsStackSizeOnly() {
        Item item = new Item().setMaxStackSize(16);
        DataComponentMap prototype = item.getComponents();
        assertThat((int) prototype.get(DataComponents.MAX_STACK_SIZE)).isEqualTo(16);
        assertThat(prototype.has(DataComponents.MAX_DAMAGE)).isFalse();
        assertThat(prototype.size()).isEqualTo(1);
        assertThat(item.getComponents()).isSameAs(prototype); // cached
    }

    @Test
    public void damageableItemPrototypeCarriesMaxDamage() {
        Item tool = new Item().setMaxDamage(250).setMaxStackSize(1);
        DataComponentMap prototype = tool.getComponents();
        assertThat((int) prototype.get(DataComponents.MAX_DAMAGE)).isEqualTo(250);
        assertThat((int) prototype.get(DataComponents.MAX_STACK_SIZE)).isEqualTo(1);
    }

    @Test
    public void settersInvalidateThePrototype() {
        Item item = new Item().setMaxStackSize(8);
        DataComponentMap first = item.getComponents();
        item.setMaxStackSize(32);
        DataComponentMap second = item.getComponents();
        assertThat(second).isNotSameAs(first);
        assertThat((int) second.get(DataComponents.MAX_STACK_SIZE)).isEqualTo(32);

        item.setMaxDamage(100);
        DataComponentMap third = item.getComponents();
        assertThat(third).isNotSameAs(second);
        assertThat((int) third.get(DataComponents.MAX_DAMAGE)).isEqualTo(100);
        assertThat((int) third.get(DataComponents.MAX_STACK_SIZE)).isEqualTo(32);
    }

    @Test
    public void buildComponentsHookRunsAfterDefaults() {
        HookedItem item = new HookedItem();
        item.setMaxDamage(64).setMaxStackSize(1);
        DataComponentMap prototype = item.getComponents();
        assertThat(item.hooked).isTrue();
        assertThat((int) prototype.get(DataComponents.LEGACY_VARIANT)).isEqualTo(42);
        assertThat((int) prototype.get(DataComponents.MAX_DAMAGE)).isEqualTo(64);
        assertThat((int) prototype.get(DataComponents.MAX_STACK_SIZE)).isEqualTo(1);
    }

    @Test
    public void getMaxDamageUnchangedWithoutStackComponents() {
        Item tool = new Item().setMaxDamage(77);
        assertThat(tool.getMaxDamage()).isEqualTo(77);
    }

    private static final class HookedItem extends Item {

        boolean hooked;

        @Override
        protected void buildComponents(DataComponentMap.Builder builder) {
            hooked = true;
            builder.set(DataComponents.LEGACY_VARIANT, 42);
        }

    }

}
