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

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.critereon.NBTPredicate;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Hooks called from patched Minecraft advancement code.
 */
public final class AdvancementHooks {

    private static final String SELECTED_ITEM = "SelectedItem";

    private AdvancementHooks() { }

    /**
     * Replaces the body of {@link NBTPredicate#test(Entity)}.
     * <p>
     * {@link CommandBase#entityToNBT(Entity)} writes the whole entity. For a player that includes the inventory, every
     * capability and the full recipe book, which takes 10 ms or more in large packs. Criteria such as
     * {@code "source_entity": {"nbt": "{SelectedItem: {id: ...}}"}} on {@code player_killed_entity} only look at the
     * held item, and the trigger tests every such criterion on every kill, so a kill could stall the server tick for
     * hundreds of milliseconds before the death status reached clients.
     * <p>
     * {@code SelectedItem} is not written by {@code writeToNBT}, {@code entityToNBT} adds it afterwards. When it is the
     * only key a player predicate compares, a compound holding just {@code SelectedItem}, built the same way, gives the
     * same match result.
     *
     * @param predicate The predicate being tested
     * @param tag The predicate's tag, {@code null} matches any tag
     * @param entity The entity being tested
     * @return {@code true} if the entity matches the predicate
     */
    public static boolean testEntity(NBTPredicate predicate, @Nullable NBTTagCompound tag, Entity entity) {
        if (predicate == NBTPredicate.ANY) {
            return true;
        }
        if (tag != null && tag.getSize() == 1 && tag.hasKey(SELECTED_ITEM) && entity instanceof EntityPlayer) {
            NBTTagCompound selected = new NBTTagCompound();
            ItemStack stack = ((EntityPlayer) entity).inventory.getCurrentItem();
            if (!stack.isEmpty()) {
                selected.setTag(SELECTED_ITEM, stack.writeToNBT(new NBTTagCompound()));
            }
            return predicate.test(selected);
        }
        return predicate.test(CommandBase.entityToNBT(entity));
    }

}
