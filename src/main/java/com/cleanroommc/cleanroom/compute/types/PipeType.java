package com.cleanroommc.cleanroom.compute.types;

import org.jspecify.annotations.NonNull;

public record PipeType(String typeName) implements OpenCLType {
    public PipeType(OpenCLType type) {
        this(type.toString());
    }

    @Override
    public int sizeof() {
        return 8;
    }

    @Override
    public @NonNull String toString() {
        return String.format("pipe %s",  typeName);
    }
}
