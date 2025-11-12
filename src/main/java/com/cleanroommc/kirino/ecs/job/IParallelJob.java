package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import org.jspecify.annotations.NonNull;

public interface IParallelJob {
    /**
     * Every execution must be stateless except the index.
     *
     * @param entityManager The entity manager
     * @param index The index
     */
    void execute(@NonNull EntityManager entityManager, int index);
    void query(@NonNull EntityQuery entityQuery);
}
