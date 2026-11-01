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

import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.BufferUtils;
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

    private static final IntBuffer SCISSOR_BOX = BufferUtils.createIntBuffer(16);

    /** When the current hover started, or -1 if not hovering. */
    private long hoverStart = -1;

    public GuiButtonExt(int id, int xPos, int yPos, String displayString)
    {
        super(id, xPos, yPos, displayString);
    }

    public GuiButtonExt(int id, int xPos, int yPos, int width, int height, String displayString)
    {
        super(id, xPos, yPos, width, height, displayString);
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
            String text = this.displayString;
            int maxWidth = this.width - PADDING * 2;
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
                this.drawCenteredString(font, text, this.x + this.width / 2, textY, color);
                return;
            }

            float offset = hoverStart < 0 ? 0F : getScrollOffset(System.currentTimeMillis() - hoverStart, strWidth - maxWidth);

            if (offset <= 0F)
            {
                int ellipsisWidth = font.getStringWidth("...");
                String trimmed = font.trimStringToWidth(text, maxWidth - ellipsisWidth).trim() + "...";
                this.drawCenteredString(font, trimmed, this.x + this.width / 2, textY, color);
            }
            else
            {
                int[] previous = beginClip(mc, this.x + PADDING, this.y, maxWidth, this.height);
                font.drawStringWithShadow(text, this.x + PADDING - offset, textY, color);
                endClip(previous);
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

    /**
     * Clips drawing to the given GUI-space rectangle, intersected with any scissor box that is
     * already active (Forge's scrolling config lists use one).
     *
     * @return the previous scissor box {x, y, w, h}, or null if scissor was not enabled
     */
    private static int[] beginClip(Minecraft mc, int gx, int gy, int gw, int gh)
    {
        int f = new ScaledResolution(mc).getScaleFactor();
        int x = gx * f;
        int y = mc.displayHeight - (gy + gh) * f;
        int w = gw * f;
        int h = gh * f;
        int[] previous = null;

        if (GL11.glIsEnabled(GL11.GL_SCISSOR_TEST))
        {
            SCISSOR_BOX.clear();
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, SCISSOR_BOX);
            previous = new int[] { SCISSOR_BOX.get(0), SCISSOR_BOX.get(1), SCISSOR_BOX.get(2), SCISSOR_BOX.get(3) };

            int left = Math.max(x, previous[0]);
            int bottom = Math.max(y, previous[1]);
            int right = Math.min(x + w, previous[0] + previous[2]);
            int top = Math.min(y + h, previous[1] + previous[3]);
            x = left;
            y = bottom;
            w = Math.max(0, right - left);
            h = Math.max(0, top - bottom);
        }
        else
        {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        GL11.glScissor(x, y, w, h);
        return previous;
    }

    private static void endClip(int[] previous)
    {
        if (previous != null)
            GL11.glScissor(previous[0], previous[1], previous[2], previous[3]);
        else
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
