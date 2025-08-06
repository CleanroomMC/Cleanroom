package com.cleanroommc.kirino.ecs.component.scan;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class StructScanEvent extends Event {
    public final List<String> scanPackageNames = new ArrayList<>();
}
