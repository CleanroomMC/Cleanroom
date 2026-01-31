package com.cleanroommc.catalogue.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CatalogueListSelection<E extends CatalogueListExtended.IListEntry> extends CatalogueListExtended<E> {
    private @Nullable E selected;

    public CatalogueListSelection(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
        this.setSelected(null);
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return Objects.equals(this.getSelected(), this.getListEntry(slotIndex));
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    public void setSelected(@Nullable E selected) {
        this.selected = selected;
    }

    @Override
    public void removeEntry(E entry) {
        if (this.children().remove(entry) && entry == this.getSelected()) {
            this.setSelected(null);
        }
    }

    @Override
    public void clearEntriesExcept(E entry) {
        super.clearEntriesExcept(entry);
        if (this.selected != entry) {
            this.setSelected(null);
        }
    }

    @Override
    protected void renderItem(int slotIndex, int rowLeft, int rowTop, int rowRight, int rowBottom, int mouseX, int mouseY, float partialTicks) {
        if (this.showSelectionBox && this.isSelected(slotIndex)) {
            renderSelection(rowLeft, rowTop, rowRight, rowBottom);
        }
        super.renderItem(slotIndex, rowLeft, rowTop, rowRight, rowBottom, mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("SameParameterValue")
    protected void renderSelection(int left, int top, int right, int bottom) {
        top -= 2;
        bottom += 2;
        Gui.drawRect(left, top, right, bottom, 0xFF808080);
        Gui.drawRect(left + 1, top + 1, right - 1, bottom - 1, -16777216);
    }
}
