package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ExampleEvent extends Event {
    public static int CURRENT_ID = 0;

    public final int id;

    public ExampleEvent() {
        this.id = CURRENT_ID++;
    }
}
