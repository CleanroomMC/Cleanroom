package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;

import java.util.List;

/**
 * GuiResponder for the {@link GuiTextField} in {@link GuiChat}
 * so that text changes fires a server tab-complete request, populating the {@link SuggestionList}
 */
public class SuggestionUpdater implements GuiPageButtonList.GuiResponder {

    private final SuggestionList suggestionList;
    private final TabCompleter tabCompleter;
    private final GuiTextField field;
    private final boolean commandBlockMode;

    private String lastRequest = "";
    private int pendingReplies = 0;

    public SuggestionUpdater(SuggestionList suggestionList, TabCompleter tabCompleter, GuiTextField field, boolean commandBlockMode) {
        this.suggestionList = suggestionList;
        this.tabCompleter = tabCompleter;
        this.field = field;
        this.commandBlockMode = commandBlockMode;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendPacket(new CPacketTabComplete("/", null, false));
            this.pendingReplies++;
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
        // In chat, only auto-suggest for commands, but command blocks suggest for any text
        if (!this.commandBlockMode && !text.startsWith("/")) {
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
        // Server-side completions (command-block GUIs flag a target block, mirroring vanilla's TabCompleter)
        mc.player.connection.sendPacket(new CPacketTabComplete(prefix, this.tabCompleter.getTargetBlockPos(), this.commandBlockMode));
        this.pendingReplies++;
    }

    /**
     * Entrypoint for the async server tab-complete reply.
     * Known root commands always runs, but the dropdown is only populated
     * when the reply still matches what the user has typed.
     * Stale replies are is dropped so the suggestions list isn't outdated.
     */
    public void onServerCompletions(String... completions) {
        List<String> suggestions = this.suggestionList.buildSuggestions(completions);
        if (this.pendingReplies > 0) {
            this.pendingReplies--;
            // A newer request is still awaiting its reply
            if (this.pendingReplies > 0) {
                return;
            }
        }
        String currentPrefix = this.field.getText().substring(0, this.field.getCursorPosition());
        // An empty prefix means a hide-branch of refresh() ran
        // A late reply must not resurrect the dropdown
        if (currentPrefix.isEmpty() || !currentPrefix.equals(this.lastRequest)) {
            return;
        }
        if (!this.commandBlockMode && !this.field.getText().startsWith("/")) {
            return;
        }
        // A lone suggestion identical to the word already typed completes nothing, don't pop up over it
        if (suggestions.size() == 1 && this.currentWord().equalsIgnoreCase(TextFormatting.getTextWithoutFormattingCodes(suggestions.getFirst()))) {
            this.suggestionList.hide();
            return;
        }
        this.suggestionList.setSuggestions(suggestions);
    }

    private String currentWord() {
        String text = this.field.getText();
        int cursor = this.field.getCursorPosition();
        int wordStart = this.field.getNthWordFromPosWS(-1, cursor, false);
        int wordEnd = cursor;
        while (wordEnd < text.length() && text.charAt(wordEnd) != ' ') {
            wordEnd++;
        }
        return text.substring(wordStart, wordEnd);
    }

}
