package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.TabCompleter;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * GuiResponder for the {@link net.minecraft.client.gui.GuiTextField} in {@link net.minecraft.client.gui.GuiChat}
 * so that text changes fires a server tab-complete request, populating the {@link SuggestionList}
 */
public class SuggestionUpdater implements GuiPageButtonList.GuiResponder {

    private final SuggestionList suggestionList;
    private final TabCompleter tabCompleter;
    private final GuiTextField field;

    private String lastRequest = "";

    public SuggestionUpdater(SuggestionList suggestionList, TabCompleter tabCompleter, GuiTextField field) {
        this.suggestionList = suggestionList;
        this.tabCompleter = tabCompleter;
        this.field = field;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            mc.player.connection.sendPacket(new CPacketTabComplete("/", null, false));
        }
    }

    @Override
    public void setEntryValue(int id, boolean value) { }

    @Override
    public void setEntryValue(int id, float value) { }

    @Override
    public void setEntryValue(int id, String value) {
        this.refresh();
    }

    /**
     * Re-request completions based on the text up to where the cursor is.
     * Safe to call on cursor-move events (arrow keys, mouse click) where {@link #setEntryValue} does not fire,
     * deduplicates on the cursor-relative prefix so a no-op move is basically free
     */
    public void refresh() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return;
        }
        String text = this.field.getText();
        if (text.isEmpty()) {
            this.lastRequest = "";
            this.suggestionList.hide();
            return;
        }
        // Complete the token the cursor sits in, not the trailing token of the full text
        String prefix = text.substring(0, this.field.getCursorPosition());
        if (prefix.equals(this.lastRequest)) {
            return;
        }
        this.lastRequest = prefix;
        if (prefix.isEmpty()) {
            this.suggestionList.hide();
            return;
        }
        // Client-side commands
        ClientCommandHandler.instance.autoComplete(prefix);
        // Server-side completions
        mc.player.connection.sendPacket(new CPacketTabComplete(prefix, this.tabCompleter.getTargetBlockPos(), false));
    }

}
