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
import java.util.concurrent.TimeUnit;

/**
 * <p>Note: Only used by {@link SdlSplashRenderer}.</p>
 */
final class LogoAnimationDrawer implements AutoCloseable {

    private static final ResourceLocation VERTEX_SHADER = new ResourceLocation("cleanroom:shaders/logo_animation.vert");
    private static final ResourceLocation FRAGMENT_SHADER = new ResourceLocation("cleanroom:shaders/logo_animation.frag");
    private static final ResourceLocation LOGO = new ResourceLocation("cleanroom:icon.png");

    private final StopWatch stopWatch = new StopWatch();
    private final VAO dummyVao;
    private final @Nullable Shader vertexShader;
    private final @Nullable Shader fragmentShader;
    private final @Nullable ShaderProgram program;
    private final @Nullable Texture2DAccessor logo;

    public LogoAnimationDrawer(ImmediateShaderAccess shaders, VAO dummyVao) throws IOException {
        Preconditions.checkNotNull(shaders);
        Preconditions.checkNotNull(dummyVao);

        this.dummyVao = dummyVao;

        try {
            vertexShader = shaders.makeShader(VERTEX_SHADER);
            fragmentShader = shaders.makeShader(FRAGMENT_SHADER);
            shaders.submitToGL(vertexShader, fragmentShader);
            program = shaders.makeProgram(vertexShader, fragmentShader);

            int resolutionLocation = program.uniformLocation("resolution");
            int progressLocation = program.uniformLocation("progress");
            int textureLocation = program.uniformLocation("logoTexture");
            int backgroundLocation = program.uniformLocation("background");
            Preconditions.checkState(resolutionLocation >= 0 && progressLocation >= 0 && textureLocation >= 0 && backgroundLocation >= 0,
                    "Missing logo animation uniforms.");

            program.uniform1i("logoTexture", 0);
            program.uniform3f("background",
                    ((PreSplashLayout.BACKGROUND >> 16) & 255) / 255f,
                    ((PreSplashLayout.BACKGROUND >> 8) & 255) / 255f,
                    (PreSplashLayout.BACKGROUND & 255) / 255f);

            logo = loadLogo();
        } catch (Throwable failure) {
            try {
                close();
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    private static Texture2DAccessor loadLogo() throws IOException {
        byte[] bytes;
        try (var input = MinecraftResourceUtils.getEarlyInputStream(LOGO)) {
            bytes = input.readAllBytes();
        }

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
                throw new IOException("Could not decode pre splash logo: " + STBImage.stbi_failure_reason());
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

    public void render(int width, int height) {
        Preconditions.checkNotNull(program);
        Preconditions.checkNotNull(logo);

        float progress = Math.min(1f, stopWatch.getTime(TimeUnit.MILLISECONDS) / (float) SdlSplashRenderer.LOGO_ANIM_DURATION_MILLIS);

        program.uniform2f("resolution", width, height);
        program.uniform1f("progress", progress);
        logo.bindUnit(0);
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
            if (logo != null) {
                logo.texture.disposeManually();
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
