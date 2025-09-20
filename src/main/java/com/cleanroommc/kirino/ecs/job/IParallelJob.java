package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.entity.EntityQuery;

public interface IParallelJob {
    /**
     * Every execution must be stateless except the index.
     *
     * @param index The index
     */
    void execute(int index);
    void query(EntityQuery entityQuery);
}
