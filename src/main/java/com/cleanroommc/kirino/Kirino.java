package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Kirino {
    public static final Logger LOGGER = LogManager.getLogger("Kirino Rendering");
    public static final CleanECSRuntime ECS_RUNTIME = new CleanECSRuntime();
}
