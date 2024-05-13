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

package net.minecraftforge.fml.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.toposort.ModSortingException;
import net.minecraftforge.fml.common.toposort.ModSortingException.SortingExceptionData;

/**
 * This class represents a GUI screen that is displayed when a mod sorting cycle is detected during Minecraft's loading process.
 * It extends the GuiScreen class from Minecraft's client GUI system.
 */
public class GuiSortingProblem extends GuiScreen {

    /**
     * The exception data related to the mod sorting cycle.
     */
    private SortingExceptionData<ModContainer> failedList;

    /**
     * Constructor for the GuiSortingProblem class.
     *
     * @param modSorting The ModSortingException object that contains the exception data related to the mod sorting cycle.
     */
    public GuiSortingProblem(ModSortingException modSorting)
    {
        this.failedList = modSorting.getExceptionData();
    }

    /**
     * Initializes the GUI components of this screen.
     * This method is called when the screen is first displayed.
     */
    @Override
    public void initGui()
    {
        super.initGui();
    }

    /**
     * Draws the screen and its components.
     *
     * @param mouseX The X coordinate of the mouse cursor.
     * @param mouseY The Y coordinate of the mouse cursor.
     * @param partialTicks The time elapsed since the last frame, used for smooth animation.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        int offset = Math.max(85 - (failedList.getVisitedNodes().size() + 3) * 10, 10);
        this.drawCenteredString(this.fontRenderer, "Forge Mod Loader has found a problem with your minecraft installation", this.width / 2, offset, 0xFFFFFF);
        offset+=10;
        this.drawCenteredString(this.fontRenderer, "A mod sorting cycle was detected and loading cannot continue", this.width / 2, offset, 0xFFFFFF);
        offset+=10;
        this.drawCenteredString(this.fontRenderer, String.format("The first mod in the cycle is %s", failedList.getFirstBadNode()), this.width / 2, offset, 0xFFFFFF);
        offset+=10;
        this.drawCenteredString(this.fontRenderer, "The remainder of the cycle involves these mods", this.width / 2, offset, 0xFFFFFF);
        offset+=5;
        for (ModContainer mc : failedList.getVisitedNodes())
        {
            offset+=10;
            this.drawCenteredString(this.fontRenderer, String.format("%s : before: %s, after: %s", mc.toString(), mc.getDependants(), mc.getDependencies()), this.width / 2, offset, 0xEEEEEE);
        }
        offset+=20;
        this.drawCenteredString(this.fontRenderer, "The file 'ForgeModLoader-client-0.log' contains more information", this.width / 2, offset, 0xFFFFFF);
    }

}