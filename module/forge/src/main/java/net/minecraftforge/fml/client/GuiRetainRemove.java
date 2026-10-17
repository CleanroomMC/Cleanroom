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

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import net.minecraftforge.fml.common.StartupQuery;

/**
 * The missing-content choice: one button per {@link StartupQuery#getOptions()} plus Cancel, which
 * answers -1 so the caller aborts the load exactly like a declined {@link GuiConfirmation}. The body
 * text is word-wrapped rather than clipped like {@link GuiNotification}'s.
 */
public class GuiRetainRemove extends GuiNotification
{
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_GAP = 10;

    public GuiRetainRemove(StartupQuery query)
    {
        super(query);
    }

    @Override
    public void initGui()
    {
        String[] options = query.getOptions();
        int count = options.length + 1; // the options, then Cancel
        int rowWidth = count * BUTTON_WIDTH + (count - 1) * BUTTON_GAP;
        int x = this.width / 2 - rowWidth / 2;
        for (int i = 0; i < options.length; i++)
        {
            this.buttonList.add(new GuiButton(i, x + i * (BUTTON_WIDTH + BUTTON_GAP), this.height - 38, BUTTON_WIDTH, 20, options[i]));
        }
        this.buttonList.add(new GuiButton(options.length, x + options.length * (BUTTON_WIDTH + BUTTON_GAP), this.height - 38, BUTTON_WIDTH, 20, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled && button.id <= query.getOptions().length)
        {
            query.setChoiceResult(button.id == query.getOptions().length ? -1 : button.id);
            FMLClientHandler.instance().showGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        int wrapWidth = this.width - 40;
        List<String> lines = new java.util.ArrayList<>();
        for (String line : query.getText().split("\n"))
        {
            lines.addAll(this.fontRenderer.listFormattedStringToWidth(line, wrapWidth));
        }

        int spaceAvailable = this.height - 38 - 20;
        int spaceRequired = Math.min(spaceAvailable, 10 + 10 * lines.size());
        int offset = 10 + (spaceAvailable - spaceRequired) / 2; // vertically centered

        for (String line : lines)
        {
            if (offset >= spaceAvailable)
            {
                this.drawCenteredString(this.fontRenderer, "...", this.width / 2, offset, 0xFFFFFF);
                break;
            }
            if (!line.isEmpty())
            {
                this.drawCenteredString(this.fontRenderer, line, this.width / 2, offset, 0xFFFFFF);
            }
            offset += 10;
        }

        for (GuiButton button : this.buttonList)
        {
            button.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
    }
}
