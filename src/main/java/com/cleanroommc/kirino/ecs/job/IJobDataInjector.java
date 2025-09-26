package com.cleanroommc.kirino.ecs.job;

public interface IJobDataInjector {
    void inject(Object owner, Object value);
}
