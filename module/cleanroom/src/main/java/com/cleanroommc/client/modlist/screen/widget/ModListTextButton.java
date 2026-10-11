package com.cleanroommc.client.modlist.screen.widget;

import com.cleanroommc.client.modlist.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ModListTextButton extends GuiButton {
    private final @Nullable Consumer<ModListTextButton> onPress;

    public ModListTextButton(int x, int y, int width, int height, String text, @Nullable Consumer<ModListTextButton> onPress) {
        super(-1, x, y, width, height, text);
        this.onPress = onPress;
    }

    public void onClick() {
        if (this.onPress != null) this.onPress.accept(this);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        this.hovered = RenderUtils.isMouseWithin(this.x, this.y, this.width, this.height, mouseX, mouseY);
        this.renderBackground(mc, mouseX, mouseY, partialTicks);
        this.mouseDragged(mc, mouseX, mouseY);
        this.renderContents(mc, mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("unused")
    protected void renderBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GuiUtils.drawContinuousTexturedBox(this.x, this.y, 0, 46 + this.getHoverState(this.hovered) * 20,
            this.width, this.height, 200, 20, 2, this.zLevel);
    }

    @SuppressWarnings("unused")
    protected void renderContents(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        this.renderScrollingString(mc.fontRenderer, 2, this.getFGColor());
    }

    @SuppressWarnings("SameParameterValue")
    protected void renderScrollingString(FontRenderer font, int xBorder, int color) {
        int boxLeft = this.x + xBorder;
        int boxRight = this.x + this.width - xBorder;
        RenderUtils.drawScrollingString(font, this.displayString, boxLeft, this.y, boxRight, this.y + this.height, color, true);
    }

    protected int getFGColor() {
        if (this.packedFGColour != 0) {
            return this.packedFGColour;
        } else if (!this.enabled) {
            return 0xA0A0A0;
        } else if (this.hovered) {
            return 0xFFFFA0;
        } else {
            return 0xE0E0E0;
        }
    }
}
