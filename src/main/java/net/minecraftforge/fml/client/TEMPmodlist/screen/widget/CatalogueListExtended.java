package net.minecraftforge.fml.client.TEMPmodlist.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public abstract class CatalogueListExtended extends GuiListExtended {
    // Noting different from the original one, but allows you to remove the shadow on the bottom and top.
    // Created to avoid mixins.
    private boolean shouldDrawTopAndBottom = true;
    private boolean shouldDrawBackground = true;

    public CatalogueListExtended(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
    }

    // Values renamed by deepseek. Comments are handwrite.
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Customized background. Empty by default.
        this.drawBackground();

        int scrollBarLeft = this.getScrollBarX();
        int scrollBarRight = scrollBarLeft + 6;

        this.bindAmountScrolled();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();

        // Shadowed dirt background. Scroll with the entries.
        if (this.shouldDrawBackground) {
            this.drawContainerBackground(tessellator);
        }

        int contentLeft = this.left + (this.shouldDrawBackground ? this.width / 2 - this.getListWidth() / 2 + 2 : 0);
        int contentTop = this.top + 4 - (int)this.amountScrolled;

        if (this.hasListHeader) {
            this.drawListHeader(contentLeft, contentTop, tessellator);
        }

        this.drawSelectionBox(contentLeft, contentTop, mouseX, mouseY, partialTicks);

        GlStateManager.disableDepth();

        // Draw overlay to hide scrolled entries
        this.overlayBackground(0, this.top, 255, 255);
        this.overlayBackground(this.bottom, this.height, 255, 255);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        // Shadow the top and bottom
        if (this.shouldDrawTopAndBottom) {
            this.drawTopAndBottom(tessellator);
        }

        // Scroll Bar
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0 && this.getContentHeight() != 0) {
            int scrollThumbHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            scrollThumbHeight = MathHelper.clamp(scrollThumbHeight, 32, this.bottom - this.top - 8);
            int scrollThumbTop = (int)this.amountScrolled * (this.bottom - this.top - scrollThumbHeight) / maxScroll + this.top;
            scrollThumbTop = Math.max(scrollThumbTop, this.top);

            // Background
            vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexBuffer.pos(scrollBarLeft, this.bottom, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
            vertexBuffer.pos(scrollBarRight, this.bottom, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
            vertexBuffer.pos(scrollBarRight, this.top, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
            vertexBuffer.pos(scrollBarLeft, this.top, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
            tessellator.draw();

            // Main
            vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexBuffer.pos(scrollBarLeft, scrollThumbTop + scrollThumbHeight, 0).tex(0, 1).color(128, 128, 128, 255).endVertex();
            vertexBuffer.pos(scrollBarRight, scrollThumbTop + scrollThumbHeight, 0).tex(1, 1).color(128, 128, 128, 255).endVertex();
            vertexBuffer.pos(scrollBarRight, scrollThumbTop, 0).tex(1, 0).color(128, 128, 128, 255).endVertex();
            vertexBuffer.pos(scrollBarLeft, scrollThumbTop, 0).tex(0, 0).color(128, 128, 128, 255).endVertex();
            tessellator.draw();

            // Border
            vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexBuffer.pos(scrollBarLeft, scrollThumbTop + scrollThumbHeight - 1, 0).tex(0, 1).color(192, 192, 192, 255).endVertex();
            vertexBuffer.pos(scrollBarRight - 1, scrollThumbTop + scrollThumbHeight - 1, 0).tex(1, 1).color(192, 192, 192, 255).endVertex();
            vertexBuffer.pos(scrollBarRight - 1, scrollThumbTop, 0).tex(1, 0).color(192, 192, 192, 255).endVertex();
            vertexBuffer.pos(scrollBarLeft, scrollThumbTop, 0).tex(0, 0).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        // Customized decorations. Empty by default.
        this.renderDecorations(mouseX, mouseY);

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    protected void drawTopAndBottom(Tessellator tessellator) {
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double)this.left, (double)(this.top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double)this.right, (double)(this.top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double)this.right, (double)(this.bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double)this.left, (double)(this.bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
    }

    public void setAmountScrolled(int amountScrolled) {
        this.amountScrolled = (float)amountScrolled;
        this.bindAmountScrolled();
        this.initialClickY = -2;
    }

    public void updateSize(int width, int height, int top, int bottom) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
    }

    /**
     * Sets whether draw the top and bottom shadow.
     */
    public void setDrawTopAndBottom(boolean shouldDraw) {
        this.shouldDrawTopAndBottom = shouldDraw;
    }

    /**
     * Sets whether draw the shadowed dirt background, which scrolls with the entries.
     */
    public void setDrawBackground(boolean shouldDraw) {
        this.shouldDrawBackground = shouldDraw;
    }
}
