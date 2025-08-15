package com.cleanroommc.kirino.mcbridge.ecs.system;

import com.cleanroommc.kirino.ecs.system.render.RenderSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;

public class MinecraftRenderSystem extends RenderSystem {
    @Override
    public void update() {

    }

    public MinecraftRenderSystem(CleanWorld world) {
        super(world);
        world.addSystem(this);
    }
}
