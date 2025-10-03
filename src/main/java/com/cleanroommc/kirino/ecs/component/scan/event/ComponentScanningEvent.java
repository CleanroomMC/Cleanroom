package com.cleanroommc.kirino.ecs.component.scan.event;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public final class ComponentScanningEvent extends Event {
    private final List<String> scanPackageNames = new ArrayList<>();

    public void register(String packageName) {
        scanPackageNames.add(packageName);
    }
}
