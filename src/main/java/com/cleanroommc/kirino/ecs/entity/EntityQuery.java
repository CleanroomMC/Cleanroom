package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class EntityQuery {
    final List<Class<? extends ICleanComponent>> mustHave;
    final List<Class<? extends ICleanComponent>> mustNotHave;

    private EntityQuery() {
        mustHave = new ArrayList<>();
        mustNotHave = new ArrayList<>();
    }

    @NonNull
    static EntityQuery query() {
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
    @NonNull
    public EntityQuery with(@NonNull Class<? extends ICleanComponent> component) {
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
    @NonNull
    public EntityQuery without(@NonNull Class<? extends ICleanComponent> component) {
        mustNotHave.add(component);
        return this;
    }
}
