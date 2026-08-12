package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @author ZZZank
 */
public class HasResultEvents {

    @Event.HasResult
    public static class Result extends Event {
    }

    public static class NoResult extends Event {
    }
}
