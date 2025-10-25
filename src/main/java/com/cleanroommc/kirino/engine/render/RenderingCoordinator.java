package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferManager;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.WhateverPass;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.render.utils.ResolutionContainer;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class RenderingCoordinator {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final ResolutionContainer resolution;
    private final List<Framebuffer> framebuffers;

    public final MinecraftScene scene;
    public final MinecraftCamera camera;

    private final ShaderRegistry shaderRegistry;
    private final GLSLRegistry glslRegistry;
    private final DefaultShaderAnalyzer defaultShaderAnalyzer;

    private final StagingBufferManager stagingBufferManager;
    private final GraphicResourceManager graphicResourceManager;

    public final GizmosManager gizmosManager;

    private final IndirectDrawBufferManager idbManager;

    private final RenderPass chunkCpuPass;
    private final RenderPass gizmosPass;

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public RenderingCoordinator(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime) {
        framebuffers = new ArrayList<>();
        resolution = new ResolutionContainer((width, height) -> {
            for (Framebuffer framebuffer : framebuffers) {
                framebuffer.resize(width, height);
            }
        });

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

        idbManager = new IndirectDrawBufferManager(1024 * 1024);

        //stagingBufferManager.genPersistentBuffers("default", 256, 256);

        ShaderProgram shaderProgram = shaderRegistry.newShaderProgram("forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag");

        Renderer renderer = new Renderer();
        chunkCpuPass = new RenderPass("Chunk CPU Pass", idbManager);
        chunkCpuPass.addSubpass("Opaque Pass", new WhateverPass(graphicResourceManager, renderer, PSOPresets.createOpaquePSO(shaderProgram), new Framebuffer(0, 0)));
        chunkCpuPass.addSubpass("Cutout Pass", new WhateverPass(graphicResourceManager, renderer, PSOPresets.createCutoutPSO(shaderProgram), new Framebuffer(0, 0)));
        chunkCpuPass.addSubpass("Transparent Pass", new WhateverPass(graphicResourceManager, renderer, PSOPresets.createTransparentPSO(shaderProgram), new Framebuffer(0, 0)));

        Framebuffer framebuffer = new Framebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        framebuffers.add(framebuffer);
        gizmosPass = new RenderPass("Gizmos Pass", idbManager);
        gizmosPass.addSubpass("Gizmos Pass", new GizmosPass(
                graphicResourceManager,
                renderer,
                PSOPresets.createGizmosPSO(shaderProgram),
                framebuffer,
                gizmosManager));
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
        resolution.update();
    }

    public void postUpdate() {
        // upscale framebuffer
        // post process
    }

    public void runChunkCpuPass() {
        //chunkCpuPass.render(camera);
    }

    // workaround
    GLStateBackup stateBackup = new GLStateBackup();
    public void runGizmosPass() {
        stateBackup.storeStates();
        gizmosPass.render(camera);
        stateBackup.restoreStates();

        // workaround: prevent slient crash
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
