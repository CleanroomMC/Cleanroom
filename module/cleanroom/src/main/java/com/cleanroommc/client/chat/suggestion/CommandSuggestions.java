package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.TabCompleter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Hooks the suggestion dropdown into a command input screen, such as chat or the command block editors.
 * Each input method returns true when the dropdown consumed the event, and the screen should then skip its own handling.
 */
@SideOnly(Side.CLIENT)
public class CommandSuggestions {

    private final GuiTextField field;
    private final TabCompleter tabCompleter;
    private final SuggestionList list;
    private final SuggestionUpdater updater;

    /**
     * @param commandBlockMode true to suggest and color commands with or without a leading "/"
     */
    public CommandSuggestions(GuiTextField field, TabCompleter tabCompleter, boolean commandBlockMode) {
        this.field = field;
        this.tabCompleter = tabCompleter;
        this.list = new SuggestionList(field, commandBlockMode);
        this.updater = new SuggestionUpdater(this.list, tabCompleter, field, commandBlockMode);
        field.setGuiResponder(this.updater);
        // Text set before the responder was attached, such as chat opened with "/", never fired it
        this.updater.refresh();
    }

    public boolean keyTyped(int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_TAB -> {
                if (this.list.isInvisible()) {
                    return false;
                }
                String selected = this.list.getSelected();
                this.list.applySuggestion(this.field, selected == null ? this.list.getFirst() : selected);
            }
            case Keyboard.KEY_ESCAPE -> {
                if (this.list.isInvisible()) {
                    return false;
                }
                this.list.hide();
            }
            case Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> {
                String selected = this.list.getSelected();
                if (selected == null) {
                    return false;
                }
                this.list.applySuggestion(this.field, selected);
            }
            // The list grows upward, so Up moves to the next, visually higher, suggestion
            case Keyboard.KEY_UP -> {
                if (this.list.isInvisible()) {
                    return false;
                }
                this.list.selectNext();
            }
            case Keyboard.KEY_DOWN -> {
                if (this.list.isInvisible()) {
                    return false;
                }
                this.list.selectPrev();
            }
            default -> {
                return false;
            }
        }
        // The screen skips its own handling, which resets this for any key other than Tab
        if (keyCode != Keyboard.KEY_TAB) {
            this.tabCompleter.resetDidComplete();
        }
        return true;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        String clicked = this.list.mouseClicked(mouseX, mouseY);
        if (clicked == null) {
            return false;
        }
        this.list.applySuggestion(this.field, clicked);
        return true;
    }

    public boolean handleMouseInput(GuiScreen screen) {
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * screen.width / mc.displayWidth;
        int mouseY = screen.height - Mouse.getEventY() * screen.height / mc.displayHeight - 1;
        if (!this.list.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        this.list.scroll(wheel);
        return true;
    }

    /**
     * Follows cursor moves from arrow keys and mouse clicks, which do not fire the responder.
     */
    public void refresh() {
        this.updater.refresh();
    }

    /**
     * Draws the input field with the ghost completion behind it and command coloring over it.
     */
    public void drawTextBox() {
        Minecraft mc = Minecraft.getMinecraft();
        this.list.drawGhostText(this.field, mc.fontRenderer);
        this.field.drawTextBox();
        this.list.drawCommandColor(this.field, mc.fontRenderer);
    }

    public void drawDropdown(int mouseX, int mouseY) {
        this.list.render(mouseX, mouseY);
    }

    public void setCompletions(String... completions) {
        // One reply consumes the Tab request, or every auto-suggest reply would keep mutating the field
        this.tabCompleter.resetRequested();
        this.updater.onServerCompletions(completions);
    }

}
