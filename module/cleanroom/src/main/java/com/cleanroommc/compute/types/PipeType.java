package com.cleanroommc.compute.types;

public record PipeType(String typeName) implements OpenCLType {
    public PipeType(OpenCLType type) {
        this(type.toString());
    }

    @Override
    public int sizeof() {
        return 8;
    }

    @Override
    public String toString() {
        return String.format("pipe %s",  typeName);
    }
}
