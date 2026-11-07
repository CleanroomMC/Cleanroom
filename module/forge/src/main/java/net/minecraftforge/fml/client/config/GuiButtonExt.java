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

package net.minecraftforge.fml.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

/**
 * This class provides a button that fixes several bugs present in the vanilla GuiButton drawing code.
 * The gist of it is that it allows buttons of any size without gaps in the graphics and with the
 * borders drawn properly. It also prevents button text from extending out of the sides of the button by
 * trimming the end of the string and adding an ellipsis. While hovered, overflowing text scrolls
 * smoothly so the full label can be read.<br/><br/>
 *
 * The code that handles drawing the button is in GuiUtils.
 *
 * @author bspkrs
 */
public class GuiButtonExt extends GuiButton
{
    /** Wait before scrolling starts and at each end of the scroll, in ms. */
    private static final long PAUSE_MS = 1000;
    /** Scroll speed in GUI pixels per second. */
    private static final float SCROLL_SPEED = 30F;
    /** Horizontal padding between the button edge and the text. */
    private static final int PADDING = 3;

    /** When the current hover started, or -1 if not hovering. */
    private long hoverStart = -1;

    private String prefix = "";
    private String suffix = "";
    private boolean clicked = false;

    public GuiButtonExt(int id, int xPos, int yPos, String displayString)
    {
        super(id, xPos, yPos, displayString);
    }

    public GuiButtonExt(int id, int xPos, int yPos, int width, int height, String displayString)
    {
        super(id, xPos, yPos, width, height, displayString);
    }

    /** Sets the text pinned to the left/right of the label while clicked, e.g. setAffixes("[ ", " ]"). */
    public GuiButtonExt setAffixes(String prefix, String suffix)
    {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.hoverStart = -1;
        return this;
    }


    public GuiButtonExt setClicked(boolean clicked)
    {
        if (this.clicked != clicked)
            this.hoverStart = -1;
        this.clicked = clicked;
        return this;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColour != 0)
            {
                color = packedFGColour;
            }
            else if (!this.enabled)
            {
                color = 10526880;
            }
            else if (this.hovered)
            {
                color = 16777120;
            }

            FontRenderer font = mc.fontRenderer;
            String pre = clicked ? prefix : "";
            String suf = clicked ? suffix : "";
            String text = this.displayString;
            int preWidth = font.getStringWidth(pre);
            int sufWidth = font.getStringWidth(suf);
            int maxWidth = this.width - PADDING * 2 - preWidth - sufWidth;   // space left for the label
            int strWidth = font.getStringWidth(text);
            int textY = this.y + (this.height - 8) / 2;

            if (k == 2)
            {
                if (hoverStart < 0)
                    hoverStart = System.currentTimeMillis();
            }
            else
            {
                hoverStart = -1;
            }

            if (strWidth <= maxWidth)
            {
                this.drawCenteredString(font, pre + text + suf, this.x + this.width / 2, textY, color);
                return;
            }

            float offset = hoverStart < 0 || maxWidth <= 0 ? 0F : getScrollOffset(System.currentTimeMillis() - hoverStart, strWidth - maxWidth);

            if (offset <= 0F)
            {
                int ellipsisWidth = font.getStringWidth("...");
                String trimmed = font.trimStringToWidth(text, maxWidth - ellipsisWidth).trim() + "...";
                this.drawCenteredString(font, pre + trimmed + suf, this.x + this.width / 2, textY, color);
            }
            else
            {
                int f = new ScaledResolution(mc).getScaleFactor();

                int labelX = this.x + PADDING + preWidth;

                // Pinned prefix / suffix, drawn outside the label clip so they are never cut off.
                if (!pre.isEmpty())
                    font.drawStringWithShadow(pre, this.x + PADDING, textY, color);
                if (!suf.isEmpty())
                    font.drawStringWithShadow(suf, this.x + this.width - PADDING - sufWidth, textY, color);

                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glScissor(
                    labelX * f,
                    mc.displayHeight - (this.y * f + this.height * f),
                    Math.max(0, maxWidth * f),
                    Math.max(0, this.height * f)
                );
                font.drawStringWithShadow(text, labelX - offset, textY, color);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }
    }

    /**
     * Ping-pong scroll: wait -> forward -> wait -> backward -> repeat. Stateless, so it is
     * frame-rate independent and cannot go out of range if the text changes.
     *
     * @param elapsed  ms since the hover started
     * @param distance how many pixels the text overflows by
     * @return the offset in pixels, 0..distance
     */
    private static float getScrollOffset(long elapsed, int distance)
    {
        if (elapsed < PAUSE_MS)
            return 0F;

        long travel = (long) (distance / SCROLL_SPEED * 1000F);
        long t = (elapsed - PAUSE_MS) % (2 * (travel + PAUSE_MS));

        if (t < travel)
            return distance * (t / (float) travel);
        t -= travel;
        if (t < PAUSE_MS)
            return distance;
        t -= PAUSE_MS;
        if (t < travel)
            return distance * (1F - t / (float) travel);
        return 0F;
    }
}
