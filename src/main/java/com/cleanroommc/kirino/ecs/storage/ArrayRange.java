package com.cleanroommc.kirino.ecs.storage;

import java.util.List;

public final class ArrayRange {
    public final int start;
    public final int end;
    public final List<Integer> deprecatedIndexes;

    ArrayRange(int start, int end, List<Integer> deprecatedIndexes) {
        this.start = start;
        this.end = end;
        this.deprecatedIndexes = deprecatedIndexes;
    }
}
