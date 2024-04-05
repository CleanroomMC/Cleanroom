package net.minecraftforge.client.gui.text;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

@FunctionalInterface
public interface IClickEventAction {
    IClickEventAction EMPTY = (clickEvent, screen, textComponent) -> false;
    boolean handle(ClickEvent clickEvent, GuiScreen screen, ITextComponent textComponent);
}
