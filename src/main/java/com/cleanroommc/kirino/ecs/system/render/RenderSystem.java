package com.cleanroommc.kirino.ecs.system.render;

import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;

public abstract class RenderSystem extends CleanSystem {
    public RenderSystem(CleanWorld world) {
        super(world);
    }
}
