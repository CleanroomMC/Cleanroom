package net.minecraftforge.client.gui.text;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;

@FunctionalInterface
public interface IHoverEventAction {
    IHoverEventAction EMPTY = (hoverEvent, screen, textComponent, x, y) -> {};
    void handle(HoverEvent hoverEvent, GuiScreen screen, ITextComponent textComponent, int x, int y);
}
