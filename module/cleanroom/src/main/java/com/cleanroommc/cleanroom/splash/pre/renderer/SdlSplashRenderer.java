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
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.ui.font.McTtfFontManager;
import com.cleanroommc.kirino.ui.simplegui.SimpleGuiRuntime;
import com.cleanroommc.kirino.ui.simpletext.ST_Config;
import com.cleanroommc.kirino.ui.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DArrayGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextProducer;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextRenderer;
import com.cleanroommc.kirino.ui.simpletext.backend.FreeTypeFontHandle;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;
import com.cleanroommc.mcttf.glyph.GlyphBuilder;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourceIndexFolder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.freetype.FreeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class SdlSplashRenderer implements AutoCloseable {

    private final GLResourceManager.ResourceScope scope;

    private final FreeTypeManager freeType;
    private final McTtfFontManager fonts;
    private final SimpleTextRuntime text;
    private final SimpleGuiRuntime gui;

    private @Nullable PreSplashState state;

    public SdlSplashRenderer(File assetsDirectory, @Nullable String assetIndex) throws Exception {
        Preconditions.checkNotNull(assetsDirectory);

        scope = GLResourceManager.openScope();

        try {
            var constructor = FreeTypeManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FreeTypeManager freeType = constructor.newInstance();
            freeType.loggerName = "pre_splash";
            freeType.init();
            this.freeType = freeType;

            Preconditions.checkNotNull(assetsDirectory);
            ResourceIndex index = assetIndex == null
                    ? new ResourceIndexFolder(assetsDirectory)
                    : new ResourceIndex(assetsDirectory, assetIndex);
            fonts = new McTtfFontManager(new DefaultResourcePack(index), freeType);
            fonts.loggerName = "pre_splash";
            var face = fonts.loadDefaultFace(48);
            Preconditions.checkState(face != null, "Could not prepare the default Minecraft font.");

            ImmediateShaderAccess shaders = new ImmediateShaderAccess(rl -> {
                String path = "/assets/" + rl.getNamespace() + "/" + rl.getPath();
                try (InputStream input = SdlSplashRenderer.class.getResourceAsStream(path)) {
                    Preconditions.checkNotNull(input, "Missing pre splash shader: %s", path);
                    return new String(input.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not load pre splash shader " + path, e);
                }
            });

            ST_Config config = new ST_Config(
                    ST_FontBackendType.FREE_TYPE,
                    48,
                    16,
                    12,
                    FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);

            AttributeLayout layout = new AttributeLayout();
            layout.push(new Stride(0));
            VAO dummyVao = new VAO(layout, null, (VBOView @Nullable []) null);

            float uiWidth = PreSplashLayout.WIDTH / PreSplashLayout.UI_SCALE;
            float uiHeight = PreSplashLayout.HEIGHT / PreSplashLayout.UI_SCALE;
            gui = new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight);
            text = new SimpleTextRuntime(
                    (_, _) -> new FreeTypeFontHandle(face),
                    runtime -> {
                        DefaultTextRenderer renderer = new DefaultTextRenderer(runtime, new Tex2DArrayGlyphAtlas(1024, 1024), shaders, 256, uiWidth, uiHeight);
                        renderer.loggerName = "pre_splash";
                        return renderer;
                    },
                    runtime -> new DefaultTextProducer(runtime, config.pixelSize(), GlyphBuilder.LINE_HEIGHT_PIXELS),
                    shaders,
                    new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight),
                    new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight), config, null);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } catch (Exception | Error failure) {
            try {
                close();
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    public void update(PreSplashState state) {
        if (this.state == state) {
            return;
        }

        this.state = state;
    }

    public void render(int width, int height) {
        if (state == null) {
            return;
        }

        float scale = Math.min((float) width / PreSplashLayout.WIDTH, (float) height / PreSplashLayout.HEIGHT);
        int viewportWidth = Math.round(PreSplashLayout.WIDTH * scale);
        int viewportHeight = Math.round(PreSplashLayout.HEIGHT * scale);
        GL11.glViewport((width - viewportWidth) / 2, (height - viewportHeight) / 2, viewportWidth, viewportHeight);

        int background = PreSplashLayout.BACKGROUND;
        GL11.glClearColor(((background >> 16) & 255) / 255f, ((background >> 8) & 255) / 255f, (background & 255) / 255f, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        text.begin();
        text.append(state.title(), 48, 106, 32, 0xFFF1F4FA);
        text.append(state.message(), 48, 190, 16, 0xFFB9C7DB);
        text.append(state.details(), 48, 286, 16, 0xFF8998AE);
        text.endDraw();
    }

    @Override
    public void close() throws Exception {
        try {
            text.close();
        } finally {
            try {
                gui.close();
            } finally {
                try {
                    fonts.destroy();
                } finally {
                    try {
                        freeType.destroy();
                    } finally {
                        scope.close();
                    }
                }
            }
        }
    }
}
