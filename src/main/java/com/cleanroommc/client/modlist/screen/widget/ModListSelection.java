package com.cleanroommc.client.modlist.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import javax.annotation.Nullable;

public class ModListSelection<E extends GuiListExtended.IGuiListEntry> extends ModListExtended<E> {
    private @Nullable E selected;

    public ModListSelection(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
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

}
