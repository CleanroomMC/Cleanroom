package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import org.jspecify.annotations.NonNull;

public abstract class CleanSystem {
    public abstract void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);
}
