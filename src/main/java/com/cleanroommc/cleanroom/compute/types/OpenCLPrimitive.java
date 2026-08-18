package com.cleanroommc.cleanroom.compute.types;

public enum OpenCLPrimitive implements OpenCLType {
    BOOL(1, "bool"),
    CHAR(1, "char"),
    SHORT(2, "short"),
    INT(4, "int"),
    LONG(8, "long"),
    SIZE(8, "size_t"),
    PTRDIFF(8, "ptrdiff_t"),
    INTPTR(8, "intptr_t"),
    HALF(2, "half"),
    FLOAT(4, "float"),
    DOUBLE(8, "double"),
    COMMAND_QUEUE(8, "queue_t"),
    SAMPLER(8, "sampler_t");

    public final int sizeof;
    public final String name;

    OpenCLPrimitive(int sizeof, String name) {
        this.sizeof = sizeof;
        this.name = name;
    }

    @Override
    public int sizeof() {
        return this.sizeof;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
