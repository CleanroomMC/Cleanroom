package com.cleanroommc.client.modlist.screen.widget;

import com.cleanroommc.client.modlist.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link GuiListExtended} that draws and hit-tests itself instead of relying on {@link GuiSlot}
 *
 * <p>OptiFine hard-replaces {@code GuiSlot}. Anything Cleanroom patches is gone when OptiFine is installed.
 * Therefore, all the previous additions were moved here instead.
 * The vanilla members still uses ({@code drawContainerBackground}, {@code getScrollBarX}, {@code getListWidth},
 * {@code getMaxScroll}, {@code getContentHeight}) and these are all present in OptiFine's copy.
 */
public class ModListExtended<E extends GuiListExtended.IGuiListEntry> extends GuiListExtended {

    private final List<E> entries = new ArrayList<>();

    public ModListExtended(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    /** Left edge of the list content. */
    protected int getListLeft() {
        return this.left + this.width / 2 - this.getListWidth() / 2;
    }

    /** Right edge of the list content. */
    protected int getListRight() {
        return this.left + this.width / 2 + this.getListWidth() / 2;
    }

    /** Left edge entries are drawn from. */
    protected int getListEntryLeft() {
        return this.getListLeft() + 2;
    }

    protected int getScrollThumbHeight() {
        int viewHeight = this.bottom - this.top;
        int contentHeight = this.getContentHeight();
        if (viewHeight <= 0 || contentHeight <= 0) {
            return 0;
        }
        int maxThumbHeight = Math.max(1, viewHeight - 8);
        int minThumbHeight = Math.min(32, maxThumbHeight);
        return MathHelper.clamp(viewHeight * viewHeight / contentHeight, minThumbHeight, maxThumbHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();
        this.bindAmountScrolled();
        this.drawContainerBackground(Tessellator.getInstance());
        RenderUtils.scissor(this.left, this.top, this.width, this.bottom - this.top);
        try {
            this.drawEntries(mouseX, mouseY, partialTicks);
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        this.drawScrollBar();
        this.renderDecorations(mouseX, mouseY);
    }

    private void drawEntries(int mouseX, int mouseY, float partialTicks) {
        int entryLeft = this.getListEntryLeft();
        int listLeft = this.getListLeft();
        int listRight = this.getListRight();
        int entryTop = this.top + 4 - (int) this.amountScrolled;
        int entryHeight = this.slotHeight - 4;
        for (int index = 0; index < this.getSize(); index++) {
            int slotTop = entryTop + index * this.slotHeight + this.headerPadding;
            if (slotTop > this.bottom || slotTop + entryHeight < this.top) {
                this.updateItemPos(index, entryLeft, slotTop, partialTicks);
                continue;
            }
            if (this.showSelectionBox && this.isSelected(index)) {
                Gui.drawRect(listLeft, slotTop - 2, listRight, slotTop + entryHeight + 2, 0xFF808080);
                Gui.drawRect(listLeft + 1, slotTop - 1, listRight - 1, slotTop + entryHeight + 1, 0xFF000000);
                // drawRect leaves its colour bound, which would tint the entry drawn next
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
            this.drawSlot(index, entryLeft, slotTop, entryHeight, mouseX, mouseY, partialTicks);
        }
    }

    private void drawScrollBar() {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int barLeft = this.getScrollBarX();
        int barRight = barLeft + 6;
        int thumbHeight = this.getScrollThumbHeight();
        int thumbTop = Math.max(this.top, (int) this.amountScrolled * (this.bottom - this.top - thumbHeight) / maxScroll + this.top);
        Gui.drawRect(barLeft, this.top, barRight, this.bottom, 0xFF000000);
        Gui.drawRect(barLeft, thumbTop, barRight, thumbTop + thumbHeight, 0xFF808080);
        Gui.drawRect(barLeft, thumbTop, barRight - 1, thumbTop + thumbHeight - 1, 0xFFC0C0C0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public int getSlotIndexFromScreenCoords(int posX, int posY) {
        if (posX < this.getListLeft() || posX > this.getListRight() || posX >= this.getScrollBarX()) {
            return -1;
        }
        int relativeY = posY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
        int index = relativeY / this.slotHeight;
        return relativeY >= 0 && index >= 0 && index < this.getSize() ? index : -1;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        if (!this.isMouseYWithinSlotBounds(mouseY)) {
            return false;
        }
        int index = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        if (this.getListEntry(index).mousePressed(index, mouseX, mouseY, mouseEvent,
                mouseX - this.getListEntryLeft(), mouseY - this.getEntryTop(index))) {
            this.setEnabled(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int mouseEvent) {
        int entryLeft = this.getListEntryLeft();
        for (int index = 0; index < this.getSize(); index++) {
            this.getListEntry(index).mouseReleased(index, mouseX, mouseY, mouseEvent, mouseX - entryLeft,
                    mouseY - this.getEntryTop(index));
        }
        this.setEnabled(true);
        return false;
    }

    private int getEntryTop(int index) {
        return this.top + 4 - this.getAmountScrolled() + index * this.slotHeight + this.headerPadding;
    }

    public final List<E> children() {
        return this.entries;
    }

    @Nonnull
    @Override
    public E getListEntry(int index) {
        return this.entries.get(index);
    }

    @Override
    protected int getSize() {
        return this.entries.size();
    }

    public void centerScrollOn(E entry) {
        int index = this.entries.indexOf(entry);
        if (index >= 0) {
            this.setAmountScrolled(index * this.slotHeight + this.slotHeight / 2.0F - (this.bottom - this.top) / 2.0F);
        }
    }

    public void addEntry(E entry) {
        this.entries.add(entry);
    }

    public void clearEntries() {
        this.entries.clear();
    }

    public void replaceEntries(Collection<? extends E> entries) {
        this.clearEntries();
        this.entries.addAll(entries);
    }

    public void setAmountScrolled(float amount) {
        this.amountScrolled = MathHelper.clamp(amount, 0.0F, this.getMaxScroll());
    }

    public void clampAmountScrolled() {
        this.setAmountScrolled(this.amountScrolled);
    }

    public void setWidth(int width) {
        this.width = width;
        this.right = this.left + width;
        this.clampAmountScrolled();
    }

    public void setHeight(int height) {
        this.height = height;
        this.bottom = this.top + height;
        this.clampAmountScrolled();
    }

    public interface IListEntry extends IGuiListEntry {

        @Override
        default void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

        @Override
        default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
            return false;
        }

        @Override
        default void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) { }

    }
}
