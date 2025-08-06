package com.cleanroommc.kirino.ecs.system.render;

import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;

public class RenderSystem extends CleanSystem {
    private final CleanWorld world;

    public RenderSystem(CleanWorld world) {
        this.world = world;
    }
}
