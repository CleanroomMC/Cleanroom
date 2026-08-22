package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class ExampleEvent extends Event {
    public static int CURRENT_ID = 0;

    public final int id;
    public final Set<String> sink = new HashSet<>();

    public ExampleEvent() {
        this.id = CURRENT_ID++;
    }
}
