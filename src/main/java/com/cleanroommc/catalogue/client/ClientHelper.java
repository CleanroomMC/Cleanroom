package com.cleanroommc.catalogue.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/// @author MrCrayfish
public class ClientHelper {
    /**
     * Creates a scissor test using minecraft screen coordinates instead of pixel coordinates.
     *
     * @param screenX
     * @param screenY
     * @param boxWidth
     * @param boxHeight
     */
    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        int scale = scaledRes.getScaleFactor();

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

    public static void blitNineSlicedSprite(NineSlice nineSlice, int x, int y, int width, int height) {
        blitNineSlicedSprite(nineSlice, x, y, 0, width, height);
    }

    public static void blitNineSlicedSprite(NineSlice nineSlice, int x, int y, int blitOffset, int width, int height) {
        NineSlice.Border border = nineSlice.border();
        int i = Math.min(border.left(), width / 2);
        int j = Math.min(border.right(), width / 2);
        int k = Math.min(border.top(), height / 2);
        int l = Math.min(border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, height);
        } else if (height == nineSlice.height()) {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, i, height);
            blitTiledSprite(x + i, y, blitOffset, width - j - i, height, i, 0, nineSlice.width() - j - i, nineSlice.height(), nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, blitOffset, j, height);
        } else if (width == nineSlice.width()) {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, k);
            blitTiledSprite(x, y + k, blitOffset, width, height - l - k, 0, k, nineSlice.width(), nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, blitOffset, width, l);
        } else {
            blitSprite(nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, i, k);
            blitTiledSprite(x + i, y, blitOffset, width - j - i, k, i, 0, nineSlice.width() - j - i, k, nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, blitOffset, j, k);
            blitSprite(nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, blitOffset, i, l);
            blitTiledSprite(x + i, y + height - l, blitOffset, width - j - i, l, i, nineSlice.height() - l, nineSlice.width() - j - i, l, nineSlice.width(), nineSlice.height());
            blitSprite(nineSlice.width(), nineSlice.height(), nineSlice.width() - j, nineSlice.height() - l, x + width - j, y + height - l, blitOffset, j, l);
            blitTiledSprite(x, y + k, blitOffset, i, height - l - k, 0, k, i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            blitTiledSprite(x + i, y + k, blitOffset, width - j - i, height - l - k, i, k, nineSlice.width() - j - i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            blitTiledSprite(x + width - j, y + k, blitOffset, i, height - l - k, nineSlice.width() - j, k, j, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
        }
    }

    private static void blitTiledSprite(int x, int y, int blitOffset, int width, int height, int uPosition, int vPosition, int spriteWidth, int spriteHeight, int nineSliceWidth, int nineSliceHeight) {
        if (width > 0 && height > 0) {
            if (spriteWidth <= 0 || spriteHeight <= 0) {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
            }

            for (int i = 0; i < width; i += spriteWidth) {
                int j = Math.min(spriteWidth, width - i);

                for (int k = 0; k < height; k += spriteHeight) {
                    int l = Math.min(spriteHeight, height - k);
                    blitSprite(nineSliceWidth, nineSliceHeight, uPosition, vPosition, x + i, y + k, blitOffset, j, l);
                }
            }
        }
    }

    private static void blitSprite(int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int blitOffset, int uWidth, int vHeight) {
        if (uWidth != 0 && vHeight != 0) {
            float minU = (float) uPosition / (float) textureWidth;
            float maxU = (float) (uPosition + uWidth) / (float) textureWidth;
            float minV = (float) vPosition / (float) textureHeight;
            float maxV = (float) (vPosition + vHeight) / (float) textureHeight;

            innerBlit(x, x + uWidth, y, y + vHeight, blitOffset, minU, maxU, minV, maxV);
        }
    }

    /**
     * Performs the inner blit operation for rendering a texture with the specified coordinates and texture coordinates without color tinting.
     *
     * @param x1         the x-coordinate of the first corner of the blit position.
     * @param x2         the x-coordinate of the second corner of the blit position
     *                   .
     * @param y1         the y-coordinate of the first corner of the blit position.
     * @param y2         the y-coordinate of the second corner of the blit position
     *                   .
     * @param blitOffset the z-level offset for rendering order.
     * @param minU       the minimum horizontal texture coordinate.
     * @param maxU       the maximum horizontal texture coordinate.
     * @param minV       the minimum vertical texture coordinate.
     * @param maxV       the maximum vertical texture coordinate.
     */
    static void innerBlit(int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x1, y1, blitOffset).tex(minU, minV).endVertex();
        bufferbuilder.pos(x1, y2, blitOffset).tex(minU, maxV).endVertex();
        bufferbuilder.pos(x2, y2, blitOffset).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(x2, y1, blitOffset).tex(maxU, minV).endVertex();
        tessellator.draw();
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
