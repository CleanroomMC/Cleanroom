/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre.renderer;

import com.cleanroommc.cleanroom.splash.pre.PreSplashState;

import org.jspecify.annotations.Nullable;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class SwingSplashRenderer extends JPanel {

    private final Runnable startupReady;

    private @Nullable PreSplashState state;
    private boolean startupSignaled;

    public SwingSplashRenderer(Runnable startupReady) {
        this.startupReady = startupReady;
    }

    public void update(PreSplashState state) {
        if (this.state == state) {
            return;
        }

        this.state = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (state == null) {
            return;
        }

        Graphics2D draw = (Graphics2D) graphics.create();
        try {
            draw.setColor(new Color(PreSplashLayout.BACKGROUND, true));
            draw.fillRect(0, 0, getWidth(), getHeight());
            double scale = Math.min((double) getWidth() / PreSplashLayout.WIDTH, (double) getHeight() / PreSplashLayout.HEIGHT);
            draw.translate((getWidth() - PreSplashLayout.WIDTH * scale) / 2, (getHeight() - PreSplashLayout.HEIGHT * scale) / 2);
            draw.scale(scale * PreSplashLayout.UI_SCALE, scale * PreSplashLayout.UI_SCALE);
            draw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            draw.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawText(draw, state.title(), 48, 106, 32, 0xFFF1F4FA);
            drawText(draw, state.message(), 48, 190, 16, 0xFFB9C7DB);
            drawText(draw, state.details(), 48, 286, 16, 0xFF8998AE);
        } finally {
            draw.dispose();
        }

        if (!startupSignaled) {
            startupSignaled = true;
            startupReady.run();
        }
    }

    private static void drawText(Graphics2D draw, String value, float x, float y, float size, int color) {
        draw.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(size));
        draw.setColor(new Color(color, true));
        draw.drawString(value, x, y);
    }
}
