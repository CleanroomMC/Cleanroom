package com.cleanroommc.event;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;


public class MixinBootEvent {
    public record Early(List<Object> mixinLoader) { }
    public record Late(List<ILateMixinLoader> mixinLoader){ }
}
