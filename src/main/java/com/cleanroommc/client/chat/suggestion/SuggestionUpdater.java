package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.TabCompleter;
import net.minecraftforge.client.ClientCommandHandler;

import java.util.List;

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
        // Only auto-suggest for commands
        if (!text.startsWith("/")) {
            this.lastRequest = prefix;
            this.suggestionList.hide();
            return;
        }
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

    /**
     * Entrypoint for the async server tab-complete reply.
     * Known root commands always runs, but the dropdown is only populated
     * when the reply still matches what the user has typed.
     * Stale replies are is dropped so the suggestions list isn't outdated.
     */
    public void onServerCompletions(String... completions) {
        List<String> suggestions = this.suggestionList.buildSuggestions(completions);
        String currentPrefix = this.field.getText().substring(0, this.field.getCursorPosition());
        if (!currentPrefix.equals(this.lastRequest)) {
            return;
        }
        this.suggestionList.showSuggestions(suggestions);
    }

}
