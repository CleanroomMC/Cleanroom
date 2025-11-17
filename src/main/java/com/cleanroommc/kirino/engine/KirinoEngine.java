package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.RenderingCoordinator;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

public class KirinoEngine {
    public final RenderingCoordinator renderingCoordinator;

    private KirinoEngine(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime,
                         boolean enableHDR, boolean enablePostProcessing) {
        renderingCoordinator = new RenderingCoordinator(eventBus, logger, ecsRuntime, enableHDR, enablePostProcessing);
    }
}
