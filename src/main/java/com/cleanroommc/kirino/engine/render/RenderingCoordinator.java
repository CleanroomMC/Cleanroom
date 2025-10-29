package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.*;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.*;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.DepthStencilAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.Texture2DView;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RenderingCoordinator {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final Logger logger;

    public final boolean enableHDR;
    public final boolean enablePostProcessing;

    // ---------- OpenGL Related Resources (initialization deferred) ----------
    private ResolutionContainer resolution;
    private MainFramebuffer mainFramebuffer;
    private PingPongFramebuffer postProcessingFramebuffer; // postProcessingManager is the only place to swap and use this framebuffer
    private Framebuffer intermediateFramebuffer; // use it to do scaling if (hdr is on AND (post-processing is disabled OR post-processing pass count == 1))
    private final AtomicReference<IndirectDrawBufferGenerator> idbGenerator;
    private final AtomicReference<VAO> fullscreenTriangleVao;

    // ---------- Utilities ----------
    private final GLStateBackup stateBackup;

    // ---------- Logic ----------
    public final MinecraftScene scene;
    public final MinecraftCamera camera;

    // ---------- Shaders ----------
    private final ShaderRegistry shaderRegistry;
    private final GLSLRegistry glslRegistry;
    private final DefaultShaderAnalyzer defaultShaderAnalyzer;

    // ---------- Managers ----------
    private final StagingBufferManager stagingBufferManager;
    private final GraphicResourceManager graphicResourceManager;
    public final GizmosManager gizmosManager;
    public final PostProcessingManager postProcessingManager;

    // ---------- Render Passes ----------
    private final RenderPass chunkCpuPass;
    private final RenderPass gizmosPass;
    private final RenderPass upscalingPass;
    private final RenderPass downscalingPass;
    private final RenderPass toneMappingPass;

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public RenderingCoordinator(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime, boolean enableHDR, boolean enablePostProcessing) {
        this.logger = logger;
        this.enableHDR = enableHDR;
        this.enablePostProcessing = enablePostProcessing;

        idbGenerator = new AtomicReference<>();
        fullscreenTriangleVao = new AtomicReference<>();

        stateBackup = new GLStateBackup();

        scene = new MinecraftScene(ecsRuntime.entityManager, ecsRuntime.jobScheduler);
        camera = new MinecraftCamera();

        shaderRegistry = new ShaderRegistry();
        ShaderRegistrationEvent shaderRegistrationEvent = new ShaderRegistrationEvent();
        eventBus.post(shaderRegistrationEvent);
        for (ResourceLocation rl : (List<ResourceLocation>) ReflectionUtils.getFieldValue(ReflectionUtils.findDeclaredField(ShaderRegistrationEvent.class, "shaderResourceLocations"), shaderRegistrationEvent)) {
            Shader shader = shaderRegistry.register(rl);
            logger.info("Registered " + shader.getShaderType().toString() + " shader " + rl + ".");
            if (shader.getShaderSource().isEmpty()) {
                logger.info("Warning! " + rl + " is empty.");
            }
        }
        shaderRegistry.compile();
        logger.info("Shader compilation passed.");
        glslRegistry = new GLSLRegistry();
        defaultShaderAnalyzer = new DefaultShaderAnalyzer();
        shaderRegistry.analyze(glslRegistry, defaultShaderAnalyzer);

        stagingBufferManager = new StagingBufferManager();
        graphicResourceManager = new GraphicResourceManager(stagingBufferManager);

        gizmosManager = new GizmosManager(graphicResourceManager);

        //stagingBufferManager.genPersistentBuffers("default", 256, 256);

        ShaderProgram shaderProgram = shaderRegistry.newShaderProgram("forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag");

        Renderer renderer = new Renderer();
        chunkCpuPass = new RenderPass("Chunk CPU", graphicResourceManager, idbGenerator);
//        chunkCpuPass.addSubpass("Opaque Pass", new WhateverPass(renderer, PSOPresets.createOpaquePSO(shaderProgram), null));
//        chunkCpuPass.addSubpass("Cutout Pass", new WhateverPass(renderer, PSOPresets.createCutoutPSO(shaderProgram), null));
//        chunkCpuPass.addSubpass("Transparent Pass", new WhateverPass(renderer, PSOPresets.createTransparentPSO(shaderProgram), null));

        gizmosPass = new RenderPass("Gizmos", graphicResourceManager, idbGenerator);
        gizmosPass.addSubpass("Gizmos Pass", new GizmosPass(
                renderer,
                PSOPresets.createGizmosPSO(shaderProgram),
                gizmosManager));

        upscalingPass = new RenderPass("Upscaling", graphicResourceManager, idbGenerator);
        upscalingPass.addSubpass("Upscaling Pass", new UpscalingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(shaderProgram)));

        downscalingPass = new RenderPass("Downscaling", graphicResourceManager, idbGenerator);
        downscalingPass.addSubpass("Downscaling Pass", new DownscalingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(shaderProgram)));

        shaderProgram = shaderRegistry.newShaderProgram("forge:shaders/post_processing.vert", "forge:shaders/post_processing.frag");

        postProcessingManager = new PostProcessingManager(
                new RenderPass("Post-Processing", graphicResourceManager, idbGenerator),
                camera,
                renderer,
                fullscreenTriangleVao);
        postProcessingManager.addSubpass("Tone Mapping Pass", shaderProgram, ToneMappingPass::new);

        toneMappingPass = new RenderPass("Tone Mapping", graphicResourceManager, idbGenerator);
        toneMappingPass.addSubpass("Tone Mapping Pass", new ToneMappingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(shaderProgram),
                fullscreenTriangleVao));
    }

    private boolean deferredInit = true;

    /**
     * Defer all OpenGL related allocation.
     */
    private void deferredInit() {
        mainFramebuffer = new MainFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight, 1f);

        final boolean initIntermediate = enableHDR && (!enablePostProcessing || postProcessingManager.getSubpassCount() == 1);

        if (initIntermediate) {
            intermediateFramebuffer = new Framebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        } else {
            postProcessingFramebuffer = new PingPongFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
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
                postProcessingFramebuffer.resize(width, height);
            }

            logger.info("Display size updated. Current display width & height: " + width + ", " + height);
            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getRatio());

            if (initIntermediate) {
                logger.info("Intermediate framebuffer resized: width=" + intermediateFramebuffer.width() + ", height=" + intermediateFramebuffer.height());
            } else {
                logger.info("Post-processing framebuffer resized: width=" + postProcessingFramebuffer.width() + ", height=" + postProcessingFramebuffer.height());
            }

        }, (width, height) -> {

            // ratio changed callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getTargetRatio()),
                    (int) (height * mainFramebuffer.getTargetRatio()));

            if (initIntermediate) {
                intermediateFramebuffer.resize(width, height);
            } else {
                postProcessingFramebuffer.resize(width, height);
            }

            logger.info("Main framebuffer ratio changed: " + mainFramebuffer.getRatio() + " -> " + mainFramebuffer.getTargetRatio());
            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getTargetRatio());

            if (initIntermediate) {
                logger.info("Intermediate framebuffer resized: width=" + intermediateFramebuffer.width() + ", height=" + intermediateFramebuffer.height());
            } else {
                logger.info("Post-processing framebuffer resized: width=" + postProcessingFramebuffer.width() + ", height=" + postProcessingFramebuffer.height());
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

        //<editor-fold desc="post-processing framebuffer A initialization">
        if (!initIntermediate) {
            postProcessingFramebuffer.framebufferA().bind();

            Texture2DView color0Tex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            postProcessingFramebuffer.framebufferA().attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            postProcessingFramebuffer.framebufferA().attach(new DepthStencilAttachment(depthTex));

            postProcessingFramebuffer.framebufferA().check();

            GL11.glViewport(0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Post-processing framebuffer A created. ID: " + postProcessingFramebuffer.framebufferA().fboID);
        }
        //</editor-fold>

        //<editor-fold desc="post-processing framebuffer B initialization">
        if (!initIntermediate) {
            postProcessingFramebuffer.framebufferB().bind();

            Texture2DView color0Tex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            postProcessingFramebuffer.framebufferB().attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            postProcessingFramebuffer.framebufferB().attach(new DepthStencilAttachment(depthTex));

            postProcessingFramebuffer.framebufferB().check();

            GL11.glViewport(0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Post-processing framebuffer B created. ID: " + postProcessingFramebuffer.framebufferB().fboID);
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

        // reset framebuffer binding. todo: store & restore states including fbo and clear hints
        Framebuffer.bind(MINECRAFT.getFramebuffer().framebufferObject);
        GL11.glViewport(0, 0, MINECRAFT.getFramebuffer().framebufferWidth, MINECRAFT.getFramebuffer().framebufferHeight);

        //<editor-fold desc="idb generator initialization">
        idbGenerator.set(new IndirectDrawBufferGenerator(1024 * 1024)); // 1MB
        //</editor-fold>

        //<editor-fold desc="fullscreen triangle vao initialization">
        AttributeLayout fullscreenTriangleLayout = new AttributeLayout();
        fullscreenTriangleLayout.push(new Stride(12).push(new Slot(Type.FLOAT, 3)));

        EBOView eboView = new EBOView(new GLBuffer());
        VBOView vboView = new VBOView(new GLBuffer());

        ByteBuffer eboByteBuffer = BufferUtils.createByteBuffer(3 * Byte.BYTES);
        eboByteBuffer.put((byte) 0).put((byte) 1).put((byte) 2);
        eboByteBuffer.position(0);
        eboByteBuffer.limit(3 * Byte.BYTES);

        ByteBuffer vboByteBuffer = BufferUtils.createByteBuffer(9 * Float.BYTES);
        vboByteBuffer.asFloatBuffer().put(new float[]{-1, -1, 0, 3, -1, 0, -1, 3, 0});
        vboByteBuffer.position(0);
        vboByteBuffer.limit(9 * Float.BYTES);

        eboView.bind();
        eboView.uploadDirectly(eboByteBuffer);
        eboView.bind(0);

        vboView.bind();
        vboView.uploadDirectly(vboByteBuffer);
        eboView.bind(0);

        VAO fullscreenTriangleVao = new VAO(fullscreenTriangleLayout, eboView, vboView);
        this.fullscreenTriangleVao.set(fullscreenTriangleVao);
        //</editor-fold>

        //<editor-fold desc="post-processing manager late initialization">
        if (enablePostProcessing) {
            postProcessingManager.lateInit(MINECRAFT.getFramebuffer(), postProcessingFramebuffer, intermediateFramebuffer);
        }
        //</editor-fold>
    }

    /**
     * Scale the {@link #mainFramebuffer} if necessary and then post-process if necessary.
     * Whichever combination it is, result will be blitted to minecraft framebuffer.
     */
    private void scaleAndPostProcessAndBlit() {
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
                toneMappingPass.render(camera, null, new Object[]{mainFramebuffer.framebuffer});
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
                toneMappingPass.render(camera, null, new Object[]{intermediateFramebuffer});
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
                toneMappingPass.render(camera, null, new Object[]{intermediateFramebuffer});
            }
        }
        //</editor-fold>

        // main framebuffer -> post-processing framebuffer A -> post-process -> minecraft framebuffer
        //<editor-fold desc="no hdr & post-processing">
        if (!enableHDR && enablePostProcessing) {
            if (mainFramebuffer.getRatio() == 1f) {
                ColorAttachment colorAttachmentSrc = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                ColorAttachment colorAttachmentDest = ((ColorAttachment) postProcessingFramebuffer.framebufferA().getColorAttachment(0));
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

                if (postProcessingManager.getSubpassCount() == 1) {
                    postProcessingManager.postProcess(false);
                } else if (postProcessingManager.getSubpassCount() >= 2) {
                    postProcessingManager.postProcess();
                }
            } else if (mainFramebuffer.getRatio() < 1f) {
                // todo: upscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, postProcessingFramebuffer.framebufferA().fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                if (postProcessingManager.getSubpassCount() == 1) {
                    postProcessingManager.postProcess(false);
                } else if (postProcessingManager.getSubpassCount() >= 2) {
                    postProcessingManager.postProcess();
                }
            } else if (mainFramebuffer.getRatio() > 1f) {
                // todo: downscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, postProcessingFramebuffer.framebufferA().fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                if (postProcessingManager.getSubpassCount() == 1) {
                    postProcessingManager.postProcess(false);
                } else if (postProcessingManager.getSubpassCount() >= 2) {
                    postProcessingManager.postProcess();
                }
            }
        }
        //</editor-fold>

        // main framebuffer -> intermediate framebuffer / post-processing framebuffer A -> post-process -> minecraft framebuffer
        //<editor-fold desc="hdr & post-processing">
        if (enableHDR && enablePostProcessing) {
            if (mainFramebuffer.getRatio() == 1f) {
                if (postProcessingManager.getSubpassCount() == 1) {
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

                    postProcessingManager.postProcess(true);
                } else if (postProcessingManager.getSubpassCount() >= 2) {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                    ColorAttachment colorAttachmentDest = ((ColorAttachment) postProcessingFramebuffer.framebufferA().getColorAttachment(0));
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

                    postProcessingManager.postProcess();
                }
            } else if (mainFramebuffer.getRatio() < 1f) {
                if (postProcessingManager.getSubpassCount() == 1) {
                    // todo: upscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediateFramebuffer.fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess(true);
                } else if (postProcessingManager.getSubpassCount() >= 2) {
                    // todo: upscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, postProcessingFramebuffer.framebufferA().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess();
                }
            } else if (mainFramebuffer.getRatio() > 1f) {
                if (postProcessingManager.getSubpassCount() == 1) {
                    // todo: downscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediateFramebuffer.fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess(true);
                } else if (postProcessingManager.getSubpassCount() >= 2) {
                    // todo: downscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, postProcessingFramebuffer.framebufferA().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                            0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess();
                }
            }
        }
        //</editor-fold>
    }

    public void update() {
        // ecs worlds update

        graphicResourceManager.runStaging();
    }

    public void updateWorld(WorldClient minecraftWorld) {
        scene.tryUpdateChunkProvider(minecraftWorld.getChunkProvider());
        scene.update();
    }

    public void preUpdate() {
        // only read states once to prevent huge amount of pipeline stalls
        if (!stateBackup.isStored()) {
            stateBackup.storeStates();
        }

        if (deferredInit) {
            deferredInit();
            deferredInit = false;
        }

        if (mainFramebuffer.isScheduledToResize()) {
            resolution.synchronize();
            mainFramebuffer.finishResize();
        }
        resolution.update();
        mainFramebuffer.framebuffer.bind();

        // current render target: main framebuffer
        GL11.glViewport(0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());
        GL11.glClearDepth(1);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
    }

    public void postUpdate() {
        scaleAndPostProcessAndBlit();

        // current render target: minecraft framebuffer
        Framebuffer.bind(MINECRAFT.getFramebuffer().framebufferObject);
        GL11.glViewport(0, 0, MINECRAFT.getFramebuffer().framebufferWidth, MINECRAFT.getFramebuffer().framebufferHeight);

        // reset everything to prevent any unexpected behavior
        stateBackup.restoreStates();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void runChunkCpuPass() {
        //chunkCpuPass.render(camera);
    }

    public void runGizmosPass() {
        gizmosPass.render(camera);
    }
}
