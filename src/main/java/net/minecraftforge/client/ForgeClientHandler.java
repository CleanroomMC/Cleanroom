/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.client;

import com.cleanroommc.client.IMEHandler;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeClientHandler
{
    private static final Set<String> classList = Arrays.stream(ForgeModContainer.inputMethodGuiWhiteList).collect(Collectors.toSet());
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        // register model for the universal bucket, if it exists
        if (FluidRegistry.isUniversalBucketEnabled())
        {
            ModelLoader.setBucketModelDefinition(ForgeModContainer.getInstance().universalBucket);
        }
    }

    @SubscribeEvent
    public static void registerItemHandlers(ColorHandlerEvent.Item event)
    {
        if (FluidRegistry.isUniversalBucketEnabled())
        {
            event.getItemColors().registerItemColorHandler(new FluidContainerColorer(), ForgeModContainer.getInstance().universalBucket);
        }
    }

    @SubscribeEvent
    public static void didChangeGui(GuiOpenEvent event) {
        boolean canInput;
        GuiScreen gui = event.getGui();
        if (gui == null) {
            // Ignore null GuiScreens
            canInput = false;
        } else if (gui instanceof GuiChat) {
            // Skip, this should be handled by Focus
            return;
        } else {
            // Vanilla GuiScreens
            canInput = gui instanceof GuiScreenBook
                    || gui instanceof GuiEditSign
                    || guiInWhiteList(gui);

        }

        IMEHandler.setIME(canInput);
    }

    private static boolean guiInWhiteList(GuiScreen gui) {
        String current = gui.getClass().getName();
        return classList.contains(current);
    }
    
    @SubscribeEvent
    public static void appendAdvancedTooltip(ItemTooltipEvent event) {

        if (!event.getFlags().isAdvanced()) {
            return;
        }
        List<String> list = event.getToolTip();
        ItemStack stack = event.getItemStack();
        
        int limit = ForgeModContainer.maxTooltipNBTListLength;
        list.add(I18n.translateToLocal("item.forge.advanced_shift_info"));
        if (stack.hasTagCompound() && limit > 0) list.add(I18n.translateToLocal("item.forge.advanced_ctrl_info"));
        String pre = TextFormatting.DARK_GRAY + "     ";
        String head = TextFormatting.GRAY + "  -";
        
        int burnTime = net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(stack);
        if (burnTime > 0) {
            list.add(TextFormatting.DARK_GRAY + I18n.translateToLocalFormatted("item.burn_time", burnTime, burnTime / 20));
        }
        
        if (GuiScreen.isShiftKeyDown()) {
            list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.translateToLocal("item.forge.advanced_info"));

            int[] ids = OreDictionary.getOreIDs(stack);
            if (ids.length > 0) {
                list.add(head + I18n.translateToLocal("item.forge.oredict"));
                for (int id : ids) {
                    list.add(pre + OreDictionary.getOreName(id));
                }
            }

            String baseName = stack.getTranslationKey();
            if (!baseName.equals("item.null")) {
                list.add(head + I18n.translateToLocal("item.forge.translation_key"));
                list.add(pre + baseName);
            }
        }

        if (GuiScreen.isCtrlKeyDown() && limit > 0) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound != null && !compound.isEmpty()) {
                list.add(head + I18n.translateToLocal("item.forge.nbt_list"));

                String compoundStr = compound.toString();
                int compoundStrLength = compoundStr.length();
                String compoundDisplay;

                if (compoundStrLength > limit) {
                    compoundDisplay = compoundStr.substring(0, limit) + TextFormatting.GRAY + " " + I18n.translateToLocalFormatted("item.forge.nbt_limited", limit);
                } else {
                    compoundDisplay = compoundStr;
                }
                list.add(pre + compoundDisplay);
            }
        }
    }
}
