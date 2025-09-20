package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;

import java.util.ArrayList;
import java.util.List;

public final class EntityQuery {
    protected final List<Class<? extends ICleanComponent>> mustHave;
    protected final List<Class<? extends ICleanComponent>> mustNotHave;

    private EntityQuery() {
        mustHave = new ArrayList<>();
        mustNotHave = new ArrayList<>();
    }

    protected static EntityQuery query() {
        return new EntityQuery();
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li><code>component</code> is valid and registered</li>
     * </ul>
     *
     * @param component The component class
     * @return The query object
     */
    public EntityQuery with(Class<? extends ICleanComponent> component) {
        mustHave.add(component);
        return this;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li><code>component</code> is valid and registered</li>
     * </ul>
     *
     * @param component The component class
     * @return The query object
     */
    public EntityQuery without(Class<? extends ICleanComponent> component) {
        mustNotHave.add(component);
        return this;
    }
}
