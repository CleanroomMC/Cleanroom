package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.*;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.DownscalingPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.UpscalingPass;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
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
import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RenderingCoordinator {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private final Logger logger;

    // ---------- OpenGL Related Resources (initialization deferred) ----------

    private ResolutionContainer resolution;
    private MainFramebuffer mainFramebuffer;
    private PingPongFramebuffer postProcessingFramebuffer;
    private final AtomicReference<IndirectDrawBufferGenerator> idbGenerator;

    // --------------------

    private final GLStateBackup stateBackup;

    public final MinecraftScene scene;
    public final MinecraftCamera camera;

    private final ShaderRegistry shaderRegistry;
    private final GLSLRegistry glslRegistry;
    private final DefaultShaderAnalyzer defaultShaderAnalyzer;

    private final StagingBufferManager stagingBufferManager;
    private final GraphicResourceManager graphicResourceManager;

    public final GizmosManager gizmosManager;

    private final RenderPass chunkCpuPass;
    private final RenderPass gizmosPass;
    private final RenderPass upscalingPass;
    private final RenderPass downscalingPass;
    private final RenderPass postProcessingPass;

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public RenderingCoordinator(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime) {
        this.logger = logger;

        idbGenerator = new AtomicReference<>();

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
                null,
                gizmosManager));

        upscalingPass = new RenderPass("Upscaling", graphicResourceManager, idbGenerator);
        upscalingPass.addSubpass("Upscaling Pass", new UpscalingPass(
                renderer,
                PSOPresets.createGizmosPSO(shaderProgram),
                null));

        downscalingPass = new RenderPass("Downscaling", graphicResourceManager, idbGenerator);
        downscalingPass.addSubpass("Downscaling Pass", new DownscalingPass(
                renderer,
                PSOPresets.createGizmosPSO(shaderProgram),
                null));

        postProcessingPass = new RenderPass("Post-Processing", graphicResourceManager, idbGenerator);
    }

    private boolean deferredInit = true;

    /**
     * Defer all OpenGL related allocation.
     */
    private void deferredInit() {
        mainFramebuffer = new MainFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight, 1f);
        postProcessingFramebuffer = new PingPongFramebuffer(mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());

        resolution = new ResolutionContainer((width, height) -> {

            // display resized callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getRatio()),
                    (int) (height * mainFramebuffer.getRatio()));
            postProcessingFramebuffer.resize(mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());

            logger.info("Display size updated. Current display width & height: " + width + ", " + height);
            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getRatio());
            logger.info("Post-processing framebuffer resized: width=" + postProcessingFramebuffer.width() + ", height=" + postProcessingFramebuffer.height());

        }, (width, height) -> {

            // ratio changed callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getTargetRatio()),
                    (int) (height * mainFramebuffer.getTargetRatio()));
            postProcessingFramebuffer.resize(mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());

            logger.info("Main framebuffer ratio changed: " + mainFramebuffer.getRatio() + " -> " + mainFramebuffer.getTargetRatio());
            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getRatio());
            logger.info("Post-processing framebuffer resized: width=" + postProcessingFramebuffer.width() + ", height=" + postProcessingFramebuffer.height());

        });

        mainFramebuffer.framebuffer.bind();

        // main framebuffer initialization
        {
            Texture2DView color0Tex = new Texture2DView(new GLTexture(mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, TextureFormat.RGBA8_UNORM);
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

        postProcessingFramebuffer.framebufferA.bind();

        // post-processing framebuffer A initialization
        {
            Texture2DView color0Tex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            postProcessingFramebuffer.framebufferA.attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            postProcessingFramebuffer.framebufferA.attach(new DepthStencilAttachment(depthTex));

            postProcessingFramebuffer.framebufferA.check();

            GL11.glViewport(0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Post-processing framebuffer A created. ID: " + postProcessingFramebuffer.framebufferA.fboID);
        }

        postProcessingFramebuffer.framebufferB.bind();

        // post-processing framebuffer B initialization
        {
            Texture2DView color0Tex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            color0Tex.bind();
            color0Tex.alloc(null, TextureFormat.RGBA8_UNORM);
            color0Tex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            postProcessingFramebuffer.framebufferB.attach(new ColorAttachment(0, color0Tex));

            Texture2DView depthTex = new Texture2DView(new GLTexture(postProcessingFramebuffer.width(), postProcessingFramebuffer.height()));
            depthTex.bind();
            depthTex.alloc(null, TextureFormat.D24S8);
            depthTex.set(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            postProcessingFramebuffer.framebufferB.attach(new DepthStencilAttachment(depthTex));

            postProcessingFramebuffer.framebufferB.check();

            GL11.glViewport(0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Post-processing framebuffer B created. ID: " + postProcessingFramebuffer.framebufferB.fboID);
        }

        Framebuffer.bind(0);

        idbGenerator.set(new IndirectDrawBufferGenerator(1024 * 1024));
    }

    public void update() {
        // ecs worlds update

        graphicResourceManager.runStaging();

        // fetch id and index from staging buffer manager
        // build render object to id & index map
    }

    public void updateWorld(WorldClient minecraftWorld) {
        scene.tryUpdateChunkProvider(minecraftWorld.getChunkProvider());
        scene.update();
    }

    public void preUpdate() {
        // only read once to prevent huge amount of pipeline stalls
        if (!stateBackup.isStored()) {
            stateBackup.storeStates();
        }

        if (deferredInit) {
            deferredInit();
            deferredInit = false;
        }

        mainFramebuffer.framebuffer.bind();
        if (mainFramebuffer.isScheduledToResize()) {
            resolution.synchronize();
            mainFramebuffer.finishResize();
        }
        resolution.update();
        GL11.glViewport(0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());
    }

    public void postUpdate() {
        // upscale framebuffer
        // post process

        // blit result to minecraft framebuffer / post-processing framebuffer
        if (mainFramebuffer.getRatio() == 1f) {
            // simply blit
            if (mainFramebuffer.framebuffer.width() != MINECRAFT.getFramebuffer().framebufferTextureWidth ||
                    mainFramebuffer.framebuffer.height() != MINECRAFT.getFramebuffer().framebufferTextureHeight) {
                // old blit method
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MINECRAFT.getFramebuffer().framebufferObject);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                        0, 0, MINECRAFT.getFramebuffer().framebufferTextureWidth, MINECRAFT.getFramebuffer().framebufferTextureHeight,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
            } else {
                // straight up copy
                ColorAttachment colorAttachment = ((ColorAttachment) mainFramebuffer.framebuffer.getColorAttachment(0));
                GL43.glCopyImageSubData(
                        colorAttachment.texture2D.texture.textureID,
                        colorAttachment.texture2D.target(),
                        0, 0, 0, 0,
                        MINECRAFT.getFramebuffer().framebufferTexture,
                        GL11.GL_TEXTURE_2D,
                        0, 0, 0, 0,
                        colorAttachment.texture2D.texture.width(),
                        colorAttachment.texture2D.texture.height(),
                        1);
            }
        } else if (mainFramebuffer.getRatio() < 1f) {
            // upscale

        } else if (mainFramebuffer.getRatio() > 1f) {
            // downscale

        }

        // reset everything to prevent any unexpected behavior
        Framebuffer.bind(MINECRAFT.getFramebuffer().framebufferObject);
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
