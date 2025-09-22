package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.CatalogueConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Author: MrCrayfish
 */
public class CatalogueIconButton extends GuiButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CatalogueConstants.MOD_ID, "textures/gui/icons.png");

    private final String label;
    private final int u, v;

    public CatalogueIconButton(int id, int x, int y, int u, int v) {
        this(id, x, y, u, v, 20, "");
    }

    public CatalogueIconButton(int id, int x, int y, int u, int v, int width, int height) {
        this(id, x, y, u, v, width, height, "");
    }

    public CatalogueIconButton(int id, int x, int y, int u, int v, int width, String label) {
        this(id, x, y, u, v, width, 20, label);
    }

    public CatalogueIconButton(int id, int x, int y, int u, int v, int width, int height, String label) {
        super(id, x, y, width, height, "");
        this.label = label;
        this.u = u;
        this.v = v;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        // Draw bg
        super.drawButton(minecraft, mouseX, mouseY, partialTicks);
        // Draw icon and text
        if (this.visible) {
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int contentWidth = 10 + fontrenderer.getStringWidth(this.label) + (!this.label.isEmpty() ? 4 : 0);
            int iconX = this.x + (this.width - contentWidth) / 2;
            int iconY = this.y + (this.height - 10) / 2;
            float brightness = this.enabled ? 1.0F : 0.5F;
            GlStateManager.color(brightness, brightness, brightness, 1.0F);
            drawModalRectWithCustomSizedTexture(iconX, iconY, this.u, this.v, 10, 10, 64, 64);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int textColor = this.getFGColor() | MathHelper.ceil(255.0F) << 24;
            drawString(fontrenderer, this.label, iconX + 14, iconY + 1, textColor);
        }

    }

    private int getFGColor() {
        if (packedFGColour != 0) {
            return packedFGColour;
        } else if (!this.enabled) {
            return 10526880;
        } else if (this.hovered) {
            return 16777120;
        } else {
            return 14737632;
        }
    }

}
