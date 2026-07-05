package com.cleanroommc.catalogue.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/// @author MrCrayfish
public final class RenderUtils {
    private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5D;
    private static final double MIN_SCROLL_PERIOD = 3.0D;

    private RenderUtils() {
    }

    /**
     * Creates a scissor test using minecraft screen coordinates instead of pixel coordinates.
     */
    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        final Minecraft mc = Minecraft.getMinecraft();
        int scale = new ScaledResolution(mc).getScaleFactor();

        int x = screenX * scale;
        int y = mc.displayHeight - (screenY * scale + boxHeight * scale);
        int width = Math.max(0, boxWidth * scale);
        int height = Math.max(0, boxHeight * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }

    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static void drawCoverFade(Minecraft minecraft, ImageInfo image, int x, int y, int width, int height, float zLevel) {
        if (width <= 0 || height <= 0 || image.width() <= 0 || image.height() <= 0) return;

        minecraft.getTextureManager().bindTexture(image.resource());
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        double sourceAspect = image.width() / (double) image.height();
        double targetAspect = width / (double) height;
        double u0 = 0.0D;
        double u1 = 1.0D;
        double v0 = 0.0D;
        double v1 = 1.0D;

        if (sourceAspect > targetAspect) {
            double visibleWidth = image.height() * targetAspect;
            double crop = (image.width() - visibleWidth) / 2.0D / image.width();
            u0 = crop;
            u1 = 1.0D - crop;
        } else if (sourceAspect < targetAspect) {
            double visibleHeight = image.width() / targetAspect;
            double crop = (image.height() - visibleHeight) / 2.0D / image.height();
            v0 = crop;
            v1 = 1.0D - crop;
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x, y + height, zLevel).tex(u0, v1).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex(u1, v1).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(x + width, y, zLevel).tex(u1, v0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.pos(x, y, zLevel).tex(u0, v0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws a string centered in a box, scrolling horizontally when it is too wide.
     *
     * @param minX left edge of the box.
     * @param minY top edge of the box.
     * @param maxX right edge of the box.
     * @param maxY bottom edge of the box.
     */
    public static void drawScrollingString(
            FontRenderer font, String text,
            int minX, int minY, int maxX, int maxY,
            int color, boolean shadow
    ) {
        drawScrollingString(font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color, shadow);
    }

    /**
     * Draws a string around a center point in a box, scrolling horizontally when it is too wide.
     *
     * @param centerX horizontal center used when the string fits in the box.
     * @param minX    left edge of the box.
     * @param minY    top edge of the box.
     * @param maxX    right edge of the box.
     * @param maxY    bottom edge of the box.
     */
    public static void drawScrollingString(
            FontRenderer font, String text,
            int centerX, int minX, int minY, int maxX, int maxY,
            int color, boolean shadow
    ) {
        int textWidth = font.getStringWidth(text);
        int textY = (minY + maxY - 9) / 2 + 1;
        int boxWidth = maxX - minX;
        if (textWidth > boxWidth) {
            int overflow = textWidth - boxWidth;
            double time = Minecraft.getSystemTime() / 1000.0D;
            double period = Math.max(overflow * PERIOD_PER_SCROLLED_PIXEL, MIN_SCROLL_PERIOD);
            double progress = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * time / period)) / 2D + 0.5D;
            double offset = overflow * progress;
            scissor(minX, minY, maxX - minX, maxY - minY);
            font.drawString(text, minX - (int) offset, textY, color, shadow);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            int clampedX = MathHelper.clamp(centerX, minX + textWidth / 2, maxX - textWidth / 2);
            int centeredX = clampedX - font.getStringWidth(text) / 2;
            font.drawString(text, centeredX, textY, color, shadow);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blitNineSlicedSprite(NineSlice nineSlice, int x, int y, int width, int height) {
        blitNineSlicedSprite(nineSlice, x, y, 0, width, height);
    }

    public static void blitNineSlicedSprite(NineSlice nineSlice, int x, int y, int z, int width, int height) {
        NineSlice.Border border = nineSlice.border();
        int leftBorder = Math.min(border.left(), width / 2);
        int rightBorder = Math.min(border.right(), width / 2);
        int topBorder = Math.min(border.top(), height / 2);
        int bottomBorder = Math.min(border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, width, height);
        } else if (height == nineSlice.height()) {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, leftBorder, height);
            blitTiledSprite(x + leftBorder, y, z, width - rightBorder - leftBorder, height, leftBorder, 0, nineSlice.width() - rightBorder - leftBorder, nineSlice.height(), nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), nineSlice.width() - rightBorder, 0, x + width - rightBorder, y, z, rightBorder, height);
        } else if (width == nineSlice.width()) {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, width, topBorder);
            blitTiledSprite(x, y + topBorder, z, width, height - bottomBorder - topBorder, 0, topBorder, nineSlice.width(), nineSlice.height() - bottomBorder - topBorder, nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - bottomBorder, x, y + height - bottomBorder, z, width, bottomBorder);
        } else {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, leftBorder, topBorder);
            blitTiledSprite(x + leftBorder, y, z, width - rightBorder - leftBorder, topBorder, leftBorder, 0, nineSlice.width() - rightBorder - leftBorder, topBorder, nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), nineSlice.width() - rightBorder, 0, x + width - rightBorder, y, z, rightBorder, topBorder);
            blitSprite(nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - bottomBorder, x, y + height - bottomBorder, z, leftBorder, bottomBorder);
            blitTiledSprite(x + leftBorder, y + height - bottomBorder, z, width - rightBorder - leftBorder, bottomBorder, leftBorder, nineSlice.height() - bottomBorder, nineSlice.width() - rightBorder - leftBorder, bottomBorder, nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), nineSlice.width() - rightBorder, nineSlice.height() - bottomBorder, x + width - rightBorder, y + height - bottomBorder, z, rightBorder, bottomBorder);
            blitTiledSprite(x, y + topBorder, z, leftBorder, height - bottomBorder - topBorder, 0, topBorder, leftBorder, nineSlice.height() - bottomBorder - topBorder, nineSlice.width(), nineSlice.height());
            blitTiledSprite(x + leftBorder, y + topBorder, z, width - rightBorder - leftBorder, height - bottomBorder - topBorder, leftBorder, topBorder, nineSlice.width() - rightBorder - leftBorder, nineSlice.height() - bottomBorder - topBorder, nineSlice.width(), nineSlice.height());
            blitTiledSprite(x + width - rightBorder, y + topBorder, z, leftBorder, height - bottomBorder - topBorder, nineSlice.width() - rightBorder, topBorder, rightBorder, nineSlice.height() - bottomBorder - topBorder, nineSlice.width(), nineSlice.height());
        }
    }

    private static void blitTiledSprite(int targetX, int targetY, int z, int width, int height, int u, int v, int spriteWidth, int spriteHeight, int textureWidth, int textureHeight) {
        if (width > 0 && height > 0) {
            if (spriteWidth <= 0 || spriteHeight <= 0) {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
            }

            for (int tileX = 0; tileX < width; tileX += spriteWidth) {
                int tileWidth = Math.min(spriteWidth, width - tileX);

                for (int tileY = 0; tileY < height; tileY += spriteHeight) {
                    int tileHeight = Math.min(spriteHeight, height - tileY);
                    blitSprite(textureWidth, textureHeight, u, v, targetX + tileX, targetY + tileY, z, tileWidth, tileHeight);
                }
            }
        }
    }

    private static void blitSprite(int textureWidth, int textureHeight, int u, int v, int targetX, int targetY, int z, int regionWidth, int regionHeight) {
        if (regionWidth != 0 && regionHeight != 0) {
            float minU = (float) u / (float) textureWidth;
            float maxU = (float) (u + regionWidth) / (float) textureWidth;
            float minV = (float) v / (float) textureHeight;
            float maxV = (float) (v + regionHeight) / (float) textureHeight;

            innerBlit(targetX, targetX + regionWidth, targetY, targetY + regionHeight, z, minU, maxU, minV, maxV);
        }
    }

    /**
     * Performs the inner blit operation for rendering a texture with the specified coordinates and texture coordinates without color tinting.
     *
     * @param left   the left x-coordinate of the blit position.
     * @param right  the right x-coordinate of the blit position.
     * @param top    the top y-coordinate of the blit position.
     * @param bottom the bottom y-coordinate of the blit position.
     * @param z      the z-level offset for rendering order.
     * @param minU   the minimum horizontal texture coordinate.
     * @param maxU   the maximum horizontal texture coordinate.
     * @param minV   the minimum vertical texture coordinate.
     * @param maxV   the maximum vertical texture coordinate.
     */
    private static void innerBlit(int left, int right, int top, int bottom, int z, float minU, float maxU, float minV, float maxV) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(left, top, z).tex(minU, minV).endVertex();
        buffer.pos(left, bottom, z).tex(minU, maxV).endVertex();
        buffer.pos(right, bottom, z).tex(maxU, maxV).endVertex();
        buffer.pos(right, top, z).tex(maxU, minV).endVertex();
        tess.draw();
    }

    // Info of the texture
    public record NineSlice(int width, int height, Border border) {
        public record Border(int left, int top, int right, int bottom) {
            public Border(int border) {
                this(border, border, border, border);
            }
        }

        public NineSlice(int width, int height, int border) {
            this(width, height, new Border(border));
        }
    }
}
