package com.cleanroommc.catalogue.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class CatalogueListExtended extends GuiListExtended {
    private boolean scrollBarVisible;

    public CatalogueListExtended(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
    }

    // Values renamed by deepseek. Comments are handwrite.
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Customized background. Empty by default.
        this.drawBackground();

        this.bindAmountScrolled();
        int maxScroll = this.getMaxScroll();
        this.scrollBarVisible = maxScroll > 0 && this.getContentHeight() != 0;

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();

        // Shadowed dirt background. Scroll with the entries.
        this.drawContainerBackground(tessellator);

        // Customized header. Empty by default
        if (this.hasListHeader) {
            this.drawListHeader(this.getListLeft(), this.getListTop(), tessellator);
        }

        this.drawSelectionBox(mouseX, mouseY, partialTicks);

        GlStateManager.disableDepth();

        // Draw overlay dirt to hide scrolled entries
        this.overlayBackground(0, this.top, 255, 255);
        this.overlayBackground(this.bottom, this.height, 255, 255);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        // Scroll Bar
        if (this.scrollBarVisible) {
            this.drawScrollBar(maxScroll);
        }

        // Customized decorations. Empty by default.
        this.renderDecorations(mouseX, mouseY);

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    protected void drawScrollBar(int maxScroll) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int scrollBarLeft = this.getScrollBarX();
        int scrollBarRight = scrollBarLeft + 6;

        int scrollThumbHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
        scrollThumbHeight = MathHelper.clamp(scrollThumbHeight, 32, this.bottom - this.top - 8);
        int scrollThumbTop = (int) this.amountScrolled * (this.bottom - this.top - scrollThumbHeight) / maxScroll + this.top;
        scrollThumbTop = Math.max(scrollThumbTop, this.top);

        // Background
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(scrollBarLeft, this.bottom, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
        buffer.pos(scrollBarRight, this.bottom, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
        buffer.pos(scrollBarRight, this.top, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
        buffer.pos(scrollBarLeft, this.top, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
        tessellator.draw();

        // Main
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(scrollBarLeft, scrollThumbTop + scrollThumbHeight, 0).tex(0, 1).color(128, 128, 128, 255).endVertex();
        buffer.pos(scrollBarRight, scrollThumbTop + scrollThumbHeight, 0).tex(1, 1).color(128, 128, 128, 255).endVertex();
        buffer.pos(scrollBarRight, scrollThumbTop, 0).tex(1, 0).color(128, 128, 128, 255).endVertex();
        buffer.pos(scrollBarLeft, scrollThumbTop, 0).tex(0, 0).color(128, 128, 128, 255).endVertex();
        tessellator.draw();

        // Border
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(scrollBarLeft, scrollThumbTop + scrollThumbHeight - 1, 0).tex(0, 1).color(192, 192, 192, 255).endVertex();
        buffer.pos(scrollBarRight - 1, scrollThumbTop + scrollThumbHeight - 1, 0).tex(1, 1).color(192, 192, 192, 255).endVertex();
        buffer.pos(scrollBarRight - 1, scrollThumbTop, 0).tex(1, 0).color(192, 192, 192, 255).endVertex();
        buffer.pos(scrollBarLeft, scrollThumbTop, 0).tex(0, 0).color(192, 192, 192, 255).endVertex();
        tessellator.draw();
    }

    protected void drawSelectionBox(int mouseX, int mouseY, float partialTicks) {
        int size = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int index = 0; index < size; ++index) {
            int rowTop = this.getRowTop(index);
            int rowBottom = this.getRowBottom(index) - 4;

            if (rowTop > this.bottom || rowBottom < this.top) {
                this.updateItemPos(index, this.getListLeft(), rowTop, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(index)) {
                int left = this.getListLeft();
                int right = this.getListRight();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(left, rowBottom + 2, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos(right, rowBottom + 2, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos(right, rowTop - 2, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos(left, rowTop - 2, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos(left + 1, rowBottom + 1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos(right - 1, rowBottom + 1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos(right - 1, rowTop - 1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos(left + 1, rowTop - 1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(index, this.getListLeft(), rowTop, rowBottom - rowTop, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void handleMouseInput() {
        if (this.isMouseYWithinSlotBounds(this.mouseY)) {

            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom) {
                int listLeft = this.getListLeft();
                int listRight = this.getListRight();

                int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                int slotIndex = relativeY / this.slotHeight;

                if (slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                    this.elementClicked(slotIndex, false, this.mouseX, this.mouseY);
                    this.selectedElement = slotIndex;
                } else if (this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                    this.clickedHeader(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                }
            }

            if (Mouse.isButtonDown(0) && this.getEnabled()) {
                if (this.initialClickY == -1) {
                    boolean clickedOnList = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
                        int listLeft = this.getListLeft();
                        int listRight = this.getListRight();
                        int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                        int slotIndex = relativeY / this.slotHeight;

                        if (slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                            boolean isDoubleClick = slotIndex == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                            this.elementClicked(slotIndex, isDoubleClick, this.mouseX, this.mouseY);
                            this.selectedElement = slotIndex;
                            this.lastClicked = Minecraft.getSystemTime();
                        } else if (this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                            this.clickedHeader(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                            clickedOnList = false;
                        }

                        int scrollBarLeft = this.getScrollBarX();
                        int scrollBarRight = scrollBarLeft + 6;

                        if (this.mouseX >= scrollBarLeft && this.mouseX <= scrollBarRight) {
                            this.scrollMultiplier = -1.0F;

                            int maxScroll = Math.max(1, this.getMaxScroll());

                            int viewHeight = this.bottom - this.top;
                            int scrollBarHeight = (int) ((float) (viewHeight * viewHeight) / (float) this.getContentHeight());

                            scrollBarHeight = MathHelper.clamp(scrollBarHeight, 32, viewHeight - 8);

                            this.scrollMultiplier /= (float) (viewHeight - scrollBarHeight) / (float) maxScroll;
                        } else {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (clickedOnList) {
                            this.initialClickY = this.mouseY;
                        } else {
                            this.initialClickY = -2;
                        }
                    } else {
                        this.initialClickY = -2;
                    }
                } else if (this.initialClickY >= 0) {
                    this.amountScrolled -= (float) (this.mouseY - this.initialClickY) * this.scrollMultiplier;
                    this.initialClickY = this.mouseY;
                }
            } else {
                this.initialClickY = -1;
            }

            int dWheel = Mouse.getEventDWheel();

            if (dWheel != 0) {
                dWheel = dWheel > 0 ? -1 : 1;
                this.amountScrolled += (float) (dWheel * this.slotHeight / 2);
            }
        }
    }

    @Deprecated
    @Override
    protected void drawSelectionBox(int contentLeft, int contentTop, int mouseXIn, int mouseYIn, float partialTicks) {
        this.drawSelectionBox(mouseXIn, mouseYIn, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        if (this.isMouseYWithinSlotBounds(mouseY)) {
            int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
            if (slotIndex >= 0) {
                int j = this.left + this.getListLeft();
                int k = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
                int relativeX = mouseX - j;
                int relativeY = mouseY - k;
                if (this.getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY)) {
                    this.setEnabled(false);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int x, int y, int mouseEvent) {
        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex) {
            int j = this.left + this.getListLeft();
            int k = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
            int relativeX = x - j;
            int relativeY = y - k;
            this.getListEntry(slotIndex).mouseReleased(slotIndex, x, y, mouseEvent, relativeX, relativeY);
        }
        this.setEnabled(true);
        return false;
    }

    @Override
    public int getSlotIndexFromScreenCoords(int mouseX, int mouseY) {
        int i = this.getListWidth() / 2;
        int j = this.left + this.width / 2;
        int k = j - i;
        int l = j + i;
        int i1 = MathHelper.floor(mouseY - this.top) - this.headerPadding + this.getAmountScrolled() - 4;
        int j1 = i1 / this.slotHeight;
        return mouseX < this.getScrollBarX() && mouseX >= k && mouseX <= l && j1 >= 0 && i1 >= 0 && j1 < this.getSize() ? j1 : -1;
    }

    public void setClampedAmountScrolled(float scroll) {
        this.amountScrolled = MathHelper.clamp(scroll, 0.0F, this.getMaxScroll());
    }

    public void setAmountScrolled(float scroll) {
        this.setClampedAmountScrolled(scroll);
    }

    public void clampAmountScrolled() {
        this.setClampedAmountScrolled(this.getAmountScrolled());
    }

    public void setWidth(int width) {
        this.width = width;
        this.right = this.left + this.width;
    }

    public void setHeight(int height) {
        this.height = height;
        this.bottom = this.top + height;
    }

    protected boolean isScrollBarVisible() {
        return this.scrollBarVisible;
    }

    protected int getListLeft() {
        return this.width / 2 - this.getListWidth() / 2 + 2;
    }

    protected int getListRight() {
        return this.getListLeft() + this.getListWidth();
    }

    protected int getListTop() {
        return this.top + 4 - (int) this.amountScrolled;
    }

    protected int getRowTop(int pIndex) {
        return this.top + 4 - (int) this.amountScrolled + pIndex * this.slotHeight + this.headerPadding;
    }

    protected int getRowBottom(int pIndex) {
        return this.getRowTop(pIndex) + this.slotHeight;
    }
}
