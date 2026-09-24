package com.cleanroommc.compute.types;

public enum OpenCLPrimitive implements OpenCLType {
    BOOL(1, "bool"),
    CHAR(1, "char"),
    UCHAR(1, "uchar"),
    SHORT(2, "short"),
    USHORT(2, "ushort"),
    INT(4, "int"),
    UINT(4, "uint"),
    LONG(8, "long"),
    ULONG(8, "ulong"),
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
