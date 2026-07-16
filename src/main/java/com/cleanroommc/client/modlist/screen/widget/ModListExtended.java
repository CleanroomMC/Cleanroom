package com.cleanroommc.client.modlist.screen.widget;

import com.cleanroommc.client.modlist.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModListExtended<E extends GuiListExtended.IGuiListEntry> extends GuiListExtended {
    protected boolean scrollBarVisible;
    private boolean scrolling;

    public ModListExtended(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
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
        this.scrollBarVisible = this.shouldShowScrollBar();

        RenderUtils.scissor(this.left, this.top, this.width, this.bottom - this.top);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tess = Tessellator.getInstance();

        // Shadowed dirt background. Scroll with the entries.
        this.drawContainerBackground(tess);

        // Customized header. Empty by default
        if (this.hasListHeader) this.drawListHeader(this.left + this.getListLeft(), this.getListTop(), tess);

        this.renderListItems(mouseX, mouseY, partialTicks);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        // Scroll Bar
        if (this.scrollBarVisible) this.drawScrollBar(maxScroll, tess);

        // Customized decorations. Empty by default.
        this.renderDecorations(mouseX, mouseY);

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawScrollBar(int maxScroll, Tessellator tess) {
        int scrollThumbHeight = this.getScrollThumbHeight();
        if (scrollThumbHeight <= 0) return;

        int viewHeight = this.bottom - this.top;
        int scrollThumbTop = (int) this.amountScrolled * (viewHeight - scrollThumbHeight) / maxScroll + this.top;
        scrollThumbTop = MathHelper.clamp(scrollThumbTop, this.top, this.bottom - scrollThumbHeight);

        int scrollBarLeft = this.getScrollBarLeft();
        int scrollBarRight = this.getScrollBarRight();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        // Background
        buffer.pos(scrollBarLeft, this.bottom, 0).tex(0, 1)
                .color(0, 0, 0, 255).endVertex();
        buffer.pos(scrollBarRight, this.bottom, 0).tex(1, 1)
                .color(0, 0, 0, 255).endVertex();
        buffer.pos(scrollBarRight, this.top, 0).tex(1, 0)
                .color(0, 0, 0, 255).endVertex();
        buffer.pos(scrollBarLeft, this.top, 0).tex(0, 0)
                .color(0, 0, 0, 255).endVertex();

        // Main
        buffer.pos(scrollBarLeft, scrollThumbTop + scrollThumbHeight, 0)
                .tex(0, 1).color(128, 128, 128, 255).endVertex();
        buffer.pos(scrollBarRight, scrollThumbTop + scrollThumbHeight, 0)
                .tex(1, 1).color(128, 128, 128, 255).endVertex();
        buffer.pos(scrollBarRight, scrollThumbTop, 0)
                .tex(1, 0).color(128, 128, 128, 255).endVertex();
        buffer.pos(scrollBarLeft, scrollThumbTop, 0)
                .tex(0, 0).color(128, 128, 128, 255).endVertex();

        // Border
        buffer.pos(scrollBarLeft, scrollThumbTop + scrollThumbHeight - 1, 0)
                .tex(0, 1).color(192, 192, 192, 255).endVertex();
        buffer.pos(scrollBarRight - 1, scrollThumbTop + scrollThumbHeight - 1, 0)
                .tex(1, 1).color(192, 192, 192, 255).endVertex();
        buffer.pos(scrollBarRight - 1, scrollThumbTop, 0)
                .tex(1, 0).color(192, 192, 192, 255).endVertex();
        buffer.pos(scrollBarLeft, scrollThumbTop, 0)
                .tex(0, 0).color(192, 192, 192, 255).endVertex();

        tess.draw();
    }

    protected void renderListItems(int mouseX, int mouseY, float partialTicks) {
        for (int index = 0; index < this.getSize(); ++index) {
            int rowLeft = this.left + this.getListLeft();
            int rowRight = this.left + this.getListRight();
            int rowTop = this.getRowTop(index);
            int rowBottom = this.getRowBottom(index) - 4;

            if (rowTop > this.bottom || rowBottom < this.top) {
                this.updateItemPos(index, rowLeft, rowTop, partialTicks);
            }

            if (rowTop + this.slotHeight >= this.top && rowTop <= this.bottom) {
                this.renderItem(index, rowLeft, rowTop, rowRight, rowBottom, mouseX, mouseY, partialTicks);
            }
        }
    }

    protected void renderItem(int slotIndex, int rowLeft, int rowTop, int rowRight, int rowBottom, int mouseX, int mouseY, float partialTicks) {
        this.drawSlot(slotIndex, rowLeft, rowTop, rowBottom - rowTop, mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() {
        if (!this.visible) {
            this.initialClickY = -1;
            this.scrolling = false;
            return;
        }

        boolean hasScrollBar = this.shouldShowScrollBar();
        boolean mouseOverList = this.isMouseWithinListBounds(this.mouseX, this.mouseY);

        if (mouseOverList) {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                int listLeft = this.left + this.getListLeft();
                int listRight = this.left + this.getListRight();
                boolean beforeScrollBar = this.isMouseBeforeScrollBar(hasScrollBar, this.mouseX);

                int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                int slotIndex = relativeY / this.slotHeight;

                if (beforeScrollBar && slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                    this.elementClicked(slotIndex, false, this.mouseX, this.mouseY);
                    this.selectedElement = slotIndex;
                } else if (beforeScrollBar && this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                    this.clickedHeader(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                }
            }
        }

        if (Mouse.isButtonDown(0) && this.getEnabled()) {
            if (this.initialClickY == -1) {
                if (mouseOverList) {
                    boolean clickedOnHeader = false;

                    int listLeft = this.left + this.getListLeft();
                    int listRight = this.left + this.getListRight();
                    int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                    int slotIndex = relativeY / this.slotHeight;
                    boolean beforeScrollBar = this.isMouseBeforeScrollBar(hasScrollBar, this.mouseX);

                    if (beforeScrollBar && slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                        boolean isDoubleClick = slotIndex == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                        this.elementClicked(slotIndex, isDoubleClick, this.mouseX, this.mouseY);
                        this.selectedElement = slotIndex;
                        this.lastClicked = Minecraft.getSystemTime();
                    } else if (beforeScrollBar && this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                        this.clickedHeader(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                        clickedOnHeader = true;
                    }

                    this.scrolling = !clickedOnHeader && this.isMouseOverScrollBar(hasScrollBar, this.mouseX);
                    if (this.scrolling) {
                        this.initialClickY = this.mouseY;
                    } else {
                        this.initialClickY = -2;
                    }
                } else {
                    this.scrolling = false;
                    this.initialClickY = -2;
                }
            } else if (this.initialClickY >= 0 && this.scrolling) {
                if (this.mouseY < this.top) {
                    this.setAmountScrolled(0.0F);
                } else if (this.mouseY > this.bottom) {
                    this.setAmountScrolled(this.getMaxScroll());
                } else {
                    int maxScroll = Math.max(1, this.getMaxScroll());
                    int viewHeight = this.bottom - this.top;
                    int scrollThumbHeight = this.getScrollThumbHeight();
                    int scrollRange = viewHeight - scrollThumbHeight;

                    this.scrollMultiplier = scrollRange > 0 ? Math.max(1.0F, (float) maxScroll / (float) scrollRange) : 0.0F;
                    this.setAmountScrolled(this.amountScrolled + (float) (this.mouseY - this.initialClickY) * this.scrollMultiplier);
                }

                this.initialClickY = this.mouseY;
            } else {
                this.initialClickY = -2;
            }
        } else {
            this.initialClickY = -1;
            this.scrolling = false;
        }

        int wheelDelta = Mouse.getEventDWheel();

        if (wheelDelta != 0 && mouseOverList) {
            wheelDelta = wheelDelta > 0 ? -1 : 1;
            this.setAmountScrolled(this.amountScrolled + (float) (wheelDelta * this.slotHeight / 2));
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.visible) return false;

        int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        if (slotIndex >= 0) {
            int rowLeft = this.left + this.getListLeft();
            int rowTop = this.getRowTop(slotIndex);
            int relativeX = mouseX - rowLeft;
            int relativeY = mouseY - rowTop;
            if (this.getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseButton, relativeX, relativeY)) {
                this.setEnabled(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!this.visible) {
            this.setEnabled(true);
            return false;
        }

        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex) {
            int rowLeft = this.left + this.getListLeft();
            int rowTop = this.getRowTop(slotIndex);
            int relativeX = mouseX - rowLeft;
            int relativeY = mouseY - rowTop;
            this.getListEntry(slotIndex).mouseReleased(slotIndex, mouseX, mouseY, mouseButton, relativeX, relativeY);
        }
        this.setEnabled(true);
        return false;
    }

    @Override
    public int getSlotIndexFromScreenCoords(int mouseX, int mouseY) {
        if (!this.isMouseWithinListBounds(mouseX, mouseY)) return -1;

        boolean hasScrollBar = this.shouldShowScrollBar();
        int listLeft = this.left + this.getListLeft();
        int listRight = this.left + this.getListRight();
        int relativeY = MathHelper.floor(mouseY - this.top) - this.headerPadding + this.getAmountScrolled() - 4;
        int slotIndex = relativeY / this.slotHeight;
        boolean beforeScrollBar = this.isMouseBeforeScrollBar(hasScrollBar, mouseX);
        return beforeScrollBar && mouseX >= listLeft && mouseX <= listRight && slotIndex >= 0 && relativeY >= 0 && slotIndex < this.getSize() ? slotIndex : -1;
    }

    public boolean isMouseWithinListBounds(int mouseX, int mouseY) {
        return mouseX >= this.left && mouseX <= this.left + this.width && this.isMouseYWithinSlotBounds(mouseY);
    }

    private boolean shouldShowScrollBar() {
        return this.getMaxScroll() > 0 && this.getContentHeight() > 0 && this.bottom > this.top;
    }

    private boolean isMouseOverScrollBar(boolean hasScrollBar, int mouseX) {
        return hasScrollBar && mouseX >= this.getScrollBarLeft() && mouseX < this.getScrollBarRight();
    }

    private boolean isMouseBeforeScrollBar(boolean hasScrollBar, int mouseX) {
        return !hasScrollBar || mouseX < this.getScrollBarLeft();
    }

    private int getScrollBarLeft() {
        return this.left + this.getScrollBarX();
    }

    private int getScrollBarRight() {
        return this.getScrollBarLeft() + 6;
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

    protected int getScrollThumbHeight() {
        int viewHeight = this.bottom - this.top;
        int contentHeight = this.getContentHeight();
        if (viewHeight <= 0 || contentHeight <= 0) return 0;

        int thumbHeight = viewHeight * viewHeight / contentHeight;
        int maxThumbHeight = Math.max(1, viewHeight - 8);
        int minThumbHeight = Math.min(32, maxThumbHeight);
        return MathHelper.clamp(thumbHeight, minThumbHeight, maxThumbHeight);
    }

    /**
     * Returns the scrollbar x-coordinate relative to this list's left bound.
     */
    @Override
    protected int getScrollBarX() {
        return super.getScrollBarX();
    }

    /**
     * Returns the row content width in screen pixels. This is a size, not an x-coordinate.
     */
    @Override
    public int getListWidth() {
        return super.getListWidth();
    }

    /**
     * Returns the row content left edge relative to this list's left bound.
     */
    protected int getListLeft() {
        return this.width / 2 - this.getListWidth() / 2 + 2;
    }

    /**
     * Returns the row content right edge relative to this list's left bound.
     */
    protected int getListRight() {
        return this.getListLeft() + this.getListWidth();
    }

    /**
     * Returns the absolute screen y-coordinate where list content starts after scroll offset.
     */
    protected int getListTop() {
        return this.top + 4 - (int) this.amountScrolled;
    }

    /**
     * Returns the absolute screen y-coordinate of a row's top edge.
     */
    protected int getRowTop(int slotIndex) {
        return this.top + 4 - (int) this.amountScrolled + slotIndex * this.slotHeight + this.headerPadding;
    }

    /**
     * Returns the absolute screen y-coordinate of a row's bottom edge.
     */
    protected int getRowBottom(int slotIndex) {
        return this.getRowTop(slotIndex) + this.slotHeight;
    }

    /*
    Some helpers.
     */

    private final List<E> children = new ArrayList<>();

    public final List<E> children() {
        return this.children;
    }

    @Nonnull
    @Override
    public E getListEntry(int slotIndex) {
        return this.children().get(slotIndex);
    }

    @Override
    protected int getSize() {
        return this.children().size();
    }

    public void centerScrollOn(E entry) {
        this.setAmountScrolled((float) (this.children().indexOf(entry) * this.slotHeight + this.slotHeight / 2 - (this.bottom - this.top) / 2));
    }

    public void addEntry(E entry) {
        this.children().add(entry);
    }

    public void clearEntries() {
        this.children().clear();
    }

    public void replaceEntries(Collection<E> entries) {
        this.clearEntries();
        this.children().addAll(entries);
    }

    public void removeEntries(List<E> entries) {
        entries.forEach(this::removeEntry);
    }

    public void removeEntry(E entry) {
        this.children.remove(entry);
    }

    public void clearEntriesExcept(E entry) {
        this.children.removeIf(candidate -> candidate != entry);
    }

    @Deprecated
    @Override
    protected final void drawSelectionBox(int contentLeft, int contentTop, int mouseX, int mouseY, float partialTicks) {
    }

    public interface IListEntry extends IGuiListEntry {
        /**
         * Called when the entry's position is moved.
         */
        @Override
        default void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        /**
         * Called when the mouse is clicked within this entry.
         *
         * @param mouseX    the current mouse x position
         * @param mouseY    the current mouse y position
         * @param relativeX the current x position of the mouse relative to the top-left corner of the entry
         * @param relativeY the current y position of the mouse relative to the top-left corner of the entry
         * @return {@code true} means that something within this entry was clicked and the list should not be dragged.
         */
        @Override
        default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
            return false;
        }

        /**
         * Called when the mouse button is released.
         *
         * @param mouseX    the current mouse x position
         * @param mouseY    the current mouse y position
         * @param relativeX the current x position of the mouse relative to the top-left corner of the entry
         * @param relativeY the current y position of the mouse relative to the top-left corner of the entry
         */
        @Override
        default void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
        }
    }
}
