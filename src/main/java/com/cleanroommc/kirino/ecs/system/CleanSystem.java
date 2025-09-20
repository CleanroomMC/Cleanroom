package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.job.JobScheduler;

public abstract class CleanSystem {
    public abstract void update(JobScheduler jobScheduler);
}
