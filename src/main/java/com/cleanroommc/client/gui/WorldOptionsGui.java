package com.cleanroommc.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLockIconButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldOptionsGui extends GuiScreen {
    private final GuiScreen parent;
    private GuiButton difficultyButton;
    private GuiLockIconButton lockButton;

    public WorldOptionsGui(GuiScreen parentIn) {
        this.parent = parentIn;
    }

    @Override
    public void initGui() {
        this.difficultyButton = new GuiButton(0, this.width / 2 - 154, this.height / 3 - 22,
                130, 20, this.getDifficultyText(this.mc.world.getDifficulty()));
        this.lockButton = new GuiLockIconButton(1, this.width / 2 - 24, this.height / 3 - 22);

        this.lockButton.setLocked(this.mc.world.getWorldInfo().isDifficultyLocked());
        this.lockButton.enabled = !this.lockButton.isLocked();
        this.difficultyButton.enabled = !this.lockButton.isLocked();

        this.buttonList.add(this.difficultyButton);
        this.buttonList.add(this.lockButton);
        var editGameRulesButton = new GuiButton(2, this.width / 2 + 4, this.height / 3 - 22, 150,
                20, I18n.format("fml.edit_game_rules"));
        editGameRulesButton.enabled = this.mc.player.canUseCommand(2, "gamerule");
        this.buttonList.add(editGameRulesButton);
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height - 26, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0 -> {
                    this.mc.world.getWorldInfo().setDifficulty(EnumDifficulty.byId(this.mc.world.getDifficulty().getId() + 1));
                    this.difficultyButton.displayString = this.getDifficultyText(this.mc.world.getDifficulty());
                }
                case 1 -> this.mc.displayGuiScreen(new GuiYesNo(this,
                        I18n.format("difficulty.lock.title"),
                        I18n.format("difficulty.lock.question",
                        I18n.format(this.mc.world.getWorldInfo().getDifficulty().getTranslationKey())),
                        1
                ));
                case 2 -> {
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new EditGameRulesGui(this));
                }
                case 3 -> {
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(this.parent);
                }
            }
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (id == 1 && result) {
            this.mc.world.getWorldInfo().setDifficultyLocked(true);
            this.lockButton.setLocked(true);
            this.lockButton.enabled = false;
            this.difficultyButton.enabled = false;
        }
        this.mc.displayGuiScreen(this);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.mc.gameSettings.saveOptions();
            this.mc.displayGuiScreen(this.parent);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("fml.world_options.title"), this.width / 2, 12, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public String getDifficultyText(EnumDifficulty difficulty) {
        return I18n.format("options.difficulty") + ": " + I18n.format(difficulty.getTranslationKey());
    }
}
