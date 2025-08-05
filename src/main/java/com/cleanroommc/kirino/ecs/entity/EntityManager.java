package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArchetypeKey;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityManager {
    private final ComponentRegistry componentRegistry;

    public EntityManager(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    // key: entity id
    private final BiMap<Integer, CleanEntity> entities = HashBiMap.create();
    private final Map<Integer, List<Class<? extends ICleanComponent>>> entityComponentMap = new HashMap<>();
    private final Map<Integer, ArchetypeKey> entityArchetypeLocationMap = new HashMap<>();
    private final Map<Integer, Integer> entityGenerationMap = new HashMap<>();

    private final List<Integer> freeIndexes = new ArrayList<>();
    private int indexCounter;

    private final Map<ArchetypeKey, ArchetypeDataPool> archetypes = new HashMap<>();
}
