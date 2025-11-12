package com.cleanroommc.kirino.ecs.job;

import org.jspecify.annotations.NonNull;

public interface IJobInstantiator {
    @NonNull Object instantiate();
}
