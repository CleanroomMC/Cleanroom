package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.*;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.WhateverPass;
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
        chunkCpuPass = new RenderPass("Chunk CPU Pass", graphicResourceManager, idbGenerator);
        chunkCpuPass.addSubpass("Opaque Pass", new WhateverPass(renderer, PSOPresets.createOpaquePSO(shaderProgram), null));
        chunkCpuPass.addSubpass("Cutout Pass", new WhateverPass(renderer, PSOPresets.createCutoutPSO(shaderProgram), null));
        chunkCpuPass.addSubpass("Transparent Pass", new WhateverPass(renderer, PSOPresets.createTransparentPSO(shaderProgram), null));

        gizmosPass = new RenderPass("Gizmos Pass", graphicResourceManager, idbGenerator);
        gizmosPass.addSubpass("Gizmos Pass", new GizmosPass(
                renderer,
                PSOPresets.createGizmosPSO(shaderProgram),
                null,
                gizmosManager));
    }

    private boolean deferredInit = true;

    /**
     * Defer all OpenGL related allocation.
     */
    private void deferredInit() {
        mainFramebuffer = new MainFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight, 1f);
        resolution = new ResolutionContainer((width, height) -> {
            // this framebuffer is guaranteed to be bound when the callback runs
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getRatio()),
                    (int) (height * mainFramebuffer.getRatio()));

            logger.info("Main framebuffer resized: width=" + mainFramebuffer.framebuffer.width() + ", height=" + mainFramebuffer.framebuffer.height() + ", ratio=" + mainFramebuffer.getRatio());
            logger.info("Current display width & height: " + width + ", " + height);
        });

        mainFramebuffer.framebuffer.bind();

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
        resolution.update();
        if (mainFramebuffer.isScheduledToResize()) {
            resolution.synchronize();
            mainFramebuffer.finishResize();
        }
        GL11.glViewport(0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());
    }

    public void postUpdate() {
        // upscale framebuffer
        // post process

        // blit result to minecraft framebuffer and bind minecraft framebuffer
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFramebuffer.framebuffer.fboID);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MINECRAFT.getFramebuffer().framebufferObject);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBlitFramebuffer(
                0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(),
                0, 0, MINECRAFT.getFramebuffer().framebufferWidth, MINECRAFT.getFramebuffer().framebufferHeight,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);

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
