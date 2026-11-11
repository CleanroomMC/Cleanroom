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

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.ResourceLocation;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cleanroommc.cleanroom.splash.pre.renderer.PreSplashLogger.LOGGER;

/**
 * <p>Note: Only used by {@link SdlSplashRenderer}.</p>
 */
final class IconDrawer implements AutoCloseable {

    private static final ResourceLocation VERTEX_SHADER = new ResourceLocation("cleanroom:shaders/icon.vert");
    private static final ResourceLocation FRAGMENT_SHADER = new ResourceLocation("cleanroom:shaders/icon.frag");
    private static final ResourceLocation LOGO = new ResourceLocation("cleanroom:icon.png");

    private final StopWatch stopWatch = new StopWatch();
    private final VAO dummyVao; // borrowed
    private final @Nullable Shader vertexShader;
    private final @Nullable Shader fragmentShader;
    private final @Nullable ShaderProgram program;
    private final @Nullable Texture2DAccessor icon;

    IconDrawer(ImmediateShaderAccess shaders, VAO dummyVao, Path gameDirectory) throws IOException {
        Preconditions.checkNotNull(shaders);
        Preconditions.checkNotNull(gameDirectory);
        Preconditions.checkNotNull(dummyVao);

        this.dummyVao = dummyVao;

        try {
            vertexShader = shaders.makeShader(VERTEX_SHADER);
            fragmentShader = shaders.makeShader(FRAGMENT_SHADER);
            shaders.submitToGL(vertexShader, fragmentShader);
            program = shaders.makeProgram(vertexShader, fragmentShader);

            for (String uniform : List.of("resolution", "progress", "logoTexture", "logoCenter", "logoScale", "background")) {
                Preconditions.checkState(program.uniformLocation(uniform) >= 0, "Missing uniform: %s", uniform);
            }

            program.uniform1i("logoTexture", 0);
            program.uniform2f("resolution", PreSplashLayout.WIDTH, PreSplashLayout.HEIGHT);
            program.uniform3f("background",
                    ((PreSplashLayout.BACKGROUND >> 16) & 255) / 255f,
                    ((PreSplashLayout.BACKGROUND >> 8) & 255) / 255f,
                    (PreSplashLayout.BACKGROUND & 255) / 255f);

            icon = loadIcon(gameDirectory);
        } catch (Throwable failure) {
            try {
                close();
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    private static Texture2DAccessor loadIcon(Path gameDirectory) throws IOException {
        Path customIcon = gameDirectory.resolve("config").resolve("cleanroom_pre_splash_icon.png");

        if (Files.isRegularFile(customIcon)) {
            try {
                return loadIcon(Files.readAllBytes(customIcon));
            } catch (IOException failure) {
                LOGGER.warn("Could not load custom pre splash icon {}.", customIcon, failure);
            }
        }

        try (var input = MinecraftResourceUtils.getEarlyInputStream(LOGO)) {
            return loadIcon(input.readAllBytes());
        }
    }

    private static Texture2DAccessor loadIcon(byte[] bytes) throws IOException {
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length);
        ByteBuffer pixels = null;
        GLTexture texture = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            encoded.put(bytes).flip();
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);
            pixels = STBImage.stbi_load_from_memory(encoded, width, height, channels, STBImage.STBI_rgb_alpha);
            if (pixels == null) {
                throw new IOException("Could not decode pre splash icon: " + STBImage.stbi_failure_reason());
            }

            texture = GLTexture.newDsaTex2D(width.get(0), height.get(0));
            Texture2DAccessor accessor = new Texture2DAccessor(true, texture);
            accessor.highlevel().alloc(false, pixels);
            accessor.setCommonParams(FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
            return accessor;
        } catch (Throwable failure) {
            if (texture != null) {
                try {
                    texture.disposeManually();
                } catch (Throwable cleanup) {
                    failure.addSuppressed(cleanup);
                }
            }
            throw failure;
        } finally {
            if (pixels != null) {
                STBImage.stbi_image_free(pixels);
            }
            MemoryUtil.memFree(encoded);
        }
    }

    void render(float x, float y, float width, float height) {
        Preconditions.checkNotNull(program);
        Preconditions.checkNotNull(icon);
        Preconditions.checkArgument(width > 0 && height > 0);

        float progress = Math.min(1f, stopWatch.getTime(TimeUnit.MILLISECONDS) / (float) SdlSplashRenderer.ICON_ANIM_DURATION_MILLIS);

        program.uniform2f("logoCenter", (x + width / 2f) / PreSplashLayout.WIDTH, 1f - (y + height / 2f) / PreSplashLayout.HEIGHT);
        program.uniform2f("logoScale", width / PreSplashLayout.WIDTH, height / PreSplashLayout.HEIGHT);
        program.uniform1f("progress", progress);
        icon.bindUnit(0);
        dummyVao.bind();

        program.use();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        program.use0();

        VAO.bind(0);
        GL45.glBindTextureUnit(0, 0);

        if (!stopWatch.isStarted()) {
            stopWatch.start();
        }
    }

    @Override
    public void close() {
        try {
            if (icon != null) {
                icon.texture.disposeManually();
            }
        } finally {
            try {
                if (program != null) {
                    program.disposeManually();
                }
            } finally {
                try {
                    if (fragmentShader != null && fragmentShader.getShaderID() != 0) {
                        fragmentShader.disposeManually();
                    }
                } finally {
                    if (vertexShader != null && vertexShader.getShaderID() != 0) {
                        vertexShader.disposeManually();
                    }
                }
            }
        }
    }
}
