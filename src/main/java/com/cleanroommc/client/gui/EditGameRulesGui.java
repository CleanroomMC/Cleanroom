package com.cleanroommc.client.gui;

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
    private final boolean[] ruleStates;
    private final boolean[] originalStates;
    private boolean changed = false;

    private static final String[] TITLES = {
            "fml.death_messages.title",
            "fml.mob_loot.title",
            "fml.destructive_mob_actions.title",
            "fml.keep_inventory.title",
            "fml.spectators_generate_terrain.title",
            "fml.spawn_mobs.title",
            "fml.advance_time.title",
            "fml.update_weather.title"
    };

    private static final String[] RULE_KEYS = {
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
        this.ruleStates = new boolean[RULE_KEYS.length];
        this.originalStates = new boolean[RULE_KEYS.length];
        loadRulesFromWorld();
    }

    private void loadRulesFromWorld() {
        var mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            var gameRules = mc.world.getGameRules();
            for (int i = 0; i < RULE_KEYS.length; i++) {
                ruleStates[i] = gameRules.getBoolean(RULE_KEYS[i]);
                originalStates[i] = ruleStates[i];
            }
        } else {
            for (int i = 0; i < RULE_KEYS.length; i++) {
                ruleStates[i] = false;
                originalStates[i] = false;
            }
        }
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 154, this.height / 6 + 180, 150,
                20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 4, this.height / 6 + 180, 150,
                20, I18n.format("gui.cancel")));

        int lineHeight = 170 / (TITLES.length - 1);
        for (int i = 0; i < TITLES.length; i++) {
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
                    boolean anyChanged = false;
                    for (int i = 0; i < ruleStates.length; i++) {
                        if (ruleStates[i] != originalStates[i]) {
                            anyChanged = true;
                            break;
                        }
                    }
                    setChanged(anyChanged);
                }
            }
        }
    }

    private void applyChanges() {
        var mc = Minecraft.getMinecraft();
        for (int i = 0; i < RULE_KEYS.length; i++) {
            if (ruleStates[i] != originalStates[i]) {
                var command = String.format("/gamerule %s %s", RULE_KEYS[i], ruleStates[i]);
                mc.player.sendChatMessage(command);
                if (mc.world != null) {
                    mc.world.getGameRules().setOrCreateGameRule(RULE_KEYS[i], Boolean.toString(ruleStates[i]));
                }
            }
        }
        System.arraycopy(ruleStates, 0, originalStates, 0, ruleStates.length);
        setChanged(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (keyCode == 1) {
            onCancel();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void onCancel() {
        if (isChange()) {
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

        int lineHeight = 170 / (TITLES.length - 1);
        for (int i = 0; i < TITLES.length; i++) {
            int y = 30 + lineHeight * i;
            this.drawCenteredString(this.fontRenderer, I18n.format(TITLES[i]), this.width / 2 - 80, y, 0xFFFFFF);
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
