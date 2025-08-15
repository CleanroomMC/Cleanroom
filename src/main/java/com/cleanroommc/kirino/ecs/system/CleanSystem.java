package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.world.CleanWorld;

public abstract class CleanSystem {
    public abstract void update();

    protected final CleanWorld world;

    public CleanSystem(CleanWorld world) {
        this.world = world;
    }
}
