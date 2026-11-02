/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.modlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public final class RenderUtils {

    private static final float[] DEFAULT_COVER_FADE_ALPHA_LEVELS = { 1.0F, 0.0F };
    private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5D;
    private static final double MIN_SCROLL_PERIOD = 3.0D;

    private RenderUtils() { }

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

    /**
     * Draws a cover-cropped texture with alpha levels distributed evenly from top to bottom.
     */
    public static void drawCoverFade(
        Minecraft mc, ImageInfo image, int x, int y, int width, int height, float depth, float... alphaLevels) {
        if (width <= 0 || height <= 0 || image.width() <= 0 || image.height() <= 0) {
            return;
        }
        float[] levels = alphaLevels != null && alphaLevels.length >= 2 ? alphaLevels : DEFAULT_COVER_FADE_ALPHA_LEVELS;

        mc.getTextureManager().bindTexture(image.resource());
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

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
        int segments = levels.length - 1;
        for (int i = 0; i < segments; i++) {
            double topProgress = i / (double) segments;
            double bottomProgress = (i + 1) / (double) segments;
            double topY = y + height * topProgress;
            double bottomY = y + height * bottomProgress;
            double topV = v0 + (v1 - v0) * topProgress;
            double bottomV = v0 + (v1 - v0) * bottomProgress;
            float topAlpha = MathHelper.clamp(levels[i], 0.0F, 1.0F);
            float bottomAlpha = MathHelper.clamp(levels[i + 1], 0.0F, 1.0F);

            buffer.pos(x, bottomY, depth).tex(u0, bottomV).color(1.0F, 1.0F, 1.0F, bottomAlpha).endVertex();
            buffer.pos(x + width, bottomY, depth).tex(u1, bottomV).color(1.0F, 1.0F, 1.0F, bottomAlpha).endVertex();
            buffer.pos(x + width, topY, depth).tex(u1, topV).color(1.0F, 1.0F, 1.0F, topAlpha).endVertex();
            buffer.pos(x, topY, depth).tex(u0, topV).color(1.0F, 1.0F, 1.0F, topAlpha).endVertex();
        }
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
        FontRenderer font, String text, int minX, int minY, int maxX, int maxY, int color, boolean shadow) {
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
        FontRenderer font, String text, int centerX, int minX, int minY, int maxX, int maxY, int color,
        boolean shadow) {
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

}
