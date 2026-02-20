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

package net.minecraftforge.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NotificationModUpdateScreen extends GuiScreen
{

    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");

    private final GuiButton modButton;
    private Status showNotification = null;
    private Status showBetaNotification = null;
    private boolean hasCheckedForUpdates = false;

    public NotificationModUpdateScreen(GuiButton modButton)
    {
        this.modButton = modButton;
    }

    @Override
    public void initGui()
    {
        if (!hasCheckedForUpdates)
        {
            if (modButton != null)
            {
                if (ForgeVersion.hasOutdatedMods())
                    showNotification = Status.OUTDATED;
                if (ForgeVersion.hasOutdatedBetaMods())
                    showBetaNotification = Status.BETA_OUTDATED;
            }
            hasCheckedForUpdates = true;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (ForgeModContainer.disableVersionCheck || (showNotification == null && showBetaNotification == null))
            return;

		Status notification = Status.FAILED;

        if (showNotification == null || showBetaNotification == null)
			notification = showNotification == null ? showBetaNotification : showNotification;
		else
			notification = ((System.currentTimeMillis() / 3200 & 1) == 1) ? showBetaNotification : showNotification;

        Minecraft.getMinecraft().getTextureManager().bindTexture(VERSION_CHECK_ICONS);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();

        int x = modButton.x;
        int y = modButton.y;
        int w = modButton.width;
        int h = modButton.height;

        drawModalRectWithCustomSizedTexture(x + w - (h / 2 + 4), y + (h / 2 - 4), notification.getSheetOffset() * 8, (notification.isAnimated() && ((System.currentTimeMillis() / 800 & 1) == 1)) ? 8 : 0, 8, 8, 64, 16);
        GlStateManager.popMatrix();
    }

    public static NotificationModUpdateScreen init(GuiScreen guiScreen, GuiButton modButton)
    {
        NotificationModUpdateScreen notificationModUpdateScreen = new NotificationModUpdateScreen(modButton);
        notificationModUpdateScreen.setGuiSize(guiScreen.width, guiScreen.height);
        notificationModUpdateScreen.initGui();
        return notificationModUpdateScreen;
    }

}
