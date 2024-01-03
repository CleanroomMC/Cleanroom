package net.minecraftforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent e) {

        ItemStack stack = e.getItemStack();
        Item item = stack.getItem();

        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) && !stack.isEmpty()) {
            var burnTime = TileEntityFurnace.getItemBurnTime(stack);
            var maxDamage = stack.getMaxDamage();
            var currentDamage = maxDamage - stack.getItemDamage();
            var metadata = stack.getMetadata();
            var nbtData = item.getNBTShareTag(stack);
            int stackSize = e.getItemStack().getMaxStackSize();

            e.getToolTip().add(TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.metadata", metadata));
            e.getToolTip().add(TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.registryName", item.getRegistryName()));
            e.getToolTip().add(TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.unlocalizedName", stack.getTranslationKey()));

            if (burnTime > 0) e.getToolTip().add(TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.burnTime", burnTime));
            if (maxDamage > 0) e.getToolTip().add(1, TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.durability", currentDamage, maxDamage));
            if (item.getNBTShareTag(stack) != null) e.getToolTip().add(TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.nbtTagData", nbtData));
            if (stackSize > 0) e.getToolTip().add(TextFormatting.DARK_GRAY + I18n.format("forge.tooltip.maxStackSize", stack.getMaxStackSize()));

            genOreDictTooltip(e);
        }
    }

    private static void genOreDictTooltip(ItemTooltipEvent e) {
        List<String> names = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(e.getItemStack())) {
            String name = OreDictionary.getOreName(id);
            if (!names.contains(name)) {
                names.add("  " + name);
            } else {
                names.add("  " + TextFormatting.DARK_GRAY + name);
            }
        }
        Collections.sort(names);
        if (!names.isEmpty()) {
            e.getToolTip().add(I18n.format("forge.tooltip.oreDict"));
            e.getToolTip().addAll(names);
        }
    }

}
