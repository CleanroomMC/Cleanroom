package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.pipeline.Renderer;
import com.cleanroommc.kirino.engine.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.pipeline.pass.WhateverPass;
import com.cleanroommc.kirino.engine.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.world.MinecraftWorld;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

public class KirinoEngine {
    public final MinecraftWorld world;
    public final MinecraftCamera camera;

    private final ShaderRegistry shaderRegistry;
    private final GLSLRegistry glslRegistry;
    private final DefaultShaderAnalyzer defaultShaderAnalyzer;

    private final RenderPass mainCpuPass;
    private final RenderPass gizmosPass;

    private KirinoEngine(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime) {
        world = new MinecraftWorld(ecsRuntime.entityManager);
        camera = new MinecraftCamera();

        shaderRegistry = new ShaderRegistry();
        ShaderRegistrationEvent shaderRegistrationEvent = new ShaderRegistrationEvent();
        eventBus.post(shaderRegistrationEvent);
        for (ResourceLocation rl : shaderRegistrationEvent.shaderResourceLocations) {
            shaderRegistry.register(rl);
        }
        shaderRegistry.compile();
        glslRegistry = new GLSLRegistry();
        defaultShaderAnalyzer = new DefaultShaderAnalyzer();
        shaderRegistry.analyze(glslRegistry, defaultShaderAnalyzer);

        ShaderProgram shaderProgram = shaderRegistry.newShaderProgram("kirino:shaders/test.vert", "kirino:shaders/test.vert");

        Renderer renderer = new Renderer();
        mainCpuPass = new RenderPass("Main CPU Pass");
        mainCpuPass.addSubpass("Opaque Pass", new WhateverPass(renderer, PSOPresets.createOpaquePSO(shaderProgram), new Framebuffer()));
        mainCpuPass.addSubpass("Cutout Pass", new WhateverPass(renderer, PSOPresets.createCutoutPSO(shaderProgram), new Framebuffer()));
        mainCpuPass.addSubpass("Transparent Pass", new WhateverPass(renderer, PSOPresets.createTransparentPSO(shaderProgram), new Framebuffer()));

        gizmosPass = new RenderPass("Gizmos");
    }

    public void updateWorld(WorldClient minecraftWorld) {
        world.tryUpdateChunkProvider(minecraftWorld.getChunkProvider());
        world.update();
    }

    public void renderWorldSolid() {
        //mainCpuPass.render();
    }

    public void renderWorldTransparent() {

    }

    public void renderGizmos() {
        gizmosPass.render();
    }
}
