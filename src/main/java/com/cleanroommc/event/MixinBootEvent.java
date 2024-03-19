package com.cleanroommc.event;

import java.util.List;

/**
 * MixinBootEvent is fired when MixinBooter gather the MixinLoader
 * Early - {@link zone.rong.mixinbooter.IEarlyMixinLoader}
 * Late  - {@link zone.rong.mixinbooter.ILateMixinLoader}
 */
public class MixinBootEvent {
    public record Early(List<Object> mixinLoader) { }
    public record Late(List<Object> mixinLoader){ }
}
