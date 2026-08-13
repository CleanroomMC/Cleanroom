package com.cleanroommc.client.modlist.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.GuiScrollingList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModListExtended<E extends ModListExtended.IListEntry> extends GuiScrollingList {
    private final List<E> entries = new ArrayList<>();

    public ModListExtended(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawSlot(int slotIndex, int entryRight, int slotTop, int slotBuffer, Tessellator tess, float partialTicks) {
        E entry = this.getListEntry(slotIndex);
        entry.drawEntry(slotIndex, this.getListContentLeft(), slotTop, this.getListWidth(), slotBuffer,
                this.mouseX, this.mouseY, this.getSlotIndexFromScreenCoords(this.mouseX, this.mouseY) == slotIndex, partialTicks);
    }

    @Override
    protected int getScrollThumbHeight() {
        int viewHeight = this.bottom - this.top;
        int contentHeight = this.getContentHeight();
        if (viewHeight <= 0 || contentHeight <= 0) return 0;

        int maxThumbHeight = Math.max(1, viewHeight - 8);
        int minThumbHeight = Math.min(32, maxThumbHeight);
        return MathHelper.clamp(viewHeight * viewHeight / contentHeight, minThumbHeight, maxThumbHeight);
    }

    public final List<E> children() {
        return this.entries;
    }

    @Nonnull
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

    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        if (!this.isMouseOverList(mouseX, mouseY)) return false;

        int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        if (slotIndex < 0) return false;

        E entry = this.getListEntry(slotIndex);
        int relativeX = mouseX - this.getListContentLeft();
        int relativeY = mouseY - (this.top + 4 - (int) this.scrollDistance + slotIndex * this.slotHeight);
        return entry.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY);
    }

    public boolean mouseReleased(int mouseX, int mouseY, int mouseEvent) {
        for (int slotIndex = 0; slotIndex < this.getSize(); slotIndex++) {
            E entry = this.getListEntry(slotIndex);
            int relativeX = mouseX - this.getListContentLeft();
            int relativeY = mouseY - (this.top + 4 - (int) this.scrollDistance + slotIndex * this.slotHeight);
            entry.mouseReleased(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY);
        }
        return false;
    }

    @Override
    protected boolean shouldCenterShortContent()
    {
        return false;
    }

    @SuppressWarnings("unused")
    public interface IListEntry {
        void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTicks);

        default void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
            return false;
        }

        default void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
        }
    }
}
