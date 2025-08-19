package com.cleanroommc.kirino.ecs.job;

public interface IParallelJob {
    Object execute(int index);
}
