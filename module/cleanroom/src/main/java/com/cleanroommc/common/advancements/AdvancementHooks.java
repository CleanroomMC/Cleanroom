/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common.advancements;

import net.minecraft.advancements.critereon.NBTPredicate;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * Hooks called from patched Minecraft advancement code.
 */
public final class AdvancementHooks {

    private static final String SELECTED_ITEM = "SelectedItem";

    private AdvancementHooks() { }

    /**
     * Called from {@link NBTPredicate#test(Entity)} to decide whether the entity has to be written in full with
     * {@link CommandBase#entityToNBT(Entity)}.
     * <p>
     * {@code entityToNBT} writes the whole entity. For a player that includes the inventory, every capability and
     * the full recipe book, which takes 10 ms or more in large packs. Criteria such as
     * {@code "source_entity": {"nbt": "{SelectedItem: {id: ...}}"}} on {@code player_killed_entity} only look at the
     * held item, and the trigger tests every such criterion on every kill, so a kill could stall the server tick for
     * hundreds of milliseconds before the death status reached clients.
     * <p>
     * {@code SelectedItem} is not written by {@code writeToNBT}, {@code entityToNBT} adds it afterwards. When it is the
     * only key a player predicate compares, {@link #selectedItemNBT(EntityPlayer)} gives the same match result.
     *
     * @param predicate The predicate's tag, {@code null} matches any tag
     * @param entity The entity being tested
     * @return {@code false} if {@link #selectedItemNBT(EntityPlayer)} can be matched instead
     */
    public static boolean needsFullEntityNBT(@Nullable NBTTagCompound predicate, Entity entity) {
        return predicate == null ||
            !(entity instanceof EntityPlayer) ||
            predicate.getSize() != 1 ||
            !predicate.hasKey(SELECTED_ITEM);
    }

    /**
     * Builds the {@code SelectedItem} tag exactly as {@link CommandBase#entityToNBT(Entity)} does, without writing the
     * rest of the player.
     *
     * @param player The player being tested
     * @return A compound holding only {@code SelectedItem}, or an empty compound if the main hand is empty
     */
    public static NBTTagCompound selectedItemNBT(EntityPlayer player) {
        NBTTagCompound tag = new NBTTagCompound();
        ItemStack stack = player.inventory.getCurrentItem();
        if (!stack.isEmpty()) {
            tag.setTag(SELECTED_ITEM, stack.writeToNBT(new NBTTagCompound()));
        }
        return tag;
    }

}
