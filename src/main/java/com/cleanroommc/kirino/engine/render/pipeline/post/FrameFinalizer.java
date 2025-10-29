package com.cleanroommc.kirino.engine.render.pipeline.post;

import com.cleanroommc.kirino.engine.render.framebuffer.PingPongFramebuffer;
import com.cleanroommc.kirino.engine.render.framebuffer.ResolutionContainer;
import com.cleanroommc.kirino.engine.render.framebuffer.ScalableFramebuffer;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.DepthStencilAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.Texture2DView;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

public class FrameFinalizer {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final Logger logger;

    public final boolean enableHDR;
    public final boolean enablePostProcessing;

    private final PostProcessingPass postProcessingPass;
    private final RenderPass toneMappingPass;

    private ResolutionContainer resolution;
    private ScalableFramebuffer mainFramebuffer;
    private PingPongFramebuffer pingPongFramebuffer; // postProcessingManager is the only place to swap and use this framebuffer
    private Framebuffer intermediateFramebuffer; // use it to do scaling if (hdr is on AND (post-processing is disabled OR post-processing pass count == 1))
    private net.minecraft.client.shader.Framebuffer minecraftFramebuffer;

    public ResolutionContainer getResolution() {
        return resolution;
    }

    public ScalableFramebuffer getMainFramebuffer() {
        return mainFramebuffer;
    }

    public PingPongFramebuffer getPingPongFramebuffer() {
        return pingPongFramebuffer;
    }

    public Framebuffer getIntermediateFramebuffer() {
        return intermediateFramebuffer;
    }

    public net.minecraft.client.shader.Framebuffer getMinecraftFramebuffer() {
        return minecraftFramebuffer;
    }

    public FrameFinalizer(Logger logger, PostProcessingPass postProcessingPass, RenderPass toneMappingPass, boolean enableHDR, boolean enablePostProcessing) {
        this.enableHDR = enableHDR;
        this.enablePostProcessing = enablePostProcessing;
        this.postProcessingPass = postProcessingPass;
        this.toneMappingPass = toneMappingPass;
        this.logger = logger;
    }

    /**
     * It modifies framebuffer binding, clear hint, and viewport. Restore these OpenGL states on your own after this call.
     *
     * @param minecraftFramebuffer The Minecraft's framebuffer
     */
    public void initResources(net.minecraft.client.shader.Framebuffer minecraftFramebuffer) {
        this.minecraftFramebuffer = minecraftFramebuffer;

        mainFramebuffer = new ScalableFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight, 1f);

        final boolean initIntermediate = enableHDR && (!enablePostProcessing || postProcessingPass.getSubpassCount() == 1);

        if (initIntermediate) {
            intermediateFramebuffer = new Framebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        } else {
            pingPongFramebuffer = new PingPongFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        }

        //<editor-fold desc="resolution and callbacks">
        resolution = new ResolutionContainer((width, height) -> {

            // display resized callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getRatio()),
                    (int) (height * mainFramebuffer.getRatio()));

            if (initIntermediate) {
                intermediateFramebuffer.resize(width, height);
            } else {
                pingPongFramebuffer.resize(width, height);
            }

            logger.info("Display size updated. Current display width & height: " + width + ", " + height);
            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getRatio());

            if (initIntermediate) {
                logger.info("Intermediate framebuffer resized: width=" + intermediateFramebuffer.width() + ", height=" + intermediateFramebuffer.height());
            } else {
                logger.info("Post-processing framebuffer resized: width=" + pingPongFramebuffer.width() + ", height=" + pingPongFramebuffer.height());
            }

        }, (width, height) -> {

            // ratio changed callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getTargetRatio()),
                    (int) (height * mainFramebuffer.getTargetRatio()));

            if (initIntermediate) {
                intermediateFramebuffer.resize(width, height);
            } else {
                pingPongFramebuffer.resize(width, height);
            }

            logger.info("Main framebuffer ratio changed: " + mainFramebuffer.getRatio() + " -> " + mainFramebuffer.getTargetRatio());
            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getTargetRatio());

            if (initIntermediate) {
                logger.info("Intermediate framebuffer resized: width=" + intermediateFramebuffer.width() + ", height=" + intermediateFramebuffer.height());
            } else {
                logger.info("Post-processing framebuffer resized: width=" + pingPongFramebuffer.width() + ", height=" + pingPongFramebuffer.height());
            }

        });
        //</editor-fold>

        //<editor-fold desc="main framebuffer initialization">
        {
            mainFramebuffer.framebuffer.bind();

            Texture2DView color0Tex = new Texture2DView(new GLTexture(mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            mainFramebuffer.framebuffer.attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            mainFramebuffer.framebuffer.attach(new DepthStencilAttachment(depthTex));

            mainFramebuffer.framebuffer.check();

            GL11.glViewport(0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Main framebuffer created. ID: " + mainFramebuffer.framebuffer.fboID);
        }
        //</editor-fold>

        //<editor-fold desc="ping-pong framebuffer A initialization">
        if (!initIntermediate) {
            pingPongFramebuffer.framebufferA().bind();

            Texture2DView color0Tex = new Texture2DView(new GLTexture(pingPongFramebuffer.width(), pingPongFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            pingPongFramebuffer.framebufferA().attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(pingPongFramebuffer.width(), pingPongFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            pingPongFramebuffer.framebufferA().attach(new DepthStencilAttachment(depthTex));

            pingPongFramebuffer.framebufferA().check();

            GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Post-processing framebuffer A created. ID: " + pingPongFramebuffer.framebufferA().fboID);
        }
        //</editor-fold>

        //<editor-fold desc="ping-pong framebuffer B initialization">
        if (!initIntermediate) {
            pingPongFramebuffer.framebufferB().bind();

            Texture2DView color0Tex = new Texture2DView(new GLTexture(pingPongFramebuffer.width(), pingPongFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            pingPongFramebuffer.framebufferB().attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(pingPongFramebuffer.width(), pingPongFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            pingPongFramebuffer.framebufferB().attach(new DepthStencilAttachment(depthTex));

            pingPongFramebuffer.framebufferB().check();

            GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Post-processing framebuffer B created. ID: " + pingPongFramebuffer.framebufferB().fboID);
        }
        //</editor-fold>

        //<editor-fold desc="intermediate framebuffer initialization">
        if (initIntermediate) {
            intermediateFramebuffer.bind();

            Texture2DView color0Tex = new Texture2DView(new GLTexture(intermediateFramebuffer.width(), intermediateFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, TextureFormat.RGBA16F); // HDR is always on in this situation
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            intermediateFramebuffer.attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(intermediateFramebuffer.width(), intermediateFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            intermediateFramebuffer.attach(new DepthStencilAttachment(depthTex));

            intermediateFramebuffer.check();

            GL11.glViewport(0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Intermediate framebuffer created. ID: " + intermediateFramebuffer.fboID);
        }
        //</editor-fold>
    }

    public void updateResolution() {
        if (mainFramebuffer.isScheduledToResize()) {
            resolution.synchronize();
            mainFramebuffer.finishResize();
        }
        resolution.update();
    }

    public void bindMainFramebuffer(boolean viewport) {
        mainFramebuffer.framebuffer.bind();
        if (viewport) {
            GL11.glViewport(0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());
        }
    }

    public void bindMinecraftFramebuffer(boolean viewport) {
        Framebuffer.bind(minecraftFramebuffer.framebufferObject);
        if (viewport) {
            GL11.glViewport(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight);
        }
    }

    /**
     * <p><b>Input Framebuffer</b>: {@link #mainFramebuffer}</p>
     * <p><b>Output Framebuffer</b>: {@link #minecraftFramebuffer}</p>
     * <br>
     * Scales the input if necessary and then post-processes it if necessary, and also handles HDR tone mapping.
     * Whichever combination it is, the result will be blitted to minecraft framebuffer.
     * <br>
     * Framebuffer binding will be chaotic after this final pass. Bind framebuffer on your own after this pass.
     *
     * <p><b>⚠ WARNING: Combinatorial logic here is over complicated but it works! Must not touch this method and its related resources unless necessary.</b></p>
     */
    public void finalizeFramebuffer() {
        // todo: blit depth

        // main framebuffer -> minecraft framebuffer
        //<editor-fold desc="no hdr & no post-processing">
        if (!enableHDR && !enablePostProcessing) {
            if (mainFramebuffer.getRatio() == 1f) {
                // just in case the size of main framebuffer and minecraft frambuffer mismatches
                if (mainFramebuffer.framebuffer.width() != MINECRAFT.getFramebuffer().framebufferTextureWidth ||
                        mainFramebuffer.framebuffer.height() != MINECRAFT.getFramebuffer().framebufferTextureHeight) {
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MINECRAFT.getFramebuffer().framebufferObject);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, MINECRAFT.getFramebuffer().framebufferTextureWidth, MINECRAFT.getFramebuffer().framebufferTextureHeight,
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
                } else {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                    GL43.glCopyImageSubData(
                            colorAttachmentSrc.texture2D.texture.textureID,
                            colorAttachmentSrc.texture2D.target(),
                            0, 0, 0, 0,
                            MINECRAFT.getFramebuffer().framebufferTexture,
                            GL11.GL_TEXTURE_2D,
                            0, 0, 0, 0,
                            colorAttachmentSrc.texture2D.texture.width(),
                            colorAttachmentSrc.texture2D.texture.height(),
                            1);
                    GL42.glMemoryBarrier(GL42.GL_TEXTURE_FETCH_BARRIER_BIT | GL42.GL_FRAMEBUFFER_BARRIER_BIT);
                }
            } else if (mainFramebuffer.getRatio() < 1f) {
                // todo: upscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MINECRAFT.getFramebuffer().framebufferObject);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, MINECRAFT.getFramebuffer().framebufferTextureWidth, MINECRAFT.getFramebuffer().framebufferTextureHeight,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            } else if (mainFramebuffer.getRatio() > 1f) {
                // todo: downscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MINECRAFT.getFramebuffer().framebufferObject);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, MINECRAFT.getFramebuffer().framebufferTextureWidth, MINECRAFT.getFramebuffer().framebufferTextureHeight,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            }
        }
        //</editor-fold>

        // main framebuffer -(skipped when no scaling)-> intermediate framebuffer -> minecraft framebuffer
        //<editor-fold desc="hdr & no post-processing">
        if (enableHDR && !enablePostProcessing) {
            if (mainFramebuffer.getRatio() == 1f) {
                Framebuffer.bind(MINECRAFT.getFramebuffer().framebufferObject);
                GL11.glViewport(0, 0, MINECRAFT.getFramebuffer().framebufferWidth, MINECRAFT.getFramebuffer().framebufferHeight);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                toneMappingPass.render(null, null, new Object[]{mainFramebuffer.framebuffer});
            } else if (mainFramebuffer.getRatio() < 1f) {
                // todo: upscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediateFramebuffer.fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                Framebuffer.bind(MINECRAFT.getFramebuffer().framebufferObject);
                GL11.glViewport(0, 0, MINECRAFT.getFramebuffer().framebufferWidth, MINECRAFT.getFramebuffer().framebufferHeight);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                toneMappingPass.render(null, null, new Object[]{intermediateFramebuffer});
            } else if (mainFramebuffer.getRatio() > 1f) {
                // todo: downscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediateFramebuffer.fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                Framebuffer.bind(MINECRAFT.getFramebuffer().framebufferObject);
                GL11.glViewport(0, 0, MINECRAFT.getFramebuffer().framebufferWidth, MINECRAFT.getFramebuffer().framebufferHeight);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                toneMappingPass.render(null, null, new Object[]{intermediateFramebuffer});
            }
        }
        //</editor-fold>

        // main framebuffer -> ping-pong framebuffer A -> post-process -> minecraft framebuffer
        //<editor-fold desc="no hdr & post-processing">
        if (!enableHDR && enablePostProcessing) {
            if (mainFramebuffer.getRatio() == 1f) {
                ColorAttachment colorAttachmentSrc = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                ColorAttachment colorAttachmentDest = ((ColorAttachment) pingPongFramebuffer.framebufferA().getColorAttachment(0));
                GL43.glCopyImageSubData(
                        colorAttachmentSrc.texture2D.texture.textureID,
                        colorAttachmentSrc.texture2D.target(),
                        0, 0, 0, 0,
                        colorAttachmentDest.texture2D.texture.textureID,
                        colorAttachmentDest.texture2D.target(),
                        0, 0, 0, 0,
                        colorAttachmentSrc.texture2D.texture.width(),
                        colorAttachmentSrc.texture2D.texture.height(),
                        1);
                GL42.glMemoryBarrier(GL42.GL_TEXTURE_FETCH_BARRIER_BIT | GL42.GL_FRAMEBUFFER_BARRIER_BIT);

                if (postProcessingPass.getSubpassCount() == 1) {
                    postProcessingPass.postProcess(false);
                } else if (postProcessingPass.getSubpassCount() >= 2) {
                    postProcessingPass.postProcess();
                }
            } else if (mainFramebuffer.getRatio() < 1f) {
                // todo: upscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pingPongFramebuffer.framebufferA().fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                if (postProcessingPass.getSubpassCount() == 1) {
                    postProcessingPass.postProcess(false);
                } else if (postProcessingPass.getSubpassCount() >= 2) {
                    postProcessingPass.postProcess();
                }
            } else if (mainFramebuffer.getRatio() > 1f) {
                // todo: downscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pingPongFramebuffer.framebufferA().fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                if (postProcessingPass.getSubpassCount() == 1) {
                    postProcessingPass.postProcess(false);
                } else if (postProcessingPass.getSubpassCount() >= 2) {
                    postProcessingPass.postProcess();
                }
            }
        }
        //</editor-fold>

        // main framebuffer -> intermediate framebuffer / ping-pong framebuffer A -> post-process -> minecraft framebuffer
        //<editor-fold desc="hdr & post-processing">
        if (enableHDR && enablePostProcessing) {
            if (mainFramebuffer.getRatio() == 1f) {
                if (postProcessingPass.getSubpassCount() == 1) {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                    ColorAttachment colorAttachmentDest = ((ColorAttachment) intermediateFramebuffer.getColorAttachment(0));
                    GL43.glCopyImageSubData(
                            colorAttachmentSrc.texture2D.texture.textureID,
                            colorAttachmentSrc.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentDest.texture2D.texture.textureID,
                            colorAttachmentDest.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentSrc.texture2D.texture.width(),
                            colorAttachmentSrc.texture2D.texture.height(),
                            1);
                    GL42.glMemoryBarrier(GL42.GL_TEXTURE_FETCH_BARRIER_BIT | GL42.GL_FRAMEBUFFER_BARRIER_BIT);

                    postProcessingPass.postProcess(true);
                } else if (postProcessingPass.getSubpassCount() >= 2) {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                    ColorAttachment colorAttachmentDest = ((ColorAttachment) pingPongFramebuffer.framebufferA().getColorAttachment(0));
                    GL43.glCopyImageSubData(
                            colorAttachmentSrc.texture2D.texture.textureID,
                            colorAttachmentSrc.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentDest.texture2D.texture.textureID,
                            colorAttachmentDest.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentSrc.texture2D.texture.width(),
                            colorAttachmentSrc.texture2D.texture.height(),
                            1);
                    GL42.glMemoryBarrier(GL42.GL_TEXTURE_FETCH_BARRIER_BIT | GL42.GL_FRAMEBUFFER_BARRIER_BIT);

                    postProcessingPass.postProcess();
                }
            } else if (mainFramebuffer.getRatio() < 1f) {
                if (postProcessingPass.getSubpassCount() == 1) {
                    // todo: upscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediateFramebuffer.fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingPass.postProcess(true);
                } else if (postProcessingPass.getSubpassCount() >= 2) {
                    // todo: upscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pingPongFramebuffer.framebufferA().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingPass.postProcess();
                }
            } else if (mainFramebuffer.getRatio() > 1f) {
                if (postProcessingPass.getSubpassCount() == 1) {
                    // todo: downscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediateFramebuffer.fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingPass.postProcess(true);
                } else if (postProcessingPass.getSubpassCount() >= 2) {
                    // todo: downscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pingPongFramebuffer.framebufferA().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingPass.postProcess();
                }
            }
        }
        //</editor-fold>
    }
}
