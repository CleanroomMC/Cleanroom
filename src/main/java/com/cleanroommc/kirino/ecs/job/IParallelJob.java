package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.storage.INativeArray;

import java.util.List;

public interface IParallelJob {
    List<JobDataQuery> queries();
    Object execute(List<INativeArray<?>> arrays, int index);
}
