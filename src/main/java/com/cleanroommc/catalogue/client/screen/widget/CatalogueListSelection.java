package com.cleanroommc.catalogue.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;

public class CatalogueListSelection<E extends GuiListExtended.IGuiListEntry> extends CatalogueListExtended<E> {
    private @Nullable E selected;

    public CatalogueListSelection(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
        this.setSelected(null);
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return this.getSelected() == this.getListEntry(slotIndex);
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
            this.renderSelection(rowLeft, rowTop, rowRight, rowBottom);
        }
        super.renderItem(slotIndex, rowLeft, rowTop, rowRight, rowBottom, mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("SameParameterValue")
    protected void renderSelection(int left, int top, int right, int bottom) {
        top -= 2;
        bottom += 2;
        Gui.drawRect(left, top, right, bottom, 0xFF808080);
        Gui.drawRect(left + 1, top + 1, right - 1, bottom - 1, 0xFF000000);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
