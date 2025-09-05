package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.world.MinecraftWorld;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

/**
 * Must only maintain one {@link KirinoEngine} instance.
 */
public class KirinoEngine {
    public final MinecraftWorld world;
    public final ShaderRegistry shaderRegistry;

    private KirinoEngine(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime) {
        world = new MinecraftWorld(ecsRuntime.entityManager);
        shaderRegistry = new ShaderRegistry();
    }
}
