package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.*;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.*;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.pipeline.post.PostProcessingPass;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
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
    public final boolean enableHDR;
    public final boolean enablePostProcessing;

    // ---------- OpenGL Related Resources (initialization deferred) ----------
    private final FrameFinalizer frameFinalizer;
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

    // ---------- Render Passes ----------
    private final RenderPass chunkCpuPass;
    private final RenderPass gizmosPass;
    private final RenderPass upscalingPass;
    private final RenderPass downscalingPass;
    public final PostProcessingPass postProcessingPass;

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public RenderingCoordinator(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime, boolean enableHDR, boolean enablePostProcessing) {
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

        postProcessingPass = new PostProcessingPass(
                new RenderPass("Post-Processing", graphicResourceManager, idbGenerator),
                renderer,
                fullscreenTriangleVao);
        postProcessingPass.addSubpass("Tone Mapping Pass", shaderProgram, ToneMappingPass::new);

        RenderPass toneMappingPass = new RenderPass("Tone Mapping", graphicResourceManager, idbGenerator);
        toneMappingPass.addSubpass("Tone Mapping Pass", new ToneMappingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(shaderProgram),
                fullscreenTriangleVao));

        frameFinalizer = new FrameFinalizer(logger, postProcessingPass, toneMappingPass, enableHDR, enablePostProcessing);
    }

    private boolean deferredInit = true;

    /**
     * Defer all OpenGL related allocation.
     */
    private void deferredInit() {
        //<editor-fold desc="frame finalizer initialization">
        int[] result = new int[1];
        GL11C.glGetIntegerv(GL30.GL_DRAW_FRAMEBUFFER_BINDING, result);
        int drawFbo = result[0];
        GL11C.glGetIntegerv(GL30.GL_READ_FRAMEBUFFER_BINDING, result);
        int readFbo = result[0];
        float[] clearColor = new float[4];
        GL11C.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
        float[] clearDepth = new float[1];
        GL11C.glGetFloatv(GL11.GL_DEPTH_CLEAR_VALUE, clearDepth);
        int[] clearStencil = new int[1];
        GL11C.glGetIntegerv(GL11.GL_STENCIL_CLEAR_VALUE, clearStencil);
        int[] viewport = new int[4];
        GL11C.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        frameFinalizer.initResources(Minecraft.getMinecraft().getFramebuffer());

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        GL11.glClearDepth(clearDepth[0]);
        GL11.glClearStencil(clearStencil[0]);
        //</editor-fold>

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
            Preconditions.checkState(postProcessingPass.getSubpassCount() >= 1,
                    "Post-processing pass must have at least one subpass at runtime to work as expected");

            postProcessingPass.lateInit(
                    frameFinalizer.getMinecraftFramebuffer(),
                    frameFinalizer.getPingPongFramebuffer(),
                    frameFinalizer.getIntermediateFramebuffer());
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

        frameFinalizer.updateResolution();

        // current render target: main framebuffer
        frameFinalizer.bindMainFramebuffer(true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
    }

    public void postUpdate() {
        frameFinalizer.finalizeFramebuffer();

        // current render target: minecraft framebuffer
        frameFinalizer.bindMinecraftFramebuffer(true);

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
