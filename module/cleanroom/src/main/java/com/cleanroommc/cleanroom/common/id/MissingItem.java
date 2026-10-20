/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

import com.cleanroommc.common.CleanroomVersion;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The one internal stack a saved item with an unresolvable name reads as. It carries the whole original
 * compound verbatim under {@link #RETAINED} inside its tag and writes that compound back out, so a
 * load-save cycle while the mod is absent loses nothing. Not a per-name placeholder: two different
 * originals never merge because their tags differ.
 */
public final class MissingItem extends Item {

    /* The tag key under the stack tag holding the original {@code ItemStack} compound, verbatim. */
    public static final String RETAINED = "CleanroomRetained";

    public static Item INSTANCE;

    public MissingItem() {
        setCreativeTab(null);
        setTranslationKey("cleanroom.missing_item");
        setRegistryName(new ResourceLocation("cleanroom", "missing_item"));
    }

    @SubscribeEvent
    public static void registerMissingItem(RegistryEvent.Register<Item> event) {
        if (INSTANCE != null) {
            return;
        }
        ModContainer prev = Loader.instance().activeModContainer();
        ModContainer cleanroom = Loader.instance().getIndexedModList().get(CleanroomVersion.MOD_ID);
        if (cleanroom != null) {
            Loader.instance().setActiveModContainer(cleanroom); // the item's "cleanroom" domain is ours, not the event owner's
        }
        try {
            INSTANCE = new MissingItem();
            event.getRegistry().register(INSTANCE);
        } finally {
            if (cleanroom != null) {
                Loader.instance().setActiveModContainer(prev);
            }
        }
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return Math.max(1, stack.getCount());
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String base = super.getItemStackDisplayName(stack);
        NBTTagCompound retained = stack.getSubCompound(RETAINED);
        if (retained != null && retained.hasKey("id", 8)) {
            return base + " (" + retained.getString("id") + ")";
        }
        return base;
    }

}
