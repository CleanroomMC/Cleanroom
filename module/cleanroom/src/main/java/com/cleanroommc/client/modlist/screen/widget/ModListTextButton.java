package com.cleanroommc.client.modlist.screen.widget;

import com.cleanroommc.client.modlist.ModListConstants;
import com.cleanroommc.client.modlist.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ModListTextButton extends GuiButton {
    private static final ResourceLocation BUTTON = ModListConstants.resource("textures/gui/sprites/widget/button.png");
    private static final ResourceLocation BUTTON_DISABLED = ModListConstants.resource("textures/gui/sprites/widget/button_disabled.png");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = ModListConstants.resource("textures/gui/sprites/widget/button_highlighted.png");
    private static final RenderUtils.NineSlice BUTTON_SLICE = new RenderUtils.NineSlice(200, 20, 3);
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
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        mc.getTextureManager().bindTexture(this.enabled ? (this.hovered ? BUTTON_HIGHLIGHTED : BUTTON) : BUTTON_DISABLED);
        RenderUtils.blitNineSlicedSprite(BUTTON_SLICE, this.x, this.y, this.width, this.height);
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
