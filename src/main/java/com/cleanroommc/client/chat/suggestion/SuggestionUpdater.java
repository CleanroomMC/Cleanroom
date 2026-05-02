package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList;
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

    private String lastText = "";

    public SuggestionUpdater(SuggestionList suggestionList, TabCompleter tabCompleter) {
        this.suggestionList = suggestionList;
        this.tabCompleter = tabCompleter;
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
        if (value.equals(this.lastText)) {
            return;
        }
        this.lastText = value;
        if (value.isEmpty()) {
            this.suggestionList.hide();
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return;
        }
        // Client-side commands
        ClientCommandHandler.instance.autoComplete(value);
        // Server-side completions
        mc.player.connection.sendPacket(new CPacketTabComplete(value, this.tabCompleter.getTargetBlockPos(), false));
    }

}
