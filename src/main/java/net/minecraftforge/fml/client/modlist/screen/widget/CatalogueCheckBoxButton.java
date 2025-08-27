package net.minecraftforge.fml.client.modlist.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.modlist.ClientHelper;

/**
 * Author: MrCrayfish
 */
public class CatalogueCheckBoxButton extends GuiButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/checkbox.png");
    private boolean selected;

    public CatalogueCheckBoxButton(int id, int x, int y, boolean selectedDefault) {
        super(id, x, y, 14, 14, "");
        this.selected = selectedDefault;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.enabled && this.visible && this.hovered) {
            this.selected = !this.selected;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            minecraft.getTextureManager().bindTexture(TEXTURE);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            ClientHelper.blit(this.x, this.y, this.hovered ? 14 : 0, this.selected() ? 14 : 0, 14, 14, 64, 64);
            this.mouseDragged(minecraft, mouseX, mouseY);
        }
    }

    public boolean selected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
