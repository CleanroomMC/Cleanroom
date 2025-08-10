package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;

import java.util.List;

public class EntityCommand {
    public enum Type {
        CREATE,
        DESTROY,
        SET_COM,
        ADD_COM,
        REMOVE_COM
    }

    public final int index;
    public final Type type;

    public List<ICleanComponent> newComponents;
    public ICleanComponent componentToSet;
    public ICleanComponent componentToAdd;
    public Class<? extends ICleanComponent> componentToRemove;

    protected EntityCommand(int index, Type type) {
        this.index = index;
        this.type = type;
    }
}
