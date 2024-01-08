package com.cleanroommc.event;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

/**
 * @Project Cleanroom
 * @Author Hileb
 * @Date 2024/1/8 23:36
 **/
public class MixinBootEvent {
    public record Early(List<Object> mixinLoader) { }
    public record Late(List<ILateMixinLoader> mixinLoader){ }
}
