package com.cleanroommc.client.modlist.screen.widget;

import com.cleanroommc.client.modlist.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModListExtended<E extends GuiListExtended.IGuiListEntry> extends GuiListExtended {
    private final List<E> entries = new ArrayList<>();

    public ModListExtended(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        RenderUtils.scissor(this.left, this.top, this.width, this.bottom - this.top);
        try {
            super.drawScreen(mouseX, mouseY, partialTicks);
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
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
        default void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        @Override
        default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
            return false;
        }

        @Override
        default void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
        }
    }
}
