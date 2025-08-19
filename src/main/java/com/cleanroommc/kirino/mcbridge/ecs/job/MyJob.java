package com.cleanroommc.kirino.mcbridge.ecs.job;

import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.storage.INativeArray;
import com.cleanroommc.kirino.mcbridge.ecs.component.PositionComponent;

public class MyJob implements IParallelJob {
    // array will be injected before execution
    @JobDataQuery(componentClass = PositionComponent.class, fieldNameChain = {"xyz", "x"})
    INativeArray<Integer> array;

    @Override
    public Object execute(int index) {
        // SIMD will hopefully be enabled
        array.set(index, array.get(index) + 1);

        return null;
    }
}
