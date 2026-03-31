package com.cleanroommc.client.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EditGameRulesGui extends GuiScreen implements GuiYesNoCallback {
    private final GuiScreen parent;
    private final boolean[] ruleStates = new boolean[8];
    private final boolean[] originalStates = new boolean[8];
    private boolean changed = false;

    private final String[] titles = {
            "fml.death_messages.title",
            "fml.mob_loot.title",
            "fml.destructive_mob_actions.title",
            "fml.keep_inventory.title",
            "fml.spectators_generate_terrain.title",
            "fml.spawn_mobs.title",
            "fml.advance_time.title",
            "fml.update_weather.title"
    };

    private final String[] ruleKeys = {
            "showDeathMessages",
            "doMobLoot",
            "mobGriefing",
            "keepInventory",
            "spectatorsGenerateChunks",
            "doMobSpawning",
            "doDaylightCycle",
            "doWeatherCycle"
    };

    public EditGameRulesGui(GuiScreen parentIn) {
        this.parent = parentIn;
    }

    @Override
    public void initGui() {
        var world = Minecraft.getMinecraft().world;
        if (world != null) {
            var gameRules = world.getGameRules();
            for (int i = 0; i < ruleKeys.length; i++) {
                ruleStates[i] = gameRules.getBoolean(ruleKeys[i]);
                originalStates[i] = ruleStates[i];
            }
        } else {
            for (int i = 0; i < ruleStates.length; i++) {
                ruleStates[i] = false;
                originalStates[i] = false;
            }
        }

        this.buttonList.add(new GuiButton(0, this.width / 2 - 154, this.height / 6 + 180, 150, 20,
                I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 4, this.height / 6 + 180, 150, 20,
                I18n.format("gui.cancel")));

        int lineHeight = 170 / (titles.length - 1);
        for (int i = 0; i < titles.length; i++) {
            int y = 30 + lineHeight * i;
            int buttonId = 2 + i;
            int x = this.width / 2 + 63;
            var buttonText = ruleStates[i] ? I18n.format("gui.yes") : I18n.format("gui.no");
            var btn = new GuiButton(buttonId, x, y - 6, 45, 20, buttonText);
            this.buttonList.add(btn);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }

        switch (button.id) {
            case 0 -> {
                applyChanges();
                this.mc.displayGuiScreen(this.parent);
            }
            case 1 -> onCancel();
            default -> {
                int index = button.id - 2;
                if (index >= 0 && index < ruleStates.length) {
                    ruleStates[index] = !ruleStates[index];
                    button.displayString = ruleStates[index] ? I18n.format("gui.yes") : I18n.format("gui.no");
                    if (ruleStates[index] != originalStates[index]) {
                        setChanged(true);
                    } else {
                        boolean anyChanged = false;
                        for (int i = 0; i < ruleStates.length; i++) {
                            if (ruleStates[i] != originalStates[i]) {
                                anyChanged = true;
                                break;
                            }
                        }

                        if (!anyChanged) {
                            setChanged(false);
                        }
                    }
                }
            }
        }
    }

    private void applyChanges() {
        var world = Minecraft.getMinecraft().world;
        for (int i = 0; i < ruleKeys.length; i++) {
            if (ruleStates[i] != originalStates[i]) {
                var command = String.format("/gamerule %s %s", ruleKeys[i], ruleStates[i]);
                mc.player.sendChatMessage(command);
                if (world != null) {
                    world.getGameRules().setOrCreateGameRule(ruleKeys[i], Boolean.toString(ruleStates[i]));
                }
            }
        }
        setChanged(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            onCancel();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void onCancel() {
        if (this.isChange()) {
            this.mc.displayGuiScreen(new GuiYesNo(this,
                    I18n.format("fml.game_rule_changes.title"),
                    I18n.format("fml.abandon_game_rule_changes"),
                    0));
        } else {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("fml.edit_game_rules.title"), this.width / 2, 4, 0xFFFFFF);

        int lineHeight = 170 / (titles.length - 1);
        for (int i = 0; i < titles.length; i++) {
            int y = 30 + lineHeight * i;
            this.drawCenteredString(this.fontRenderer, I18n.format(titles[i]), this.width / 2 - 80, y, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean isChange() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (result) {
            this.mc.displayGuiScreen(this.parent);
        } else {
            this.mc.displayGuiScreen(this);
        }
    }
}
