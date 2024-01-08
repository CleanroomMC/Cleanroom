package com.cleanroommc.event;

import com.google.common.eventbus.EventBus;

/**
 * The Event Bus used for early event.
 * For example: Transform event, Config event, mod load event...
 * Because the design. You need to register instances to subscribe the events.
 * for example:
 * <pre>
 *     @ Subscribe
 *     public void listen(Event event){
 *         //...
 *     }
 * </pre>
**/
public class EarlyBus {
    public static final EventBus BUS=new EventBus("cleanroom_early_bus");
}
