package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.world.MinecraftWorld;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

public class KirinoEngine {
    public final MinecraftWorld world;
    public final MinecraftCamera camera;

    public final ShaderRegistry shaderRegistry;
    public final GLSLRegistry glslRegistry;
    public final DefaultShaderAnalyzer defaultShaderAnalyzer;

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
    }
}
