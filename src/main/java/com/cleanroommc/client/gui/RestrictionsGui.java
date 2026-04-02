package com.cleanroommc.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScreenChatOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RestrictionsGui extends GuiScreen {
    private final GuiScreen parent;

    public RestrictionsGui(GuiScreen parentIn) {
        this.parent = parentIn;
    }

    @Override
    public void initGui() {
        if (this.mc.gameSettings.chatVisibility != EnumChatVisibility.FULL) {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 80, I18n.format("fml.chat_settings_screen")));
        }
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 26, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0 -> this.mc.displayGuiScreen(new ScreenChatOptions(this.parent, this.mc.gameSettings));
                case 1 -> this.mc.displayGuiScreen(this.parent);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(this.parent);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("fml.restrictions.title"),
                this.width / 2, 12, 0xFFFFFF);
        this.drawChatStatus(this.mc.gameSettings.chatVisibility);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawChatStatus(EnumChatVisibility enumChatVisibility) {
        switch (enumChatVisibility) {
            case FULL -> {
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.send_chat_messages"),
                        this.width / 2, 67, 0x00FF00);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.send_commands"),
                        this.width / 2, 76, 0x00FF00);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.receive_system_messages"),
                        this.width / 2, 85, 0x00FF00);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.receive_player_messages"),
                        this.width / 2, 94, 0x00FF00);
            }
            case SYSTEM -> {
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.chat_restricted"),
                        this.width / 2, 67, 0xFF0000);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.cannot_send_chat_messages"),
                        this.width / 2, 114, 0xFF0000);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.send_commands"),
                        this.width / 2, 123, 0x00FF00);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.receive_system_messages"),
                        this.width / 2, 132, 0x00FF00);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.cannot_receive_player_messages"),
                        this.width / 2, 141, 0xFF0000);
            }
            case HIDDEN -> {
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.chat_restricted"),
                        this.width / 2, 67, 0xFF0000);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.cannot_send_chat_messages"),
                        this.width / 2, 114, 0xFF0000);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.cannot_send_commands"),
                        this.width / 2, 123, 0xFF0000);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.cannot_receive_system_messages"),
                        this.width / 2, 132, 0xFF0000);
                this.drawCenteredString(this.fontRenderer, I18n.format("fml.cannot_receive_player_messages"),
                        this.width / 2, 141, 0xFF0000);
            }
        }
    }
}
