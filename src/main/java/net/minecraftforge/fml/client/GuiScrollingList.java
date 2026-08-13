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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public abstract class GuiScrollingList extends Gui
{
    protected final Minecraft client;
    protected final int listWidth;
    protected final int listHeight;
    public int top;
    public int bottom;
    public int right;
    public int left;
    protected final int slotHeight;
    private int scrollUpActionId;
    private int scrollDownActionId;
    protected int mouseX;
    protected int mouseY;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    protected float scrollDistance;
    protected int selectedIndex = -1;
    private long lastClickTime = 0L;
    private boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;
    protected boolean visible = true;
    // Unused
    @Deprecated
    protected final int screenWidth;
    @Deprecated
    protected final int screenHeight;
    @Deprecated
    protected boolean captureMouse = true;

    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int entryHeight)
    {
        this(client, width, height, top, bottom, 0, entryHeight);
    }

    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight)
    {
       this(client, width, height, top, bottom, left, entryHeight, getScaledWidth(client), getScaledHeight(client));
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight)
    {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    private static int getScaledWidth(Minecraft client) {
        return new ScaledResolution(client).getScaledWidth();
    }

    private static int getScaledHeight(Minecraft client) {
        return new ScaledResolution(client).getScaledHeight();
    }

    @Deprecated // Unused, remove in 1.9.3?
    public void func_27258_a(boolean highlightSelected)
    {
        this.highlightSelected = highlightSelected;
    }

    @Deprecated protected void func_27259_a(boolean hasFooter, int footerHeight){ setHeaderInfo(hasFooter, footerHeight); }
    protected void setHeaderInfo(boolean hasHeader, int headerHeight)
    {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;
        if (!hasHeader) this.headerHeight = 0;
    }

    protected abstract int getSize();

    protected void elementClicked(int index, boolean doubleClick) {}

    protected boolean isSelected(int index) { return false; }

    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.headerHeight;
    }

    protected void drawBackground() {}

    @Deprecated
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {}

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawSlot(int slotIndex, int entryRight, int slotTop, int slotBuffer, Tessellator tess, float partialTicks)
    {
        drawSlot(slotIndex, entryRight, slotTop, slotBuffer, tess);
    }

    @Deprecated protected void func_27260_a(int entryRight, int relativeY, Tessellator tess) {}
    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess) { func_27260_a(entryRight, relativeY, tess); }

    @Deprecated protected void func_27255_a(int x, int y) {}
    protected void clickHeader(int x, int y) { func_27255_a(x, y); }

    @Deprecated protected void func_27257_b(int mouseX, int mouseY) {}
    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawScreen(int mouseX, int mouseY) { func_27257_b(mouseX, mouseY); }

    @Deprecated // Unused, Remove in 1.9.3?
    public int func_27256_c(int x, int y)
    {
        return this.getSlotIndexFromScreenCoords(x, y);
    }

    public int getSlotIndexFromScreenCoords(int mouseX, int mouseY)
    {
        int relativeY = mouseY - this.top - this.headerHeight + (int) this.scrollDistance - 4;
        int slotIndex = relativeY / this.slotHeight;
        return mouseX >= this.getListLeft() && mouseX <= this.getListRight()
                && (this.getMaxScroll() == 0 || mouseX < this.getScrollbarLeft())
                && slotIndex >= 0 && relativeY >= 0 && slotIndex < this.getSize() ? slotIndex : -1;
    }

    public boolean isMouseOverList(int mouseX, int mouseY)
    {
        return mouseX >= this.left && mouseX <= this.right && mouseY >= this.top && mouseY <= this.bottom;
    }

    // FIXME: is this correct/still needed?
    public void registerScrollButtons(List<GuiButton> buttons, int upActionID, int downActionID)
    {
        this.scrollUpActionId = upActionID;
        this.scrollDownActionId = downActionID;
    }

    private void applyScrollLimits()
    {
        int listHeight = this.getMaxScroll();

        if (this.shouldCenterShortContent() && listHeight == 0)
        {
            listHeight = (this.getContentHeight() - (this.bottom - this.top - 4)) / 2;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > listHeight)
        {
            this.scrollDistance = listHeight;
        }
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == this.scrollUpActionId)
            {
                this.scrollDistance -= (float)(this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
            else if (button.id == this.scrollDownActionId)
            {
                this.scrollDistance += (float)(this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
        }
    }

    public void handleMouseInput() throws IOException
    {
        this.handleMouseInput(this.mouseX, this.mouseY);
    }

    public void handleMouseInput(int mouseX, int mouseY) throws IOException
    {
        int mouseButton = Mouse.getEventButton();
        boolean mouseButtonState = Mouse.getEventButtonState();
        boolean isHovering = this.isMouseOverList(mouseX, mouseY);

        if (!this.visible)
        {
            if (mouseButton == 0 && !mouseButtonState)
            {
                this.initialMouseClickY = -1.0F;
            }
            return;
        }

        if (isHovering)
        {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0)
            {
                this.scrollDistance += (float)((-1 * scroll) * this.slotHeight / 2);
            }
        }

        if (mouseButton == 0 && mouseButtonState && isHovering)
        {
            int listLength = this.getSize();
            int scrollBarLeft = this.getScrollbarLeft();
            int scrollBarRight = scrollBarLeft + 6;
            int entryLeft = this.getListLeft();
            int entryRight = this.getListRight();
            int viewHeight = this.bottom - this.top;
            int border = 4;
            int mouseListY = mouseY - this.top - this.headerHeight + (int)this.scrollDistance - border;
            int slotIndex = mouseListY / this.slotHeight;

            if (mouseX >= entryLeft && mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength)
            {
                this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
                this.selectedIndex = slotIndex;
                this.lastClickTime = System.currentTimeMillis();
            }
            else if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY < 0)
            {
                this.clickHeader(mouseX - entryLeft, mouseY - this.top + (int)this.scrollDistance - border);
            }

            if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight)
            {
                this.scrollFactor = -1.0F;
                int scrollHeight = this.getContentHeight() - viewHeight - border;
                if (scrollHeight < 1) scrollHeight = 1;

                int thumbHeight = this.getScrollThumbHeight();
                if (viewHeight > thumbHeight)
                {
                    this.scrollFactor /= (float) (viewHeight - thumbHeight) / scrollHeight;
                }
            }
            else
            {
                this.scrollFactor = 1.0F;
            }

            this.initialMouseClickY = mouseY;
        }
        else if (mouseButton == -1 && this.initialMouseClickY >= 0.0F)
        {
            this.scrollDistance -= (mouseY - this.initialMouseClickY) * this.scrollFactor;
            this.initialMouseClickY = mouseY;
        }
        else if (mouseButton == 0 && !mouseButtonState)
        {
            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();

        int listLength     = this.getSize();
        int scrollBarWidth = 6;
        int scrollBarLeft  = this.getScrollbarLeft();
        int scrollBarRight = scrollBarLeft + scrollBarWidth;
        int entryLeft      = this.getListLeft();
        int entryRight     = this.getListRight();
        int viewHeight     = this.bottom - this.top;
        int border         = 4;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();

        ScaledResolution res = new ScaledResolution(client);
        double scaleW = client.displayWidth / res.getScaledWidth_double();
        double scaleH = client.displayHeight / res.getScaledHeight_double();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left * scaleW), (int)(client.displayHeight - (bottom * scaleH)),
                       (int)((right - left) * scaleW), (int)(viewHeight * scaleH));

        if (this.drawBackground(tess))
        {
            if (this.client.world != null)
            {
                this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
            }
            else // Draw dark dirt background
            {
                GlStateManager.disableLighting();
                GlStateManager.disableFog();
                this.client.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                float scale = 32.0F;
                BufferBuilder buffer = tess.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(this.left,  this.bottom, 0.0D).tex(this.left / scale,  (this.bottom + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                buffer.pos(this.right, this.bottom, 0.0D).tex(this.right / scale, (this.bottom + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                buffer.pos(this.right, this.top,    0.0D).tex(this.right / scale, (this.top + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                buffer.pos(this.left,  this.top,    0.0D).tex(this.left / scale,  (this.top + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                tess.draw();
            }
        }

        int baseY = this.top + border - (int)this.scrollDistance;

        if (this.hasHeader) {
            this.drawHeader(entryRight, baseY, tess);
        }

        for (int slotIdx = 0; slotIdx < listLength; ++slotIdx)
        {
            int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
            int slotBuffer = this.slotHeight - border;

            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top)
            {
                if (this.highlightSelected && this.isSelected(slotIdx))
                {
                    int min = this.getListLeft();
                    int max = this.getListRight();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture2D();
                    worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldr.pos(min,     slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max,     slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max,     slotTop              - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min,     slotTop              - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(max - 1, slotTop              - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(min + 1, slotTop              - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    tess.draw();
                    GlStateManager.enableTexture2D();
                }

                this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess, partialTicks);
            }
        }

        if (this.drawTopBottomShadow(tess))
        {
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            BufferBuilder buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(this.left,  this.top + 4, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0x00).endVertex();
            buffer.pos(this.right, this.top + 4, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0x00).endVertex();
            buffer.pos(this.right,    this.top, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            buffer.pos(this.left,     this.top, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(this.left,     this.bottom, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            buffer.pos(this.right,    this.bottom, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            buffer.pos(this.right, this.bottom - 4, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0x00).endVertex();
            buffer.pos(this.left,  this.bottom - 4, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0x00).endVertex();
            tess.draw();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        }
        GlStateManager.disableDepth();

        int extraHeight = (this.getContentHeight() + border) - viewHeight;
        if (extraHeight > 0)
        {
            int height = this.getScrollThumbHeight();

            int barTop = (int)this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            GlStateManager.disableTexture2D();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,  this.bottom, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.top,    0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,  this.top,    0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,  barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight,    barTop,          0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,     barTop,          0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,         barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarRight - 1,    barTop,              0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,            barTop,              0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }

        this.drawScreen(mouseX, mouseY);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected boolean drawTopBottomShadow(Tessellator tess)
    {
        return true;
    }

    protected boolean drawBackground(Tessellator tess)
    {
        return true;
    }

    protected boolean shouldCenterShortContent()
    {
        return true;
    }

    protected int getListLeft()
    {
        return this.left;
    }

    protected int getListRight()
    {
        return this.right - 7;
    }

    /**
     * Returns the x-coordinate passed to entry renderers.
     * This may be inset from {@link #getListLeft()}.
     */
    protected int getListContentLeft()
    {
        return this.getListLeft();
    }

    public int getListWidth()
    {
        return this.listWidth;
    }

    protected int getScrollbarLeft()
    {
        return this.right - 6;
    }

    public int getMaxScroll()
    {
        return Math.max(0, this.getContentHeight() - (this.bottom - this.top - 4));
    }

    public void setAmountScrolled(float amount)
    {
        this.scrollDistance = MathHelper.clamp(amount, 0, this.getMaxScroll());
    }

    public void clampAmountScrolled()
    {
        this.setAmountScrolled(this.scrollDistance);
    }

    public void setLeft(int left)
    {
        int width = this.right - this.left;
        this.left = left;
        this.right = left + width;
        this.clampAmountScrolled();
    }

    public void setWidth(int width)
    {
        this.right = this.left + width;
        this.clampAmountScrolled();
    }

    public void setHeight(int height)
    {
        this.bottom = this.top + height;
        this.clampAmountScrolled();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    protected int getScrollThumbHeight()
    {
        int viewHeight = this.bottom - this.top;
        int height = viewHeight * viewHeight / this.getContentHeight();

        if (height < 32) height = 32;
        if (height > viewHeight - 8) height = viewHeight - 8;
        return height;
    }
}
