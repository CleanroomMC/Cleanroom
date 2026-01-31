package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.Utils;
import com.cleanroommc.catalogue.client.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class CatalogueTextButton extends GuiButton {
    protected static final WidgetSprites SPRITES = new WidgetSprites(Utils.withDefaultNamespace("widget/button"), Utils.withDefaultNamespace("widget/button_disabled"), Utils.withDefaultNamespace("widget/button_highlighted"));

    public CatalogueTextButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        mc.getTextureManager().bindTexture(SPRITES.get(this.enabled, this.hovered));
        ClientHelper.blitNineSlicedSprite(new ClientHelper.NineSlice(200, 20, 3), this.x, this.y, this.width, this.height);

        this.mouseDragged(mc, mouseX, mouseY);
        this.renderString(mc.fontRenderer, this.getFGColor());
    }

    public void renderString(FontRenderer font, int color) {
        this.renderScrollingString(font, 2, color);
    }

    protected void renderScrollingString(FontRenderer font, int width, int color) {
        int i = this.x + width;
        int j = this.x + this.width - width;
        renderScrollingString(font, this.displayString, i, this.y, j, this.y + this.height, color);
    }

    public void renderScrollingString(FontRenderer font, String text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    public void renderScrollingString(FontRenderer font, String text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int i = font.getStringWidth(text);
        int j = (minY + maxY - 9) / 2 + 1;
        int k = maxX - minX;
        if (i > k) {
            int l = i - k;
            double d0 = (double) System.currentTimeMillis() / (double) 1000.0F;
            double d1 = Math.max((double) l * (double) 0.5F, (double) 3.0F);
            double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / (double) 2.0F + (double) 0.5F;
            double d3 = Utils.lerp(d2, 0.0F, l);
            ClientHelper.scissor(minX, minY, maxX - minX, maxY - minY);
            drawString(font, text, minX - (int) d3, j, color);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            int i1 = MathHelper.clamp(centerX, minX + i / 2, maxX - i / 2);
            drawCenteredString(font, text, i1, j, color);
        }
    }

    protected int getFGColor() {
        if (this.packedFGColour != 0) {
            return this.packedFGColour;
        } else if (!this.enabled) {
            return 10526880;
        } else if (this.hovered) {
            return 16777120;
        } else {
            return 14737632;
        }
    }
}
