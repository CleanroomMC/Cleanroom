package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.WhateverPass;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.utils.ResolutionContainer;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RenderingCoordinator {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final ResolutionContainer resolution;
    private final List<Framebuffer> framebuffers;

    public final MinecraftScene scene;
    public final MinecraftCamera camera;

    public final GizmosManager gizmosManager;

    private final ShaderRegistry shaderRegistry;
    private final GLSLRegistry glslRegistry;
    private final DefaultShaderAnalyzer defaultShaderAnalyzer;

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

        gizmosManager = new GizmosManager();

        shaderRegistry = new ShaderRegistry();
        ShaderRegistrationEvent shaderRegistrationEvent = new ShaderRegistrationEvent();
        eventBus.post(shaderRegistrationEvent);
        for (ResourceLocation rl : (List<ResourceLocation>) ReflectionUtils.getFieldValue(ReflectionUtils.findDeclaredField(ShaderRegistrationEvent.class, "shaderResourceLocations"), shaderRegistrationEvent)) {
            shaderRegistry.register(rl);
            logger.info("Registered shader " + rl + ".");
        }
        shaderRegistry.compile();
        logger.info("Shader compilation passed.");
        glslRegistry = new GLSLRegistry();
        defaultShaderAnalyzer = new DefaultShaderAnalyzer();
        shaderRegistry.analyze(glslRegistry, defaultShaderAnalyzer);

        ShaderProgram shaderProgram = shaderRegistry.newShaderProgram("kirino:shaders/test.vert", "kirino:shaders/test.vert");

        Renderer renderer = new Renderer();
        chunkCpuPass = new RenderPass("Chunk CPU Pass");
        chunkCpuPass.addSubpass("Opaque Pass", new WhateverPass(renderer, PSOPresets.createOpaquePSO(shaderProgram), new Framebuffer(0, 0)));
        chunkCpuPass.addSubpass("Cutout Pass", new WhateverPass(renderer, PSOPresets.createCutoutPSO(shaderProgram), new Framebuffer(0, 0)));
        chunkCpuPass.addSubpass("Transparent Pass", new WhateverPass(renderer, PSOPresets.createTransparentPSO(shaderProgram), new Framebuffer(0, 0)));

        shaderProgram = shaderRegistry.newShaderProgram("kirino:shaders/gizmos_line.vert", "kirino:shaders/gizmos_line.frag");

        gizmosPass = new RenderPass("Gizmos Pass");
        gizmosPass.addSubpass("Gizmos Line Pass", new GizmosPass(
                renderer,
                PSOPresets.createGizmosLinePSO(shaderProgram),
                new Framebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight),
                gizmosManager));
    }

    public void preUpdate() {
        resolution.update();
    }

    public void postUpdate() {

    }

    public void updateWorld(WorldClient minecraftWorld) {
        scene.tryUpdateChunkProvider(minecraftWorld.getChunkProvider());
        scene.update();
    }

    public void runChunkCpuPass() {
        //chunkCpuPass.render();
    }

    public void runGizmosPass() {
        gizmosPass.render();
    }
}
