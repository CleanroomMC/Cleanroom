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
    private boolean cycling;

    /**
     * @param commandBlockMode true to suggest and color commands with or without a leading "/"
     */
    public CommandSuggestions(GuiTextField field, TabCompleter tabCompleter, boolean commandBlockMode) {
        this.field = field;
        this.tabCompleter = tabCompleter;
        this.list = new SuggestionList(field, commandBlockMode);
        this.updater = new SuggestionUpdater(this.list, tabCompleter, field, commandBlockMode);
        field.setGuiResponder(this.updater);
        if (commandBlockMode) {
            // Text set before the responder was attached never fired it
            this.updater.refresh();
        } else {
            // Chat opened with "/" keeps Up and Down on history until the user edits, like vanilla
            this.updater.setPaused(true);
        }
    }

    public boolean keyTyped(int keyCode) {
        if (keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_DOWN) {
            this.updater.setPaused(false);
        }
        if (keyCode != Keyboard.KEY_TAB) {
            this.cycling = false;
        }
        switch (keyCode) {
            // Repeated presses cycle through the suggestions, keeping the dropdown open
            case Keyboard.KEY_TAB -> {
                if (this.list.isInvisible()) {
                    return false;
                }
                if (this.cycling || this.list.getSelected() == null) {
                    this.list.selectNext();
                    // Wraps past the "no selection" slot back to the first suggestion
                    if (this.list.getSelected() == null) {
                        this.list.selectNext();
                    }
                }
                this.cycling = true;
                // Detach the responder, or the edit would request new completions and replace the list being cycled
                this.field.setGuiResponder(null);
                this.list.replaceWord(this.field, this.list.getSelected());
                this.field.setGuiResponder(this.updater);
            }
            case Keyboard.KEY_ESCAPE -> {
                if (this.list.isInvisible()) {
                    return false;
                }
                this.list.hide();
            }
            case Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> {
                String selected = this.list.getSelected();
                // A Tab-cycled suggestion is already in the field, so Enter submits it
                if (selected == null || this.cycling) {
                    return false;
                }
                this.list.applySuggestion(this.field, selected);
            }
            // The list grows upward, so Up moves to the next, visually higher, suggestion
            case Keyboard.KEY_UP, Keyboard.KEY_DOWN -> {
                if (this.list.isInvisible()) {
                    // The screen recalls history, keep the dropdown from popping up over the recalled text
                    this.updater.setPaused(true);
                    return false;
                }
                if (keyCode == Keyboard.KEY_UP) {
                    this.list.selectNext();
                } else {
                    this.list.selectPrev();
                }
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
