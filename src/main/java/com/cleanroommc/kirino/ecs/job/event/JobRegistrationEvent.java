package com.cleanroommc.kirino.ecs.job.event;

import com.cleanroommc.kirino.ecs.job.IParallelJob;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class JobRegistrationEvent extends Event {
    private final List<Class<? extends IParallelJob>> parallelJobClasses = new ArrayList<>();

    public void register(Class<? extends IParallelJob> parallelJobClass) {
        parallelJobClasses.add(parallelJobClass);
    }
}
